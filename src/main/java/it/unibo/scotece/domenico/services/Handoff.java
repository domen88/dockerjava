package it.unibo.scotece.domenico.services;

import com.spotify.docker.client.DockerClient;

public interface Handoff {

    void createDataVolumeContainer(DockerClient docker, String baseImage, String containerName);
    void createBackup(DockerClient docker, String volumesFrom);
}
