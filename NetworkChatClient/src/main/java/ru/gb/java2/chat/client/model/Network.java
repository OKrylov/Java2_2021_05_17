package ru.gb.java2.chat.client.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Network {

    private static final int SERVER_PORT = 8189;
    private static final String SERVER_HOST = "localhost";

    private static Network INSTANCE;

    private final String host;
    private final int port;
    private Socket socket;
    private DataInputStream socketInput;
    private DataOutputStream socketOutput;

    private List<ReadMessageListener> listeners = new CopyOnWriteArrayList<>();
    private Thread readMessageProcess;
    private boolean connected;


    public static Network getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Network();
        }

        return INSTANCE;
    }


    private Network(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private Network() {
        this(SERVER_HOST, SERVER_PORT);
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            socketInput = new DataInputStream(socket.getInputStream());
            socketOutput = new DataOutputStream(socket.getOutputStream());
            readMessageProcess = startReadMessageProcess();
            connected = true;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to establish connection");
            return false;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    private Thread startReadMessageProcess() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    String message = socketInput.readUTF();
                    for (ReadMessageListener messageListener : listeners) {
                        messageListener.processReceivedMessage(message);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to read message from server");
                    close();
                    break;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    public void sendMessage(String message) throws IOException {
        try {
            socketOutput.writeUTF(message);
        } catch (IOException e) {
            System.err.println("Failed to send message to server");
            throw e;
        }
    }

    public ReadMessageListener addReadMessageListener(ReadMessageListener listener) {
        listeners.add(listener);
        return listener;
    }

    public void removeReadMessageListener(ReadMessageListener listener) {
        listeners.remove(listener);
    }

    public void close() {
        try {
            readMessageProcess.interrupt();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
