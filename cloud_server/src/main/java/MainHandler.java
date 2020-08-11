import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MainHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileRequestMessage) {
            FileRequestMessage frm = (FileRequestMessage) msg;
            if (Files.exists(Paths.get("serverFiles/" + frm.getFileName()))) {
                FileSendMessage fsm = new FileSendMessage(Paths.get("serverFiles/" + frm.getFileName()));
                ctx.writeAndFlush(fsm);
            }
        }
        if (msg instanceof FileSendMessage) {
            FileSendMessage fsm = (FileSendMessage) msg;
            Files.write(Paths.get("serverFiles/" + fsm.getFileName()), fsm.getContent(), StandardOpenOption.CREATE);
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
