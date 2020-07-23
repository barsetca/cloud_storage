package io;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MultiServer {

        public static void main(String[] args) {

            try (ServerSocket serverSocket = new ServerSocket(8189)) {
                System.out.println("Сервер запущен, ожидаем подключения ...");
                System.out.println("Для разрыва всех подключений и выхода из прогпраммы программы введите exit");

// поток на получение команд серверу от администратора (пока только exit)
                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                        String serverMsg;
                        while ((serverMsg = reader.readLine()) != null) {
                            if (serverMsg.trim().isEmpty()) {
                                continue;
                            }
                            if (serverMsg.equalsIgnoreCase("exit")) {
                                serverSocket.close();
                                System.out.println("Сервер завершил работу. Для возобновления работы перезапустите сервер.");
                                System.exit(0);
                            } else {
                                System.out.println("Некорректная команда");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();

                while (true) {
                    Socket socket = serverSocket.accept();
                    new Thread(() -> {
                        String name = Thread.currentThread().getName();
// поток на получение сообщений от клиента
                        new Thread(() -> {
                            try {
                                System.out.println("Клиент: " + Thread.currentThread().getName() + " подключился");
                                DataInputStream in = new DataInputStream(socket.getInputStream());
                                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                                while (true) {
                                    try {
                                        String clientMsg = in.readUTF();
                                        if ("upload".equals(clientMsg)) {
                                            String fileName = in.readUTF();
                                            System.out.println("fileName: " + fileName);
                                            String dirName = "./cloud_common/server/" + Thread.currentThread().getName() + "/";
                                            FileUtility.createDirectory(dirName);
                                            File file = new File(dirName + fileName);
                                            FileUtility.createFile(dirName + fileName);
                                            int count = 0;
                                            System.out.println("Downloading the file to the server ...");
                                            try (OutputStream os = new FileOutputStream(file)) {
                                                byte[] buffer = new byte[8192];
                                                while (in.available() > 0) {
                                                    int readBytes = in.read(buffer);
                                                    count = count + readBytes;
                                                    os.write(buffer, 0, readBytes);
                                                }
                                            }
                                            out.writeUTF("File uploaded! \nInsert new command:");
                                            System.out.println("File uploaded! Uploaded " + count + " bytes");
                                            continue;
                                        }
                                        if ("content".equals(clientMsg)) {

                                            File folder = new File("./cloud_common/server/" + Thread.currentThread().getName() + "/");
                                            File[] files = folder.listFiles();
                                            if (files == null) {
                                                out.writeUTF("Content not exist");
                                            } else {
                                                for (File file : folder.listFiles()) {
                                                    out.writeUTF(file.getName());
                                                }

                                                System.out.println("Content sent!");
                                                continue;
                                            }
                                        }
                                        if ("download".equals(clientMsg)) {

                                            String downloadFileName = in.readUTF();
                                            String targetDir = in.readUTF();
                                            Path pathFileName = Paths.get(downloadFileName);
                                            String fileName = pathFileName.getFileName().toString();
                                            System.out.println(fileName);

                                            File file = new File("./cloud_common/server/" + Thread.currentThread().getName() + "/" + fileName);
                                            if (!file.exists()) {
                                                out.writeUTF("File " + fileName + "is not exists");
                                                continue;

                                            } else {
                                                out.writeUTF(clientMsg);
                                                out.writeUTF(fileName);
                                                out.writeUTF(targetDir);
                                                InputStream is = new FileInputStream(file);
                                                byte[] buffer = new byte[8192];
                                                System.out.print("/");
                                                while (is.available() > 0) {
                                                    int readBytes = is.read(buffer);
                                                    out.write(buffer, 0, readBytes);
                                                }
                                                System.out.println("/");
                                                is.close();
                                                continue;

                                            }

                                        }
                                    } catch (Exception e) {
                                        break;
                                    }
                                }
                                socket.close();
                                System.out.println(Thread.currentThread().getName() + " left the service");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }, name).start();
// поток на передачу сообщений от сервера
                        new Thread(() -> {
                            try {
                                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                                out.writeUTF("Welcome to the cloud service!");
                                out.writeUTF("Commands:\n" +
                                        "exit - leave the service\n" +
                                        "upload - upload file to the server\n" +
                                        "download - download file from the cloud\n" +
                                        "content - get list of server files\n" +
                                        "Insert command:");

                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        }, name).start();
                    }).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

