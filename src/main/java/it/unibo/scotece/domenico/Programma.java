package it.unibo.scotece.domenico;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import it.unibo.scotece.domenico.services.impl.DockerConnectImpl;
import it.unibo.scotece.domenico.services.impl.HandoffImpl;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import spark.Spark;

import java.beans.IntrospectionException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;


public class Programma {

    public static void main(String[] args) throws DockerCertificateException, DockerException, InterruptedException {

        //Current docker connector
        DockerConnectImpl currentDockerConnector =  new DockerConnectImpl();
        final StringBuffer address = new StringBuffer();

        //Set Java Spark server port
        port(8080);

        before((request, response) -> response.type("application/json"));

        get("/connect", "application/json", (req, res) -> {

            DockerClient docker = currentDockerConnector.setConnection();
            address.append("localhost");
            return "{\"message\":\"Connect OK\"}";

        });

        get("/connect/:ip", "application/json", (req, res) -> {

            final String ip = req.params(":ip");
            address.append(ip);
            String path = "/Users/domenicoscotece/.docker/openfog";
            path += ip.equals("137.204.57.112") ? "1" : "2";
            DockerClient docker = currentDockerConnector.setConnection(ip, path);
            return "{\"message\":\"Connect OK\"}";

        });

        get("/info", "application/json", (req, res) -> {

            DockerClient docker = currentDockerConnector.getConnection();
            final Info info = docker.info();
            Gson json = new Gson();
            return json.toJson(info);

        });


        get("/createVolume", "application/json", (req, res) -> {

            DockerClient docker = currentDockerConnector.getConnection();

            //Pull latest mongo images from docker hub
            docker.pull("mongo:latest");

            //Remove previous data container volume
            try {
                docker.removeContainer("dbdata");
            } catch (ContainerNotFoundException containerNotFoundException){
                System.out.println("Container not found: dbdata");
            }

            //Configuration of Container Data Volume
            final ContainerConfig containerConfig = ContainerConfig.builder()
                    .image("mongo")
                    .addVolume("/data/db")
                    .cmd("/bin/true")
                    .build();

            //Create Container Data Volume
            ContainerCreation container = docker.createContainer(containerConfig, "dbdata");
            Gson json = new Gson();
            return json.toJson(container);

        });

        get("/handoff", "application/json", (req, res) -> {

            DockerClient docker = currentDockerConnector.getConnection();

            //Open Socket communication
            final String uri = "http://137.204.57.112:8080/socket";
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(uri);
            CloseableHttpResponse response = httpclient.execute(httpGet);
            response.close();

            HandoffImpl handoff = new HandoffImpl();
            handoff.createBackup(docker, "dbdata");
            handoff.sendBackup("localhost", "137.204.57.112");
            return "{\"message\":\"Handoff Completed\"}";

        });

        get("/createHandoff", "application/json", (req, res) -> {

            DockerClient docker = currentDockerConnector.getConnection();

            HandoffImpl handoff = new HandoffImpl();
            handoff.createBackup(docker, "dbdata");

            return "{\"message\":\"Handoff Created\"}";

        });

        get("/sendHandoff", "application/json", (req, res) -> {

            DockerClient docker = currentDockerConnector.getConnection();

            HandoffImpl handoff = new HandoffImpl();
            handoff.sendBackup("localhost", "137.204.57.112");

            return "{\"message\":\"Handoff Sent\"}";

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
