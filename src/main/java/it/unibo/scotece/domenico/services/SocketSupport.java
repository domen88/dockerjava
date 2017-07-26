package it.unibo.scotece.domenico.services;

import java.io.IOException;

public interface SocketSupport {

    void startServer() throws IOException;
    void startClient(String address) throws Exception;

}
