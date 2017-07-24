package it.unibo.scotece.domenico.services;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;

public interface DockerConnect {

    DockerClient connect(String... args) throws DockerCertificateException;
    void close();

}
