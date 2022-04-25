package ru.peshekhonov.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class Terminal {

    private Path dir;
    private final ServerSocketChannel serverChannel;
    private final Selector selector;
    private final ByteBuffer buffer = ByteBuffer.allocate(256);

    public Terminal() throws IOException {

        dir = Path.of("src/files").toAbsolutePath();

        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(8189));
        serverChannel.configureBlocking(false);

        selector = Selector.open();

        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started on port: 8189");

        while (serverChannel.isOpen()) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            try {
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    }
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                    iterator.remove();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        String message = readMessageFromChannel(channel).trim();
        System.out.println("Received: " + message);
        String[] command = message.split(" +", 2);
        String str = "";
        switch (command[0].toLowerCase(Locale.ROOT)) {
            case "ls":
                if (command.length == 1) {
                    channel.write(ByteBuffer.wrap(getLsResultString().getBytes(StandardCharsets.UTF_8)));
                } else {
                    str = "\"ls\" must not have parameters" + System.lineSeparator();
                    channel.write(ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8)));
                }
                break;
            case "mkdir":
                if (command.length == 2) {
                    try {
                        Path newDir = dir.resolve(command[1]);
                        Files.createDirectory(newDir);
                        break;
                    } catch (InvalidPathException e) {
                        str = "invalid pathname";
                        e.printStackTrace();
                    } catch (FileAlreadyExistsException e) {
                        str = "file already exists";
                        e.printStackTrace();
                    } catch (IOException e) {
                        str = "parent directory does not exist or I/O error";
                        e.printStackTrace();
                    }
                } else {
                    str = "name of new directory not specified";
                }
                str += System.lineSeparator();
                channel.write(ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8)));
                break;
            case "cd":
                if (command.length == 2) {
                    try {
                        Path path = dir.resolve(command[1]).normalize().toAbsolutePath();
                        if (Files.isDirectory(path)) {
                            dir = path;
                            break;
                        }
                        str = "directory does not exist";
                    } catch (InvalidPathException e) {
                        str = "invalid pathname";
                        e.printStackTrace();
                    }
                } else {
                    str = "name of directory not specified";
                }
                str += System.lineSeparator();
                channel.write(ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8)));
                break;
            case "touch":
                if (command.length == 2) {
                    try {
                        Path path = dir.resolve(command[1]);
                        Files.createFile(path);
                        break;
                    } catch (InvalidPathException e) {
                        str = "invalid pathname";
                        e.printStackTrace();
                    } catch (FileAlreadyExistsException e) {
                        str = "file already exists";
                        e.printStackTrace();
                    } catch (IOException e) {
                        str = "parent directory does not exist or I/O error";
                        e.printStackTrace();
                    }
                } else {
                    str = "filename not specified";
                }
                str += System.lineSeparator();
                channel.write(ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8)));
                break;
            case "cat":
                if (command.length == 2) {
                    try {
                        Path path = dir.resolve(command[1]);
                        StringBuilder sb = new StringBuilder();
                        if (Files.isDirectory(path)) {
                            str = "file is a directory";
                        } else if (Files.notExists(path)) {
                            str = "file is not exist";
                        } else if (Files.size(path) <= 0) {
                            str = "file is empty";
                        } else if (Files.isReadable(path)) {
                            List<String> lines = Files.readAllLines(path);
                            for (int i = 0; i < lines.size() - 1; i++) {
                                sb.append(lines.get(i));
                                sb.append(System.lineSeparator());
                            }
                            str = sb.append(lines.get(lines.size() - 1)).toString();
                        } else {
                            str = "file cannot be read";
                        }
                    } catch (InvalidPathException e) {
                        str = "invalid pathname";
                        e.printStackTrace();
                    } catch (IOException e) {
                        str = "I/O error or a malformed or unmappable byte sequence is read";
                        e.printStackTrace();
                    }
                } else {
                    str = "filename not specified";
                }
                str += System.lineSeparator();
                channel.write(ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8)));
                break;
            case "":
                break;
            default:
                str = "Unknown command" + System.lineSeparator();
                channel.write(ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8)));
        }
        str = dir.toString() + "-> ";
        channel.write(ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8)));
    }

    private String getLsResultString() throws IOException {
        return Files.list(dir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator();
    }

    private String readMessageFromChannel(SocketChannel channel) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int readCount = channel.read(buffer);
            if (readCount == -1) {
                channel.close();
                break;
            }
            if (readCount == 0) {
                break;
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                sb.append((char) buffer.get());
            }
            buffer.clear();
        }
        return sb.toString();
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        System.out.println("Client accepted...");
        String str = "Welcome in Mike terminal!" + System.lineSeparator() + dir.toString() + "-> ";
        channel.write(ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8)));
    }

    public static void main(String[] args) throws IOException {
        new Terminal();
    }

}
