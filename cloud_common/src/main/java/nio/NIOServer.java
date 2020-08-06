package nio;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                            //key.channel().close();
                            //break;
                        } else if (command == 35) {
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

                            File file = new File(serverFilePath + "/" + fileName);
                            Path filePath = Paths.get(serverFilePath + "/" + fileName);
                            System.out.println(filePath.toString());
                           SocketChannel channel = ((SocketChannel)key.channel());
                            if (file.exists()) {
                                try {
                                    buffer.flip();
                                    channel.write(ByteBuffer.wrap("OK".getBytes()));
                                    buffer.clear();
                                    channel.write(ByteBuffer.wrap("OK2".getBytes()));
                                    buffer.clear();
                                    // System.out.println("OK");
//                                    byte[] bufFile = new byte[1];
//                                    RandomAccessFile raf = new RandomAccessFile(serverFilePath + "/" + fileName, "rw");
//                                    FileChannel inChannel = raf.getChannel();
//                                    buffer = ByteBuffer.allocate(1024);
//                                    long len = file.length();
//                                    System.out.println(len);
//                                    for (long i = 0; i < len; i++) {
//                                        System.out.println(i);
//                                        buffer.clear();
//                                        raf.read(bufFile);
//                                        buffer.put(bufFile);
//                                        buffer.flip();
//                                        while (buffer.hasRemaining()) {
//                                            inChannel.write(buffer);
//                                        }
//                                    }
//                                    raf.close();
                                 // SocketChannel channel = ((SocketChannel) key.channel());
                                    long size = file.length();
                                    channel.write(ByteBuffer.wrap("OK3".getBytes()));
                                    buffer.clear();
                                    buffer = ByteBuffer.allocate(48);
                                    buffer.clear();
                                    buffer.put("OK48".getBytes());
                                    buffer.flip();
                                    while(buffer.hasRemaining()) {
                                        channel.write(buffer);
                                    }
                                    //((SocketChannel)key.channel()).write(buffer);
                                    //channel.write(ByteBuffer.wrap("OK4".getBytes()));
                                    buffer.clear();
                                    System.out.println(size);
//                                    if (file.exists()) {
                                        //channel.write(ByteBuffer.wrap("OK".getBytes()));
                                       // System.out.println("OK");
//                                        ByteBuffer bufferLong = ByteBuffer.allocate(Long.BYTES);
//                                        bufferLong.putLong(size);
//                                        bufferLong.flip();
//                                        channel.write(bufferLong);
                                        //channel.write(ByteBuffer.wrap(Files.readAllBytes(filePath)));

//                                         try( InputStream is = new FileInputStream(file)) {
//                                            byte[] buf = new byte[8192];
//                                            System.out.print("/...");
//                                             channel.write(ByteBuffer.wrap("OK3".getBytes()));
//                                            while (is.available() > 0) {
//                                                int readBytes = is.read(buf);
//                                                //buffer.flip();
//
//
//                                            }
//                                            System.out.println("/");
//                                        }
                                   // channel.write(ByteBuffer.wrap("OK5".getBytes()));
                                    buffer = ByteBuffer.allocate(48);
                                    buffer.clear();
                                    buffer.put("OK6".getBytes());
                                    buffer.flip();
                                    while(buffer.hasRemaining()) {
                                        channel.write(buffer);
                                    }
                                    buffer.clear();

                                    buffer = ByteBuffer.allocate(48);
                                    buffer.clear();
                                    buffer.put("OK26".getBytes());
                                    buffer.flip();
                                    while(buffer.hasRemaining()) {
                                        channel.write(buffer);
                                    }
                                    buffer.clear();

//                                    buffer.put("OK6".getBytes());
//                                    buffer.flip();
//                                    while(buffer.hasRemaining()) {
//                                        channel.write(buffer);
//                                    }
//                                    buffer.clear();

                                    RandomAccessFile raf = new RandomAccessFile(serverFilePath + "/" + fileName, "rw");
                                    FileChannel fileChannel = raf.getChannel();

//                                    channel.write(ByteBuffer.wrap("OK6".getBytes()));
//                                    buffer.clear();
                                    ByteBuffer buf = ByteBuffer.allocate(48);
                                    buf.clear();
                                    int bytes =  fileChannel.read(buf);
                                    while(bytes !=-1){
                                        buf.flip();
                                        while (buf.hasRemaining()){
                                            System.out.print((char) buf.get());
                                            buffer = ByteBuffer.allocate(48);
                                            buffer.clear();
                                            buffer.put("raf12345".getBytes());
                                            buffer.flip();
                                            while(buffer.hasRemaining()) {
                                                channel.write(buffer);
                                            }
                                            buffer.clear();
                                            //channel.write(buf);
                                        }
                                        buf.clear();
                                        bytes =  fileChannel.read(buf);

                                    }

                                    raf.close();
//                                        channel.close();
//                                        byte[] bufFile = new byte[1];
//                                        RandomAccessFile raf = new RandomAccessFile(serverFilePath + "/" + fileName, "rw");
//                                        FileChannel inChannel = raf.getChannel();
//
//                                        SocketChannel channel = ((SocketChannel) key.channel());
//                                        long len = file.length();
//                                        System.out.println(len);
//                                        for (long i = 0; i < len; i++) {
//                                            System.out.println(i);
//                                            buffer.clear();
//                                            buffer = ByteBuffer.allocate(1);
//
//                                            raf.read(bufFile);
//                                          buffer.put(bufFile);
//                                            ((SocketChannel) key.channel()).write(buffer);
//                                            buffer.flip();
//                                            while (buffer.hasRemaining()) {
//                                                channel.write(ByteBuffer.wrap(bufFile));
////                                            }
//                                        }
//                                        buffer.flip();
//                                        System.out.println("File downloaded");
//                                        raf.close();

//                                    } else {
//                                       // channel.write(ByteBuffer.wrap("FALSE".getBytes()));
//                                    }



                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
//                                long len = file.length();
//                                buffer = ByteBuffer.allocate(8);
//                                buffer.clear();
//                                buffer.putLong(len);
//                                buffer.flip();
//                                while (buffer.hasRemaining()) {
//                                    channel.write(buffer);
                            }
                            //channel.write(ByteBuffer.wrap("OK".getBytes()));
                        }

                    }
                }
            }
//        }
    } catch(    Exception e)

    {
        e.printStackTrace();
    }

}

    public static void main(String[] args) throws IOException {
        new Thread(new NIOServer()).start();
    }
}
