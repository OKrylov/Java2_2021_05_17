package ru.gb.java2.chat.client.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import ru.gb.java2.chat.client.dialogs.Dialogs;
import ru.gb.java2.chat.client.model.Network;
import ru.gb.java2.chat.client.model.ReadMessageListener;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ChatController {

    private static final List<String> USERS_TEST_DATA = List.of(
            "username1",
            "username2",
            "username3");

    @FXML
    public ListView<String> usersList;
    @FXML
    private Button sendButton;
    @FXML
    private TextArea chatHistory;
    @FXML
    private TextArea messageTextArea;


    @FXML
    public void initialize() {
        usersList.setItems(FXCollections.observableArrayList(USERS_TEST_DATA));
    }

    @FXML
    private void sendMessage() {
        String message = messageTextArea.getText().trim();
        if (message.isEmpty()) {
            messageTextArea.clear();
            return;
        }

        String sender = null;
        if (!usersList.getSelectionModel().isEmpty()) {
            sender = usersList.getSelectionModel().getSelectedItem();
        }

        try {
            message = sender != null ? String.join(": ", sender, message) : message; // sender + ": " + message
            Network.getInstance().sendMessage(message);
        } catch (IOException e) {
            Dialogs.NetworkError.SEND_MESSAGE.show();
        }
        appendMessageToChat("Я", message);
    }

    private void appendMessageToChat(String sender, String message) {
        chatHistory.appendText(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.appendText(System.lineSeparator());
        if (sender != null) {
            chatHistory.appendText(sender + ":");
            chatHistory.appendText(System.lineSeparator());
        }
        chatHistory.appendText(message);
        chatHistory.appendText(System.lineSeparator());
        chatHistory.appendText(System.lineSeparator());
        messageTextArea.clear();
    }

    @FXML
    public void sendTextAreaMessage(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            event.consume(); // otherwise a new line will be added to the textArea after the sendFunction() call
            if (event.isShiftDown()) {
                messageTextArea.appendText(System.lineSeparator());
            } else {
                sendMessage();
            }
        }
    }

    public void initMessageHandler() {
        Network.getInstance().addReadMessageListener(new ReadMessageListener() {
            @Override
            public void processReceivedMessage(String message) {
                Platform.runLater(() -> ChatController.this.appendMessageToChat("Server", message));
            }
        });
    }

}