package com.example;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;

public class HelloController {

    private final HelloModel model = new HelloModel();

    @FXML
    private ListView<NtfyMessageDto> messageView;

    @FXML
    private TextField messageField;

    @FXML
    private Button emojiButton;

    @FXML
    private void initialize() {
        messageView.setItems(model.getMessages());

        messageView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(NtfyMessageDto msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setGraphic(null);
                    return;
                }

                Label label = new Label(msg.toString());
                label.setWrapText(true);
                label.setMaxWidth(180);

                if ("me".equals(msg.topic())) {
                    label.setStyle("-fx-background-color: lightgreen; -fx-padding: 6; -fx-background-radius: 8;");
                    setAlignment(Pos.CENTER_RIGHT);
                } else {
                    label.setStyle("-fx-background-color: lightgray; -fx-padding: 6; -fx-background-radius: 8;");
                    setAlignment(Pos.CENTER_LEFT);
                }

                setGraphic(label);
            }
        });
    }

    @FXML
    private void sendMessage(ActionEvent actionEvent) {
        String text = messageField.getText().trim();
        if (text.isEmpty()) return;

        messageField.clear();
        model.sendMessage(text);
    }

    @FXML
    private void emojis() {
        String[] emojis = {"😀", "😂", "😍", "😎", "😭", "👍", "🎉"};
        ContextMenu menu = new ContextMenu();
        for (String e : emojis) {
            MenuItem item = new MenuItem(e);
            item.setOnAction(ev -> messageField.appendText(e));
            menu.getItems().add(item);
        }
        menu.show(emojiButton, Side.BOTTOM, 0, 0);
    }
}
