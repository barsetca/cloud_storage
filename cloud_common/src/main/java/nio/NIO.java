package nio;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;

public class NIO {
    public NIO() {
    }

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("./cloud_common/src/main/resources/nioDir");
//        try {
//            if (!Files.exists(path)) {
//                Files.createDirectory(path);
//            }
////            for (Path p : path) {
////                System.out.println(p);
////            }
//            System.out.println(path.getParent());
//            path = Paths.get(path.toString(), "file.txt");
//            if (!Files.exists(path)) {
//                Files.createFile(path);
//            }
////            Files.copy(Paths.get(path.getParent().toString(), "file1.txt"),
////                    path, StandardCopyOption.REPLACE_EXISTING);
//            Files.write(path, "123456789".getBytes(), StandardOpenOption.APPEND);
//            Files.lines(path).forEach(System.out :: println);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        RandomAccessFile raf = new RandomAccessFile("./cloud_common/src/main/resources/serverFiles/user1/2.txt", "rw");
        FileChannel fileChannel = raf.getChannel();
        ByteBuffer buf = ByteBuffer.allocate(48);
        int bytes =  fileChannel.read(buf);
       while(bytes !=-1){
        buf.flip();
        while (buf.hasRemaining()){
            System.out.print((char) buf.get());

        }
        buf.clear();
        bytes =  fileChannel.read(buf);
    }

        raf.seek(34);

        String newData = "New String to write to file...";
//        ByteBuffer buf = ByteBuffer.allocate(48);
        buf.clear();
        buf.put(newData.getBytes());
        buf.flip();
        while(buf.hasRemaining()) {
            fileChannel.write(buf);
        }


        raf.close();



    }



}
