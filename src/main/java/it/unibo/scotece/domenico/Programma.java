package it.unibo.scotece.domenico;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import it.unibo.scotece.domenico.services.impl.DockerConnectImpl;
import spark.Spark;

import java.beans.IntrospectionException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;


public class Programma {

    public static void main(String[] args) throws DockerCertificateException, DockerException, InterruptedException {

        //Current docker connector
        DockerConnectImpl currentDockerConnector =  new DockerConnectImpl();;

        //Set Java Spark server port
        port(8080);

        get("/connect", "application/json", (req, res) -> {

            DockerConnectImpl dockerConnector = new DockerConnectImpl();
            DockerClient docker = dockerConnector.getConnection();
            return "{\"message\":\"Connect OK\"}";

        });

        get("/connect/:ip", "application/json", (req, res) -> {

            final String ip = req.params(":ip");
            String path = "/Users/domenicoscotece/.docker/openfog";
            path += ip.equals("137.204.57.112") ? "1" : "2";
            DockerConnectImpl dockerConnector = new DockerConnectImpl(ip, path);
            DockerClient docker = dockerConnector.getConnection();
            return "{\"message\":\"Connect OK\"}";

        });

        get("/info/:ip", "application/json", (req, res) -> {

            final String ip = req.params(":ip");
            String path = "/Users/domenicoscotece/.docker/openfog";
            path += ip.equals("137.204.57.112") ? "1" : "2";
            DockerConnectImpl dockerConnector = new DockerConnectImpl(ip, path);
            DockerClient docker = dockerConnector.getConnection();
            final Info info = docker.info();
            dockerConnector.close();
            Gson json = new Gson();
            return json.toJson(info);

        });

        get("/info", "application/json", (req, res) -> {

            DockerConnectImpl dockerConnector = new DockerConnectImpl();
            DockerClient docker = dockerConnector.getConnection();
            final Info info = docker.info();
            dockerConnector.close();
            Gson json = new Gson();
            return json.toJson(info);

        });

        //Open the docker client
        //final DockerClient docker = DefaultDockerClient.fromEnv().build();

        //DockerConnectImpl dockerConnector = new DockerConnectImpl("137.204.57.106", "/Users/domenicoscotece/.docker/openfog2");
        //DockerConnectImpl dockerConnector = new DockerConnectImpl();
        //DockerClient docker = dockerConnector.getConnection();

        /*Integer control = Integer.parseInt(args[0]);

        if (control.equals(0)) {

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

        } else if (control.equals(1)){

            //Pull latest ubuntu images from docker hub
            docker.pull("busybox:latest");
            String currentUsersHomeDir = System.getProperty("user.home");

            HostConfig hostConfig = HostConfig.builder()
                    .autoRemove(Boolean.TRUE)
                    .volumesFrom("dbdata")
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
            //docker.stopContainer(container.id(), 5);

        }*/


        //System.out.println(docker.info().toString());
        //Close the docker client
        //dockerConnector.close();

    }

}
