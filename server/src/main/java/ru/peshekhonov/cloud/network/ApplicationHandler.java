package ru.peshekhonov.cloud.network;

import ru.peshekhonov.cloud.Frame;
import ru.peshekhonov.cloud.StatusType;
import ru.peshekhonov.cloud.frames.StartData;
import ru.peshekhonov.cloud.frames.SubsequentData;

import java.io.*;
import java.net.Socket;

public class ApplicationHandler {

    private final Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private OutputStream out;
    private long fileLength;
    private long size;
    private String filename;
    private File file;
    private File serverFile;

    public ApplicationHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void handle() throws IOException {
        serverFile = new File("server/files");
        inputStream = new ObjectInputStream(clientSocket.getInputStream());
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

        Server.executorService.execute(() -> {
            try {
                readMessages();
            } catch (IOException e) {
                System.err.println("Failed to process frames from client");
                e.printStackTrace();
            } finally {
                try {
                    closeConnection();
                } catch (IOException e) {
                    System.err.println("Failed to close connection");
                    e.printStackTrace();
                }
            }
        });

    }

    private void closeConnection() throws IOException {
        if (!clientSocket.isClosed()) {
            clientSocket.close();
        }
    }

    private void readMessages() throws IOException {
        while (true) {
            Frame frame = readFrame();
            if (frame == null) {
                continue;
            }

            switch (frame.getType()) {
                case START: {
                    if (filename != null) {
                        System.err.println("Inconsistent start frame");
                        sendStatusFrame(Frame.createStatusFrame("", StatusType.ERROR, "Inconsistent data"));
                        break;
                    }
                    System.out.println("Start frame");
                    StartData data = (StartData) frame.getData();
                    filename = data.getFilename();
                    file = new File(serverFile, filename);
                    if (file.exists()) {
                        String message = String.format("File \"%s\" is already exist.", filename);
                        sendStatusFrame(Frame.createStatusFrame(filename, StatusType.ERROR, message));
                        filename = null;
                        file = null;
                        break;
                    }
                    try {
                        out = new BufferedOutputStream(new FileOutputStream(file, true));
                        fileLength = data.getLength();
                        byte[] array = data.getData();
                        size = array.length;
                        out.write(array);
                        flushIfEndOfFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        handleErrorWritingToFile();
                    }
                    break;
                }
                case CONTINUE: {
                    if (filename == null) {
                        System.err.println("Inconsistent continue frame");
                        sendStatusFrame(Frame.createStatusFrame("", StatusType.ERROR, "Inconsistent data"));
                        break;
                    }
                    System.out.println("Continue frame");
                    SubsequentData data = (SubsequentData) frame.getData();
                    byte[] array = data.getData();
                    size += array.length;
                    try {
                        out.write(array);
                        flushIfEndOfFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        handleErrorWritingToFile();
                    }
                    break;
                }
                case FILE_LIST_REQUEST: {
                    System.out.println("File list request frame");
                    sendFrame(Frame.createFileListFrame(serverFile));
                    break;
                }
                default:
                    System.out.println("Undefined frame");
            }
        }
    }

    private void flushIfEndOfFile() throws IOException {
        if (size >= fileLength) {
            out.flush();
            out.close();
            sendStatusFrame(Frame.createStatusFrame(filename, StatusType.OK));
            filename = null;
            file = null;
        }
    }

    private void handleErrorWritingToFile() {
        System.err.println("Failed to write to file " + "\"" + filename + "\"");
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String message = String.format("Failed to copy file \"%s\".", filename);
        sendStatusFrame(Frame.createStatusFrame(filename, StatusType.ERROR, message));
        filename = null;
        if (file != null && file.exists()) {
            file.delete();
            file = null;
        }
    }

    private Frame readFrame() throws IOException {
        Frame frame = null;
        try {
            frame = (Frame) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to read Frame class");
            e.printStackTrace();
        }
        return frame;
    }

    public void sendStatusFrame(Frame frame) {
        try {
            outputStream.writeObject(frame);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFrame(Frame frame) throws IOException {
        outputStream.writeObject(frame);
    }
}
