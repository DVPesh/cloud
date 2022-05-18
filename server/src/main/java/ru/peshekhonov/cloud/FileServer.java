package ru.peshekhonov.cloud;

import ru.peshekhonov.cloud.network.Server;

public class FileServer {

    public static void main(String[] args) {
        int port = Configuration.SERVER_PORT;
        if (args.length != 0) {
            port = Integer.parseInt(args[0]);
        }

        Server.start(port);
    }
}
