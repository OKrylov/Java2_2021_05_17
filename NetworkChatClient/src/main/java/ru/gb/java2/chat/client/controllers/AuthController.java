package ru.gb.java2.chat.client.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ru.gb.java2.chat.client.ClientChat;
import ru.gb.java2.chat.client.dialogs.Dialogs;
import ru.gb.java2.chat.client.model.Network;
import ru.gb.java2.chat.client.model.ReadMessageListener;

import java.io.IOException;

public class AuthController {
    private static final String AUTH_COMMAND = "/auth";
    private static final String AUTH_OK_COMMAND = "/authOk";

    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button authButton;
    private ReadMessageListener readMessageListener;


    @FXML
    public void executeAuth(ActionEvent actionEvent) {
        String login = loginField.getText();
        String password = passwordField.getText();
        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            Dialogs.AuthError.EMPTY_CREDENTIALS.show();
            return;
        }

        if (!connectToServer()) {
            Dialogs.NetworkError.SERVER_CONNECT.show();
            return;
        }

        String authCommandMessage = String.format("%s %s %s", AUTH_COMMAND, login, password);
        try {
            Network.getInstance().sendMessage(authCommandMessage);
        } catch (IOException e) {
            Dialogs.NetworkError.SEND_MESSAGE.show();
            e.printStackTrace();
        }
    }

    private boolean connectToServer() {
        Network network = getNetwork();
        return network.isConnected() || network.connect();
    }

    private Network getNetwork() {
        return Network.getInstance();
    }

    public void initMessageHandler() {
        readMessageListener = getNetwork().addReadMessageListener(new ReadMessageListener() {
            @Override
            public void processReceivedMessage(String message) {
                if (message.startsWith(AUTH_OK_COMMAND)) {
                    String[] parts = message.split(" ");
                    String username = parts[1];
                    Platform.runLater(() -> ClientChat.INSTANCE.switchToMainChatWindow(username));
                } else {
                    Platform.runLater(Dialogs.AuthError.INVALID_CREDENTIALS::show);
                }
            }
        });
    }

    public void close() {
        getNetwork().removeReadMessageListener(readMessageListener);
    }
}
