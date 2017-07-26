package it.unibo.scotece.domenico.services;

import java.nio.channels.SocketChannel;

public interface ClientSocketSupport {

    SocketChannel createChannel(String address);
    void sendFile();

}
