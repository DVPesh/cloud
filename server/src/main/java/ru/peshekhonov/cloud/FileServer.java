package ru.peshekhonov.cloud;

import ru.peshekhonov.cloud.network.Server;

public class FileServer {

    private static final int DEFAULT_PORT = 8189;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length != 0) {
            port = Integer.parseInt(args[0]);
        }

        Server.start(port);
    }

}
