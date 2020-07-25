package io_solution;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerIo {
    private boolean isRunning = true;

    public void stop() {
        isRunning = false;
    }

    public ServerIo() {
        try(ServerSocket server = new ServerSocket(8189)) {
            System.out.println("server started!");
            while (isRunning) {
                Socket conn = server.accept();
                System.out.println("client accepted!");
                new Thread(new FileHandler(conn)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ServerIo();
    }
}
