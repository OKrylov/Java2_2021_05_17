package ru.gb.java2.chat.client.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ChatHistory implements AutoCloseable {

    private static final String FILENAME_PATTERN = "./history/history_%s.txt";

    private final String username;
    private PrintWriter printWriter;
    private File historyFile;

    public ChatHistory(String username) {
        this.username = username;
    }

    public void init() {
        try {
            historyFile = createHistoryFile();
            this.printWriter = new PrintWriter(new BufferedWriter(new FileWriter(historyFile, StandardCharsets.UTF_8, true)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void appendText(String text) {
        printWriter.print(text);
        printWriter.flush();
    }

    private File createHistoryFile() throws IOException {
        String filePath = String.format(FILENAME_PATTERN, username);
        File file = new File(filePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        return file;
    }

    @Override
    public void close() throws Exception {
        if (printWriter != null) {
            printWriter.close();
        }
    }

    public List<String> loadLastRows(int rowsNumber) {
        List<String> result = new ArrayList<>();

        try(RandomAccessFile raf = new RandomAccessFile(historyFile, "r")) {
            long pointer;
            int count = 0;

            long lastPointer = raf.length() - 1;
            for (pointer = raf.length() - 1; pointer > 0; pointer--) {
                raf.seek(pointer);
                if (raf.read() == '\n') {
                    count++;

                    byte[] data = new byte[(int) (lastPointer - pointer)];
                    raf.read(data, 0, data.length);
                    result.add(String.valueOf(new StringBuilder().append(data).reverse()));

                }

                if (count == rowsNumber) {
                    break;
                }
            }

            if (pointer >= 0) {
                raf.seek(pointer);
            }

            String line;
            while((line = raf.readLine()) != null) {
                result.add(new String(line.getBytes(StandardCharsets.US_ASCII)));
            }

            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
