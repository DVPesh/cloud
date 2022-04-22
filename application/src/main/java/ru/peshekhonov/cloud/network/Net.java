package ru.peshekhonov.cloud.network;

import javafx.application.Platform;
import ru.peshekhonov.cloud.Frame;
import ru.peshekhonov.cloud.controller.CloudController;
import ru.peshekhonov.cloud.frames.FileListData;
import ru.peshekhonov.cloud.frames.StatusData;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class Net {

    public static final int SERVER_PORT = 8189;
    public static final String SERVER_HOST = "localhost";
    private Socket socket;
    private ObjectInputStream socketInput;
    private ObjectOutputStream socketOutput;
    private final CloudController cloudController;

    public Net(CloudController cloudController) {
        this.cloudController = cloudController;
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            socketOutput = new ObjectOutputStream(socket.getOutputStream());
            socketInput = new ObjectInputStream(socket.getInputStream());
            startReadMessageProcess();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to set connection");
        }
    }

    public void startReadMessageProcess() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Frame frame = readFrame();
                    if (frame == null) {
                        continue;
                    }
                    switch (frame.getType()) {
                        case STATUS: {
                            StatusData data = (StatusData) frame.getData();
                            System.out.printf("Status frame: %s [%s] %s%n", data.getStatus(), data.getFilename(), data.getMessage());
                            break;
                        }
                        case FILE_LIST: {
                            System.out.println("File list frame");
                            FileListData data = (FileListData) frame.getData();
                            Platform.runLater(() -> {
                                cloudController.updateServerListView(data.getFileList());
                            });

                            break;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Failed to read frame from server");
                    close();
                    break;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    cloudController.updateClientListView();
                    try {
                        sendFrame(Frame.createFileListRequestFrame());
                    } catch (IOException e) {
                        System.err.println("Failed to send frame to server");
                        e.printStackTrace();
                    }
                });
            }
        }, 100, 3000);
    }

    public void close() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Frame readFrame() throws IOException {
        Frame frame = null;
        try {
            frame = (Frame) socketInput.readObject();
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to read Frame class");
            e.printStackTrace();
        }
        return frame;
    }

    public void sendFrame(Frame frame) throws IOException {
        socketOutput.writeObject(frame);
    }
}
