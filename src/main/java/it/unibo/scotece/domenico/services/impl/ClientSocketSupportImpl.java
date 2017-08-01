package it.unibo.scotece.domenico.services.impl;

import it.unibo.scotece.domenico.services.SocketSupport;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public class ClientSocketSupportImpl implements SocketSupport {

    public static final int BUFFER_SIZE = 1024 * 50;
    private byte[] buffer;

    public ClientSocketSupportImpl(){
        this.buffer = new byte[BUFFER_SIZE];
    }

    @Override
    public void startServer() throws IOException {
        ServerSocket socket = new ServerSocket(9000);
        Socket client = socket.accept();
        final String backup = System.getProperty("user.home") + "/backup.tar.gz";

        BufferedInputStream in =
                new BufferedInputStream(client.getInputStream());

        BufferedOutputStream out =
                new BufferedOutputStream(new FileOutputStream(backup));

        int len = 0;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
            System.out.print("#");
        }

        in.close();
        out.flush();
        out.close();
        client.close();
        socket.close();
        System.out.println("\nDone!");
    }

    @Override
    public void startClient(String address) throws Exception {
        Socket socket = new Socket(address, 9000);
        final String backup = System.getProperty("user.home") + "/backup.tar.gz";

        BufferedInputStream in =
                new BufferedInputStream(
                        new FileInputStream(backup));

        BufferedOutputStream out =
                new BufferedOutputStream(socket.getOutputStream());

        int len = 0;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
            System.out.print("#");
        }
        in.close();
        out.flush();
        out.close();
        socket.close();
        System.out.println("\nDone!");
    }
}
