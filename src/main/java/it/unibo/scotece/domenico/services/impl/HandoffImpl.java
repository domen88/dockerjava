package it.unibo.scotece.domenico.services.impl;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerExit;
import com.spotify.docker.client.messages.HostConfig;
import it.unibo.scotece.domenico.services.Handoff;
import java.util.concurrent.*;


public class HandoffImpl implements Handoff {

    @Override
    public void createDataVolumeContainer(DockerClient docker, String baseImage, String containerName) throws DockerException, InterruptedException, ExecutionException {

        //Pull latest mongo images from docker hub
        docker.pull(baseImage+":latest");

        //Remove previous data container volume
        try {
            docker.removeContainer(containerName);
        } catch (ContainerNotFoundException containerNotFoundException){
            System.out.println("Container not found: " + containerName);
        }

        //Configuration of Container Data Volume
        final ContainerConfig containerConfig = ContainerConfig.builder()
                .image(baseImage)
                .addVolume("/data/db")
                .cmd("/bin/true")
                .build();


        //Create Container Data Volume
        docker.createContainer(containerConfig, containerName);

        /*Callable<ContainerCreation> task = () -> {
            try {
                return docker.createContainer(containerConfig, containerName);
            }
            catch (InterruptedException e) {
                throw new IllegalStateException("task interrupted", e);
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<ContainerCreation> future = executor.submit(task);

        ContainerCreation result = future.get();*/

    }

    @Override
    public void createBackup(DockerClient docker, String volumesFrom) throws DockerException, InterruptedException, ExecutionException {

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

        Callable<ContainerExit> task = () -> {
            try {
                return docker.waitContainer(container.id());
            }
            catch (InterruptedException e) {
                throw new IllegalStateException("task interrupted", e);
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<ContainerExit> future = executor.submit(task);

        ContainerExit result = future.get();

        System.out.println(result.statusCode());

    }

    @Override
    public void sendBackup(String src, String dst) throws Exception {

        ClientSocketSupportImpl clientSocketSupport = new ClientSocketSupportImpl();
        clientSocketSupport.startClient(dst);

    }

    @Override
    public void restore(DockerClient docker, String volumesFrom) throws DockerException, InterruptedException, ExecutionException {

        //Pull latest ubuntu images from docker hub
        docker.pull("busybox:latest");
        final String currentUsersHomeDir = "/home/dscotece";

        HostConfig hostConfig = HostConfig.builder()
                .autoRemove(Boolean.TRUE)
                .volumesFrom(volumesFrom)
                .binds(HostConfig.Bind.from(currentUsersHomeDir).to("/backup").build())
                .build();

        //Configuration of Container Data Volume
        final ContainerConfig containerConfig = ContainerConfig.builder()
                .image("busybox")
                .hostConfig(hostConfig)
                .cmd("tar", "xvf", "/backup/backup.tar")
                .build();

        //Create Container Data Volume
        final ContainerCreation container = docker.createContainer(containerConfig, "dbBackup");

        docker.startContainer(container.id());

        Callable<ContainerExit> task = () -> {
            try {
                return docker.waitContainer(container.id());
            }
            catch (InterruptedException e) {
                throw new IllegalStateException("task interrupted", e);
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<ContainerExit> future = executor.submit(task);

        ContainerExit result = future.get();

        System.out.println(result.statusCode());

    }

    @Override
    public void startContainerWithBackup(DockerClient docker, String baseImage, String volumesFrom, String containerName) throws DockerException, InterruptedException {

        //Pull latest mongo images from docker hub
        docker.pull(baseImage+":latest");

        HostConfig hostConfig = HostConfig.builder()
                .volumesFrom(volumesFrom)
                .build();

        //Configuration of Container Data Volume
        final ContainerConfig containerConfig = ContainerConfig.builder()
                .image(baseImage)
                .hostConfig(hostConfig)
                .build();

        //Create Container Data Volume
        ContainerCreation container = docker.createContainer(containerConfig, containerName);
        docker.startContainer(container.id());

    }

}
