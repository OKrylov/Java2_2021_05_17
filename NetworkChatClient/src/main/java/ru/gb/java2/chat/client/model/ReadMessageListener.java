package ru.gb.java2.chat.client.model;

public interface ReadMessageListener {

    void processReceivedMessage(String message);
}
