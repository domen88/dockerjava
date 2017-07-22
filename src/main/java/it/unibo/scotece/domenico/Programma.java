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

        //Open the docker client
        final DockerClient docker = DefaultDockerClient.fromEnv().build();

        //Pull latest mongo images from docker hub
        docker.pull("mongo:latest");

        //Configuration of Container Data Volume
        final ContainerConfig containerConfig = ContainerConfig.builder()
                .image("mongo")
                .addVolume("/data/db")
                .cmd("/bin/true")
                .build();

        //Create Container Data Volume
        docker.createContainer(containerConfig, "dbdata");

        //Close the docker client
        docker.close();

    }

}
