package io_solution;

import java.io.*;
import java.net.Socket;

public class FileHandler implements Runnable {

        private String serverFilePath = "./cloud_common/src/main/resources/serverFiles";
        private Socket socket;
        private DataInputStream dis;
        private DataOutputStream dos;
        private boolean isRunning = true;
        private static int cnt = 1;

    public FileHandler(Socket socket) throws IOException {
            this.socket = socket;
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            String userName = "user" + cnt;
            cnt++;
            serverFilePath += "/" + userName;
            File dir = new File(serverFilePath);
            if (!dir.exists()) {
                dir.mkdir();
            }
        }

        @Override
        public void run() {
            while (isRunning) {
                try {
                    String command = dis.readUTF();
                    if (command.equals("./download")) {
                        String fileName = dis.readUTF();
                        System.out.println("find file with name: " + fileName);
                        File file = new File(serverFilePath + "/" + fileName);
                        if (file.exists()) {
                            dos.writeUTF("OK");
                            long len = file.length();
                            dos.writeLong(len);
                            FileInputStream fis = new FileInputStream(file);
                            byte[] buffer = new byte[1024];
                            while (fis.available() > 0) {
                                int count = fis.read(buffer);
                                dos.write(buffer, 0, count);
                                fis.close();
                            }
                        } else {
                            dos.writeUTF("File not exists");
                        }
                    } else  if (command.equals("./upload")) {
                        // TODO: 7/23/2020 upload
                        String fileName = dis.readUTF();
                        System.out.println("find file with name: " + fileName);
                        File file = new File(serverFilePath + "/" + fileName);
                        if (!file.exists()){
                            file.createNewFile();
                        }
                        long len = dis.readLong();
                        byte [] buffer = new byte[1024];
                        try(FileOutputStream fos = new FileOutputStream(file)) {

                            if (len < 1024) {
                                int count = dis.read(buffer);
                                fos.write(buffer, 0, count);
                            } else {
                                for (long i = 0; i <= len / 1024; i++) {
                                    int count = dis.read(buffer);
                                    fos.write(buffer, 0, count);
                                }
                            }
                        }
                        dos.writeUTF("OK");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
}
