package com.ringkasanbuku.controller;

import com.ringkasanbuku.exporter.PDFExporter;
import com.ringkasanbuku.exporter.TxtExporter;
import com.ringkasanbuku.model.Summary;
import com.ringkasanbuku.model.SummaryLength;
import com.ringkasanbuku.model.SummaryOptions;
import com.ringkasanbuku.model.SummaryStyle;
import com.ringkasanbuku.repository.HistoryRepository;
import com.ringkasanbuku.summarizer.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.File;

public class MainController {

    @FXML private TextArea inputArea;
    @FXML private TextArea resultArea;
    @FXML private ComboBox<SummaryLength> lengthComboBox;
    @FXML private ComboBox<SummaryStyle> styleComboBox;
    @FXML private Button summarizeButton;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;

    private APIFallbackManager apiFallbackManager;
    private Summarizer tfidfSummarizer;
    private HistoryRepository historyRepository;
    private Summary currentSummary;

    @FXML
    public void initialize() {
        // Setup Combo Boxes
        lengthComboBox.getItems().setAll(SummaryLength.values());
        lengthComboBox.setValue(SummaryLength.MEDIUM);

        styleComboBox.getItems().setAll(SummaryStyle.values());
        styleComboBox.setConverter(new javafx.util.StringConverter<SummaryStyle>() {
            @Override
            public String toString(SummaryStyle style) {
                if (style == SummaryStyle.EXTRACTIVE) return "Ringkas dengan Rulebased";
                if (style == SummaryStyle.ABSTRACTIVE) return "Ringkas dengan LLM";
                return style == null ? "" : style.toString();
            }

            @Override
            public SummaryStyle fromString(String string) {
                return null; // Tidak digunakan karena ComboBox tidak bisa di-edit manual oleh user
            }
        });
        styleComboBox.setValue(SummaryStyle.ABSTRACTIVE);

        // Init Managers
        apiFallbackManager = new APIFallbackManager();
        apiFallbackManager.addProvider(new GroqSummarizer());
        apiFallbackManager.addProvider(new GeminiSummarizer());
        apiFallbackManager.addProvider(new OpenAISummarizer());
        
        tfidfSummarizer = new TFIDFSummarizer();
        historyRepository = new HistoryRepository();
    }

    @FXML
    public void handleSummarize() {
        String raw = inputArea.getText();
        String text = com.ringkasanbuku.util.TextPreprocessor.clean(raw);
        if (text == null || text.trim().isEmpty()) {
            showAlert("Error", "Teks asli tidak boleh kosong!");
            return;
        }

        SummaryOptions options = new SummaryOptions(lengthComboBox.getValue(), styleComboBox.getValue());
        boolean useLLM = options.getStyle() == SummaryStyle.ABSTRACTIVE;

        summarizeButton.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        statusLabel.setText("Memproses teks...");
        resultArea.clear();

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                com.ringkasanbuku.util.TextChunker chunker = new com.ringkasanbuku.util.TextChunker(3000, 200);
                java.util.List<String> chunks = chunker.chunk(text);

                if (chunks.size() <= 1) {
                    if (useLLM) {
                        updateMessage("Mengirim ke API...");
                        return apiFallbackManager.summarize(text, options);
                    } else {
                        updateMessage("Memproses algoritma TF-IDF...");
                        return tfidfSummarizer.summarize(text, options);
                    }
                }

                updateMessage("Membagi teks menjadi " + chunks.size() + " bagian (Hierarchical Summarization)...");
                StringBuilder combinedSummaries = new StringBuilder();

                for (int i = 0; i < chunks.size(); i++) {
                    updateMessage("Meringkas bagian " + (i + 1) + " dari " + chunks.size() + "...");
                    updateProgress(i, chunks.size() + 1); 
                    
                    String chunkSummary;
                    if (useLLM) {
                        // Tambahkan jeda waktu sebelum request berikutnya (kecuali untuk chunk pertama)
                        // Ini untuk menghindari batas Rate Limit (429) dari API LLM yang agresif
                        if (i > 0) {
                            updateMessage("Menunggu sejenak untuk menghindari API Rate Limit...");
                            Thread.sleep(3000); // Jeda 3 detik
                            updateMessage("Meringkas bagian " + (i + 1) + " dari " + chunks.size() + "...");
                        }
                        chunkSummary = apiFallbackManager.summarize(chunks.get(i), options);
                    } else {
                        chunkSummary = tfidfSummarizer.summarize(chunks.get(i), options);
                    }
                    combinedSummaries.append(chunkSummary).append("\n\n");
                }

                updateMessage("Merangkum hasil akhir...");
                updateProgress(chunks.size(), chunks.size() + 1);
                
                // Reduce step: Ringkas gabungan ringkasan-ringkasan kecil
                String finalSummary;
                if (useLLM) {
                    finalSummary = apiFallbackManager.summarize(combinedSummaries.toString(), options);
                } else {
                    finalSummary = tfidfSummarizer.summarize(combinedSummaries.toString(), options);
                }
                updateProgress(chunks.size() + 1, chunks.size() + 1);
                return finalSummary;
            }
        };

        // Bind progress bar & label ke task (agar update live)
        statusLabel.textProperty().bind(task.messageProperty());
        progressBar.progressProperty().bind(task.progressProperty());

        task.setOnSucceeded(e -> {
            statusLabel.textProperty().unbind();
            progressBar.progressProperty().unbind();
            String result = task.getValue();
            resultArea.setText(result);
            
            // Simpan history
            String method = useLLM ? "LLM" : "RULE_BASED";
            String provider = useLLM ? "APIFallbackManager" : null;
            currentSummary = new Summary("Ringkasan Baru", text, result, method, provider, options);
            historyRepository.save(currentSummary);
            
            resetUI("Selesai meringkas teks.");
        });

        task.setOnFailed(e -> {
            statusLabel.textProperty().unbind();
            progressBar.progressProperty().unbind();
            resetUI("Gagal meringkas teks.");
            showAlert("Error", "Terjadi kesalahan saat meringkas: " + task.getException().getMessage());
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    @FXML
    public void handleExportTxt() {
        export(new TxtExporter(), "ringkasan.txt");
    }

    @FXML
    public void handleExportPdf() {
        export(new PDFExporter(), "ringkasan.pdf");
    }

    @FXML
    public void handleViewHistory() throws java.io.IOException {
        com.ringkasanbuku.App.setRoot("fxml/history");
    }

    @FXML
    public void handleImportFile() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Pilih File Teks atau PDF");
        fileChooser.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("Text & PDF Files", "*.txt", "*.pdf")
        );
        File file = fileChooser.showOpenDialog(inputArea.getScene().getWindow());
        if (file != null) {
            String fileName = file.getName().toLowerCase();
            if (!fileName.endsWith(".txt") && !fileName.endsWith(".pdf")) {
                showAlert("Format Tidak Didukung", "Maaf, saat ini aplikasi hanya bisa meng-import file berformat PDF atau TXT.");
                return;
            }

            try {
                String content;
                if (fileName.endsWith(".pdf")) {
                    try (org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.Loader.loadPDF(file)) {
                        org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
                        content = stripper.getText(document);
                    }
                } else {
                    content = java.nio.file.Files.readString(file.toPath());
                }
                inputArea.setText(content);
                showAlert("Sukses", "File " + file.getName() + " berhasil dimuat!");
            } catch (Exception e) {
                showAlert("Error", "Gagal membaca file: " + e.getMessage());
            }
        }
    }

    private void export(com.ringkasanbuku.exporter.FileExporter exporter, String filename) {
        if (currentSummary == null || resultArea.getText().trim().isEmpty()) {
            showAlert("Peringatan", "Tidak ada hasil ringkasan untuk diekspor!");
            return;
        }
        try {
            exporter.export(currentSummary, filename);
            showAlert("Sukses", "Berhasil diekspor ke " + new File(filename).getAbsolutePath());
        } catch (Exception e) {
            showAlert("Error", "Gagal mengekspor file: " + e.getMessage());
        }
    }

    private void resetUI(String status) {
        summarizeButton.setDisable(false);
        progressBar.setVisible(false);
        progressBar.setProgress(0);
        statusLabel.setText(status);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}