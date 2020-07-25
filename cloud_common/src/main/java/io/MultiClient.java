package io;

import java.io.*;
import java.net.Socket;

public class MultiClient {

        private final String SERVER_ADDR = "localhost";
        private final int SERVER_PORT = 8189;

        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;

        public MultiClient() {
            try {
                openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void openConnection() throws IOException {
            socket = new Socket(SERVER_ADDR, SERVER_PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                receiveMsg(in);
                closeConnection();
                System.out.println("Сервер закрыл соединение.");
                System.exit(0);
            }).start();

            new Thread(() -> {
                sendMsg(out);
                closeConnection();
                System.out.println("Вы закрыли соединение");
                System.exit(0);
            }).start();
        }

        private void receiveMsg(DataInputStream in) {
            while (true) {
                try {
                    String serverMsg = in.readUTF();

                    if ("download".equals(serverMsg)) {
                        String fileName = in.readUTF();
                        String targetDir = in.readUTF();
                        System.out.println("Файл для загрузки: " + fileName);
                        System.out.println("Целевая папка для загрузки: " + targetDir);
                        String dirName = targetDir + "/";
                        File dir = new File(dirName);
                        if (!dir.exists()) {
                            dir.mkdir();
                        }
                        System.out.println("Folder " + dir.getAbsolutePath());
                        File file = new File(dirName + fileName);
                        if (!file.exists()) {
                            file.createNewFile();
                        }

                        int readBuckets = 0;
                        int countBytes = 0;

                        System.out.println("File " + file.getAbsolutePath());
                        try (OutputStream os = new FileOutputStream(file)) {
                            System.out.println("Downloading...");
                            byte[] buffer = new byte[1024];
                            while (in.available() > 0) {
                                int readBytes = in.read(buffer);
                                readBuckets++;
                                countBytes = countBytes + readBytes;
                                if (readBuckets % 1000 == 0){
                                    System.out.print(">");
                                }
                                os.write(buffer, 0, readBytes);
                            }
                        }
                        System.out.println("\nFile downloaded successfully!");
                        System.out.println("Downloaded " + countBytes + " bytes");
                        System.out.println("Insert new command: ");
                        continue;
                    } else if ("exit".equals(serverMsg)) {
                        break;
                    }else {
                        System.out.println(serverMsg);
                    }

                } catch (Exception e) {
                    break;
                }
            }
        }

        private void sendMsg(DataOutputStream out) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                String clientMsg;
                while ((clientMsg = reader.readLine()) != null) {
                    if (clientMsg.trim().isEmpty()) {
                        continue;
                    }
                    if ("upload".equals(clientMsg.trim().toLowerCase())) {
                        System.out.println("Insert full path to the file");
                        String path = reader.readLine().trim();

                        File file = new File(path);
                        if (!file.exists()){
                            System.out.println("File " + file.getAbsolutePath() + " is not exists");
                            continue;
                        }
                        out.writeUTF(clientMsg);
                        InputStream is = new FileInputStream(file);
                        byte[] buffer = new byte[8192];
                        out.writeUTF(file.getName());
                        System.out.print("/...");
                        while (is.available() > 0) {
                            int readBytes = is.read(buffer);
                            out.write(buffer, 0, readBytes);
                        }
                        System.out.println("/");
                        is.close();
                        continue;

                    } else if ("content".equals(clientMsg.trim().toLowerCase())) {
                        out.writeUTF(clientMsg);
                        continue;

                    } else if ("download".equals(clientMsg.trim().toLowerCase())) {
                        out.writeUTF(clientMsg);
                        System.out.println("Введите имя файла");
                        String fileName = reader.readLine().trim();
                        out.writeUTF(fileName);
                        System.out.println("Insert full path to the folder for downloading");
                        String path = reader.readLine().trim();
                        out.writeUTF(path);
                        continue;

                    } else if (clientMsg.equalsIgnoreCase("exit")) {
                        return;
                    } else {
                        System.out.println("Unknown command");
                        continue;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void closeConnection() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();

            }
        }

        public static void main(String[] args) {
            new Thread(MultiClient::new).start();
        }
    }
