package it.unibo.scotece.domenico.services.impl;

import com.spotify.docker.client.DockerClient;
import it.unibo.scotece.domenico.services.Handoff;

public class HandoffImpl implements Handoff {

    public void createDataVolumeContainer(DockerClient docker, String baseImage, String containerName) {

    }

    public void createBackup(DockerClient docker, String volumesFrom) {

    }


}
