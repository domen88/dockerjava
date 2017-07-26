package it.unibo.scotece.domenico.services.impl;

import com.google.gson.Gson;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import it.unibo.scotece.domenico.services.Handoff;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.*;


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
    public void sendBackup(String src, String dst) throws Exception {

        ClientSocketSupportImpl clientSocketSupport = new ClientSocketSupportImpl();
        clientSocketSupport.startClient(dst);

    }

}
