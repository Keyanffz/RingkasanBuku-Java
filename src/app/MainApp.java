package app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.File;

public class MainApp extends Application {

    private TextArea inputArea;
    private TextArea outputArea;
    private ComboBox<String> metodeCombo;
    private TextInputHandler inputHandler = new TextInputHandler();
    private SummaryHistoryManager historyManager = new SummaryHistoryManager();

    @Override
    public void start(Stage stage) {
        stage.setTitle("📚 Aplikasi Ringkasan Buku Otomatis");

        Label labelInput = new Label("📝 Masukkan Teks:");
        inputArea = new TextArea();
        inputArea.setPromptText("Paste atau ketik teks di sini...");
        inputArea.setWrapText(true);
        inputArea.setPrefHeight(200);

        Label labelMetode = new Label("⚙️ Metode Ringkasan:");
        metodeCombo = new ComboBox<>();
        metodeCombo.getItems().addAll("Rule-Based", "API-Based");
        metodeCombo.setValue("Rule-Based");

        Button btnRingkas  = new Button("✨ Ringkas");
        Button btnLoadFile = new Button("📂 Load File");
        Button btnSimpan   = new Button("💾 Simpan");
        Button btnRiwayat  = new Button("🕓 Riwayat");

        btnRingkas.setStyle(
            "-fx-background-color: #4A90D9; -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-padding: 8 20;"
        );

        HBox tombolBox = new HBox(10, btnLoadFile, btnRingkas, btnSimpan, btnRiwayat);
        tombolBox.setPadding(new Insets(5, 0, 5, 0));

        Label labelOutput = new Label("📄 Hasil Ringkasan:");
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setPrefHeight(150);
        outputArea.setStyle("-fx-background-color: #f0f8ff;");

        VBox root = new VBox(10,
            labelInput, inputArea,
            labelMetode, metodeCombo,
            tombolBox,
            labelOutput, outputArea
        );
        root.setPadding(new Insets(20));

        // Tombol Ringkas
        btnRingkas.setOnAction(e -> {
            String teks = inputArea.getText().trim();
            if (teks.isEmpty()) {
                showAlert("Input kosong!", "Masukkan teks terlebih dahulu.");
                return;
            }
            Summarizer summarizer;
            if (metodeCombo.getValue().equals("Rule-Based")) {
                summarizer = new RuleBasedSummarizer();
            } else {
                summarizer = new ApiBasedSummarizer();
            }
            String hasil = summarizer.summarize(teks);
            outputArea.setText(hasil);
            historyManager.tambahRiwayat(hasil);
        });

        // Tombol Load File
        btnLoadFile.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Pilih File Teks");
            fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );
            File file = fc.showOpenDialog(stage);
            if (file != null) {
                try {
                    String isi = inputHandler.loadFromFile(file.getAbsolutePath());
                    inputArea.setText(isi);
                } catch (Exception ex) {
                    showAlert("Error", "Gagal membaca file: " + ex.getMessage());
                }
            }
        });

        // Tombol Simpan
        btnSimpan.setOnAction(e -> {
    String hasil = outputArea.getText();
    if (hasil.isEmpty()) {
        showAlert("Belum ada ringkasan!", "Ringkas dulu sebelum menyimpan.");
        return;
    }

    // Munculkan pilihan format
    ChoiceDialog<String> dialog = new ChoiceDialog<>("TXT", "TXT", "PDF");
    dialog.setTitle("Simpan Ringkasan");
    dialog.setHeaderText("Pilih format file:");
    dialog.setContentText("Format:");

    dialog.showAndWait().ifPresent(format -> {
        SummaryFormatter formatter = new SummaryFormatter();
        if (format.equals("TXT")) {
            formatter.simpanKeTxt(hasil);
            showAlert("Berhasil!", "Disimpan ke hasil_ringkasan.txt");
        } else {
            formatter.simpanKePdf(hasil);
            showAlert("Berhasil!", "Disimpan ke hasil_ringkasan.pdf");
        }
        });
    });

        // Tombol Riwayat
        btnRiwayat.setOnAction(e -> {
            String riwayat = historyManager.getRiwayat();
            showInfo("📜 Riwayat Ringkasan", riwayat);
        });

        Scene scene = new Scene(root, 650, 600);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(msg.isEmpty() ? "Belum ada riwayat." : msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}