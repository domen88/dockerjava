package it.unibo.scotece.domenico.services.impl;

import it.unibo.scotece.domenico.services.ClientSocketSupport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class ClientSocketSupportImpl implements ClientSocketSupport {

    private SocketChannel socketChannel;

    @Override
    public SocketChannel createChannel(String address) {
        try {
            this.socketChannel = SocketChannel.open();
            SocketAddress socketAddress = new InetSocketAddress(address, 9999);
            this.socketChannel.connect(socketAddress);
            System.out.println("Connected... Now sending the file");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.socketChannel;
    }

    @Override
    public void sendFile() {

        final String backup = System.getProperty("user.home") + "/backup/backup.tar";
        final RandomAccessFile randomAccessFile;

        try{
            final File file = new File(backup);
            randomAccessFile = new RandomAccessFile(file, "r");

            FileChannel inChannel = randomAccessFile.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (inChannel.read(buffer) > 0) {
                buffer.flip();
                this.socketChannel.write(buffer);
                buffer.clear();
            }

            Thread.sleep(1000);
            System.out.println("End of file reached..");
            this.socketChannel.close();
            randomAccessFile.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
