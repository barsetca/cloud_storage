package nio;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOServer implements Runnable {
    private ServerSocketChannel server;
    private Selector selector;
    private String serverFilePath = "./cloud_common/src/main/resources/serverFiles";
    private static int cnt = 1;

    public NIOServer() throws IOException {
        server = ServerSocketChannel.open();
        server.socket().bind(new InetSocketAddress(8189));
        server.configureBlocking(false);
        selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {
        try {
            System.out.println("server started");
            while (server.isOpen()) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        System.out.println("client accepted");
                        String userName = "user" + cnt;
                        cnt++;
                        serverFilePath += "/" + userName;
                        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ);
                        channel.write(ByteBuffer.wrap("Hello!".getBytes()));
                    }
                    if (key.isReadable()) {
                        // TODO: 7/23/2020 fileStorage handle
                        System.out.println("read key");
                        ByteBuffer buffer = ByteBuffer.allocate(1);
                        int count = ((SocketChannel) key.channel()).read(buffer);
                        if (count == -1) {
                            key.channel().close();
                            break;
                        }

                        buffer.flip();
                        byte command = buffer.get();
                        if (command == 25) {


                        buffer = ByteBuffer.allocate(4);
                       ((SocketChannel) key.channel()).read(buffer);
                        buffer.flip();
                        int nameInt = buffer.getInt();
                        System.out.println(nameInt);

                        buffer = ByteBuffer.allocate(nameInt);
                        ((SocketChannel) key.channel()).read(buffer);
                        buffer.flip();


                        StringBuilder s = new StringBuilder();
                        while (buffer.hasRemaining()) {
                            s.append((char) buffer.get());
                        }
                        String fileName = s.toString();
                        System.out.println(fileName);

                        buffer = ByteBuffer.allocate(8);
                        ((SocketChannel) key.channel()).read(buffer);
                        buffer.flip();
                        long fileLength = buffer.getLong();
                        System.out.println(fileLength);

                        File dir = new File(serverFilePath);
                            if (!dir.exists()) {
                                dir.mkdir();
                            }

                        File file = new File(serverFilePath + "/" + fileName);
                        if (!file.exists()) {
                            file.createNewFile();
                        }

                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

                        for (int i = 0; i < fileLength; i++) {
                            buffer = ByteBuffer.allocate(1);
                            ((SocketChannel) key.channel()).read(buffer);
                            buffer.flip();
                            byte byteFile = buffer.get();
                            bos.write(byteFile);
                        }

                        System.out.println("File upload");
                        bos.close();
                        key.channel().close();
                        break;
                    }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new Thread(new NIOServer()).start();
    }
}
