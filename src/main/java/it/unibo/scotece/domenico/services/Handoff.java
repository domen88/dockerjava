package it.unibo.scotece.domenico.services;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;

public interface Handoff {

    void createDataVolumeContainer(DockerClient docker, String baseImage, String containerName);
    void createBackup(DockerClient docker, String volumesFrom) throws DockerException, InterruptedException;
    void sendBackup(String src, String dst) throws Exception;
}
