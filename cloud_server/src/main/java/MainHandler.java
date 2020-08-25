import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

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

public class MainHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileRequestMessage) {
            new Thread(() -> {
                FileRequestMessage frm = (FileRequestMessage) msg;
                if (Files.exists(Paths.get("serverFiles/" + frm.getFileName()))) {
                    File file = new File("serverFiles/" + frm.getFileName());
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
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        if (msg instanceof FileSendMessage) {
           // new Thread(() -> {
                try {

                    FileSendMessage fsm = (FileSendMessage) msg;
                    String fileName = "serverFiles/" + fsm.fileName;
                    Path filePath = Paths.get(fileName);


                    if (fsm.partNumber == 1) {
                        //boolean deleteIfExists = Files.deleteIfExists(filePath);
                        if (Files.deleteIfExists(filePath)){
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
                    e.printStackTrace();
                }

            //}).start();
        }

        if (msg instanceof RequestFilesList) {

                System.out.println("RequestFilesList");
                List<String> cloudFilesList = new ArrayList<>();

                File dir = new File("serverFiles/");
                String[] dirList = dir.list();
                if (dirList != null) {
                    cloudFilesList.addAll(Arrays.asList(dir.list()));
                }
                CloudFilesList cfl = new CloudFilesList(cloudFilesList);
                ctx.writeAndFlush(cfl);
            System.out.println("Список отправлен");

        }

        if (msg instanceof FileDeleteMessage) {
            FileDeleteMessage fdm = (FileDeleteMessage) msg;
            Files.deleteIfExists(Paths.get("serverFiles/" + fdm.getFileName()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
