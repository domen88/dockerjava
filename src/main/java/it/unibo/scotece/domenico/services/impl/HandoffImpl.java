package it.unibo.scotece.domenico.services.impl;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import it.unibo.scotece.domenico.services.ClientSocketSupport;
import it.unibo.scotece.domenico.services.Handoff;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.util.Random;

public class HandoffImpl implements Handoff {

    @Override
    public void createDataVolumeContainer(DockerClient docker, String baseImage, String containerName) {

    }

    @Override
    public void createBackup(DockerClient docker, String volumesFrom) throws DockerException, InterruptedException {

        //Pull latest ubuntu images from docker hub
        docker.pull("busybox:latest");
        final String currentUsersHomeDir = System.getProperty("user.home");

        HostConfig hostConfig = HostConfig.builder()
                .autoRemove(Boolean.TRUE)
                .volumesFrom(volumesFrom)
                .binds(HostConfig.Bind.from(currentUsersHomeDir).to("/backup").build())
                .build();

        //Configuration of Container Data Volume
        final ContainerConfig containerConfig = ContainerConfig.builder()
                .image("busybox")
                .hostConfig(hostConfig)
                .cmd("tar", "cvf", "/backup/backup.tar", "/data/db")
                .build();
        //Create Container Data Volume
        final ContainerCreation container = docker.createContainer(containerConfig, "dbBackup");

        docker.startContainer(container.id());

    }

    @Override
    public void sendBackup(String src, String dst) throws IOException {

        ClientSocketSupportImpl clientSocketSupport = new ClientSocketSupportImpl();

        //Open Socket communication
        final String url = "http://" + dst + ":8080/socket";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");


        clientSocketSupport.createChannel("dst");
        clientSocketSupport.sendFile();




    }

}
