import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import messages.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MainHandler extends ChannelInboundHandlerAdapter {

    public static ConcurrentHashMap<String, String> userStorage = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> activeUserStorage = new ConcurrentHashMap<>();
    private String userName;
    private static final String SERVER_DIR = "serverFiles/";

    static {
        userStorage.put("login1", "password1");
        userStorage.put("login2", "password2");
        userStorage.put("login3", "password3");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof AuthRegMessage) {
            AuthRegMessage arm = (AuthRegMessage) msg;
            String login = arm.getLogin();
            String password = arm.getPassword();
            String storagePass = userStorage.get(login);
            System.out.println(arm.getLogin());
            if (arm.isNew()) {
                if (storagePass == null) {
                    userStorage.put(login, password);
                    openAccess(ctx, arm);
                } else {
                    closeAccess(ctx, "The login is already exist. Try again!");
                }
            } else {
                if (storagePass != null) {
                    if (activeUserStorage.get(arm.getLogin()) != null) {
                        String message = "User with login: " + arm.getLogin() + " is already connected!";
                        System.out.println(message);
                        closeAccess(ctx, message);
                    } else if (storagePass.equalsIgnoreCase(password)) {
                        openAccess(ctx, arm);
                    } else {
                        closeAccess(ctx, "Failed password. Try again!");
                    }

                } else {
                    closeAccess(ctx, "The login is not exist. Try again!");
                }
            }
            System.out.println(userName);
        }

        if (msg instanceof FileRequestMessage) {
            new Thread(() -> {
                FileRequestMessage frm = (FileRequestMessage) msg;
                if (Files.exists(Paths.get(SERVER_DIR + userName + "/" + frm.getFileName()))) {
                    File file = new File(SERVER_DIR + userName + "/" + frm.getFileName());
                    int bufSize = 1024 * 1024 * 10;
                    int partsCount = new Long(file.length() / bufSize).intValue();

                    System.out.println(file.length());
                    if (file.length() % bufSize != 0) {
                        partsCount++;
                    }
                    FileSendMessage fsm = new FileSendMessage(file.getName(), -1, partsCount, new byte[bufSize]);
                    try (FileInputStream fis = new FileInputStream(file)) {
                        for (int i = 0; i < partsCount; i++) {
                            int read = fis.read(fsm.partContent);
                            fsm.partNumber = i + 1;

                            if (read < bufSize) {
                                fsm.partContent = Arrays.copyOfRange(fsm.partContent, 0, read);
                            }
                            ctx.writeAndFlush(fsm);
                            Thread.sleep(10);
                            System.out.println("Send part number " + fsm.partNumber);
                        }

                    } catch (IOException | InterruptedException e) {
                        closeConnection(ctx, e.getMessage());
                    }
                }
            }).start();
        }
        if (msg instanceof FileSendMessage) {
            try {
                FileSendMessage fsm = (FileSendMessage) msg;
                String fileName = SERVER_DIR + userName + "/" + fsm.fileName;
                Path filePath = Paths.get(fileName);


                if (fsm.partNumber == 1) {
                    if (Files.deleteIfExists(filePath)) {
                        System.out.println("Удалили существующий файл");
                    }

                    Files.write(filePath, fsm.partContent, StandardOpenOption.CREATE);
                    System.out.println("Uploaded " + fsm.partNumber + "/" + fsm.partsCount);
                }
                if (fsm.partNumber > 1) {
                    System.out.println("Uploaded " + fsm.partNumber + "/" + fsm.partsCount);
                    Files.write(filePath, fsm.partContent, StandardOpenOption.APPEND);
                }
                if (fsm.partNumber == fsm.partsCount) {
                    System.out.println("File " + fsm.fileName + " uploaded!");
                    System.out.println(new File(fileName).length());
                    ctx.flush();

                }

            } catch (IOException e) {
                closeConnection(ctx, e.getMessage());
            }
        }

        if (msg instanceof FilesListRequest) {

            System.out.println("RequestFilesList");
            List<String> cloudFilesList = new ArrayList<>();

            File dir = new File(SERVER_DIR + userName + "/");
            String[] dirList = dir.list();
            if (dirList != null) {
                cloudFilesList.addAll(Arrays.asList(dirList));
            }
            CloudFilesList cfl = new CloudFilesList(cloudFilesList);
            ctx.writeAndFlush(cfl);
            System.out.println("Список отправлен");

        }

        if (msg instanceof FileDeleteMessage) {
            FileDeleteMessage fdm = (FileDeleteMessage) msg;
            Files.deleteIfExists(Paths.get(SERVER_DIR + userName + "/" + fdm.getFileName()));
        }
    }

    private void closeAccess(ChannelHandlerContext ctx, String s) {
        ServerOkMessage som = new ServerOkMessage(s, false);
        ctx.writeAndFlush(som);
    }

    private void openAccess(ChannelHandlerContext ctx, AuthRegMessage arm) {
        activeUserStorage.put(arm.getLogin(), arm.getPassword());
        ServerOkMessage som = new ServerOkMessage("Successful", true);
        userName = arm.getLogin();
        createUserFolder(userName);
        ctx.writeAndFlush(som);
    }

    private void createUserFolder(String userName) {
        Path clientFilesPath = Paths.get(SERVER_DIR + userName + "/");
        try {
            if (!Files.exists(clientFilesPath)) {
                Files.createDirectory(clientFilesPath);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        closeConnection(ctx, cause.getMessage());
    }

    private void closeConnection(ChannelHandlerContext ctx, String msg) {
        System.out.println(msg);
        System.out.println("Клиент " + userName + " отключился");
        if (userName != null) {
            activeUserStorage.remove(userName);
            ctx.close();
        }

    }

}
