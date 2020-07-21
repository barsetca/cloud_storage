package io;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    DataInputStream is;
    DataOutputStream os;
    ServerSocket server;

    public Server() throws IOException {
        server = new ServerSocket(8189);
        Socket socket = server.accept();
        System.out.println("Client accepted!");
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        String fileName = is.readUTF();
        System.out.println("fileName: " + fileName);
        FileUtility.createDirectory("./cloud_common/server/");
        File file = new File("./cloud_common/server/" + fileName);
        FileUtility.createFile("./cloud_common/server/" + fileName);
        try (OutputStream os = new FileOutputStream(file)) {
            byte[] buffer = new byte[8192];
            while (is.available() > 0) {
                int readBytes = is.read(buffer);
                System.out.println(readBytes);
                os.write(buffer, 0, readBytes);
            }
        }

        System.out.println("File uploaded!");
    }

    public static void main(String[] args) throws IOException {
        new Server();
    }
}
