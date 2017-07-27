package it.unibo.scotece.domenico.services;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.concurrent.ExecutionException;

public interface Handoff {

    void createDataVolumeContainer(DockerClient docker, String baseImage, String containerName) throws DockerException, InterruptedException, ExecutionException;
    void createBackup(DockerClient docker, String volumesFrom) throws DockerException, InterruptedException, ExecutionException;
    void sendBackup(String src, String dst) throws Exception;
    void restore(DockerClient docker, String volumesFrom) throws DockerException, InterruptedException, ExecutionException;
    void startContainerWithBackup(DockerClient docker, String baseImage, String volumesFrom, String containerName) throws DockerException, InterruptedException;
}
