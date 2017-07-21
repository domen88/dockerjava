package it.unibo.scotece.domenico;


import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;

import java.net.URI;
import java.util.List;

public class Programma {

    public static void main(String[] args) throws DockerCertificateException, DockerException, InterruptedException {

        final DockerClient docker = DefaultDockerClient.fromEnv().build();

        final Volume volumeToCreate = Volume.builder()
                .name("dbdata")
                .build();
        final Volume created = docker.createVolume(volumeToCreate);

        final HostConfig hostConfig = HostConfig.builder()
                .binds(HostConfig.Bind.from(created).to("/dbdata").build())
                .build();

        docker.pull("training/postgres");

        final ContainerConfig containerConfig = ContainerConfig.builder()
                .image("training/postgres")
                .tty(true)
                .hostConfig(hostConfig)
                .build();

        docker.createContainer(containerConfig, "cont");

        // Close the docker client
        docker.close();

    }

}
