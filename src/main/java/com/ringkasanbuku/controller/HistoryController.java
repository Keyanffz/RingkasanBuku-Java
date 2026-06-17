package com.ringkasanbuku.controller;

import com.ringkasanbuku.App;
import com.ringkasanbuku.model.Summary;
import com.ringkasanbuku.repository.HistoryRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.util.List;

public class HistoryController {

    @FXML private ListView<Summary> historyListView;
    @FXML private TextArea detailArea;

    private HistoryRepository historyRepository;

    @FXML
    public void initialize() {
        historyRepository = new HistoryRepository();
        loadHistory();

        historyListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Summary item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTimestamp().toString() + " - " + item.getTitle() + " (" + item.getMethod() + ")");
                }
            }
        });

        historyListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                detailArea.setText("Teks Asli:\n" + newValue.getOriginalText() + "\n\n" +
                                   "Hasil Ringkasan:\n" + newValue.getResult());
            } else {
                detailArea.clear();
            }
        });
    }

    private void loadHistory() {
        List<Summary> summaries = historyRepository.loadAll();
        historyListView.getItems().setAll(summaries);
    }

    @FXML
    public void handleBack() throws IOException {
        App.setRoot("fxml/main");
    }

    @FXML
    public void handleDelete() {
        Summary selected = historyListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            historyRepository.delete(selected.getId());
            loadHistory();
            detailArea.clear();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Peringatan");
            alert.setHeaderText(null);
            alert.setContentText("Pilih ringkasan yang ingin dihapus!");
            alert.showAndWait();
        }
    }
}
