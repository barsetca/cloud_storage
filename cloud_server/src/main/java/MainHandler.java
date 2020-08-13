import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

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
                            Thread.sleep(1);
                            System.out.println("Send part number " + fsm.partNumber);
                        }

                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        if (msg instanceof FileSendMessage) {
//            FileSendMessage fsm = (FileSendMessage) msg;
//            Files.write(Paths.get("serverFiles/" + fsm.getFileName()), fsm.getContent(), StandardOpenOption.CREATE);
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
