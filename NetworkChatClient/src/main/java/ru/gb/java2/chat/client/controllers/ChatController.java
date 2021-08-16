package ru.gb.java2.chat.client.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import ru.gb.java2.chat.client.ClientChat;
import ru.gb.java2.chat.client.dialogs.Dialogs;
import ru.gb.java2.chat.client.model.Network;
import ru.gb.java2.chat.client.model.ReadCommandListener;
import ru.gb.java2.chat.clientserver.Command;
import ru.gb.java2.chat.clientserver.CommandType;
import ru.gb.java2.chat.clientserver.commands.ClientMessageCommandData;
import ru.gb.java2.chat.clientserver.commands.UpdateUsersListCommandData;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ChatController {

    @FXML
    private ListView<String> usersList;
    @FXML
    private Button reconnectButton;
    @FXML
    private Button sendButton;
    @FXML
    private TextArea chatHistory;
    @FXML
    private TextArea messageTextArea;

    @FXML
    private void sendMessage() {
        String message = messageTextArea.getText().trim();
        if (message.isEmpty()) {
            messageTextArea.clear();
            return;
        }

        String recipient = getSelectedUser();
        try {
            if (recipient != null) {
                Network.getInstance().sendPrivateMessage(recipient, message);
            } else {
                Network.getInstance().sendMessage(message);
            }
        } catch (IOException e) {
            Dialogs.NetworkError.SEND_MESSAGE.show();
        }
        appendMessageToChat("Я", message);
    }

    private String getSelectedUser() {
        if (!usersList.getSelectionModel().isEmpty()) {
            return usersList.getSelectionModel().getSelectedItem();
        }
        return null;
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
        Network.getInstance().addReadMessageListener(new ReadCommandListener() {
            @Override
            public void processReceivedCommand(Command command) {
                if (command.getType() == CommandType.CLIENT_MESSAGE) {
                    ClientMessageCommandData data = (ClientMessageCommandData) command.getData();
                    Platform.runLater(() -> ChatController.this.appendMessageToChat(data.getSender(), data.getMessage()));
                } else if (command.getType() == CommandType.UPDATE_USERS_LIST) {
                    UpdateUsersListCommandData data = (UpdateUsersListCommandData) command.getData();
                    updateUsersList(data.getUsers());
                }
            }
        });
    }

    public void updateUsersList(List<String> users) {
        Platform.runLater(() -> usersList.setItems(FXCollections.observableArrayList(users)));
    }

    public void reconnectToServer(ActionEvent actionEvent) {
        Network network = Network.getInstance();
        if (network.isConnected()) {
            return;
        }

        try {
            if (network.connect()) {
                network.sendAuthMessage(network.getLastLogin(), network.getLastPassword());
            } else {
                throw new Exception("Failed to connect");
            }
        } catch (Exception e) {
            Dialogs.NetworkError.SEND_MESSAGE.show();
        }
    }

    public void closeChat(ActionEvent actionEvent) {
        ClientChat.INSTANCE.getPrimaryStage().close();
    }

    public void changeUsername(ActionEvent actionEvent) {
        TextInputDialog editDialog = new TextInputDialog(Network.getInstance().getCurrentUsername());
        editDialog.setTitle("Изменить юзернейм");
        editDialog.setHeaderText("Введите новый юзернейм");
        editDialog.setContentText("Username:");

        Optional<String> result = editDialog.showAndWait();
        if (result.isPresent()) {
            try {
                Network.getInstance().changeUsername(result.get());
            } catch (IOException e) {
                Dialogs.NetworkError.SEND_MESSAGE.show();
            }

        }
    }
}