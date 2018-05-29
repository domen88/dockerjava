package it.unibo.scotece.domenico;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import it.unibo.scotece.domenico.services.impl.DockerConnectImpl;
import it.unibo.scotece.domenico.services.impl.HandoffImpl;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import java.time.Duration;
import java.time.Instant;


public class Program {

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
            String path = Conf.dockerCertificatesPath;
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
            final String image = Conf.dockerImageName + ":" + Conf.dockerImageVersion;
            docker.pull(image);

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
            final CloseableHttpClient httpclient = HttpClients.createDefault();

            Instant startHandoff = Instant.now();

            //Open Socket communication
            final String uriSocket = "http://" + Conf.remoteHostIP + ":" + Conf.remoteHostPORT + "/socket";
            HttpGet httpGet = new HttpGet(uriSocket);
            CloseableHttpResponse response = httpclient.execute(httpGet);
            response.close();

            //Handoff procedure on local host
            HandoffImpl handoff = new HandoffImpl();

            //Backup procedure
            Instant start = Instant.now();
            handoff.createBackup(docker, "dbdata");
            Instant stop = Instant.now();
            System.out.println("DURATION: Create Backup Procedure "  + Duration.between(start, stop));

            start = Instant.now();
            handoff.sendBackup("localhost", Conf.remoteHostIP);
            stop = Instant.now();
            System.out.println("DURATION: Transfer Backup Procedure "  + Duration.between(start, stop));

            //Start handoff procedure on remote host
            docker = currentDockerConnector.setConnection(Conf.remoteHostIP, Conf.dockerCertificatesPath);

            start = Instant.now();
            //Mongo Image for RPI: nonoroazoro/rpi-mongo
            handoff.createDataVolumeContainer(docker, "nonoroazoro/rpi-mongo", "dbdata");
            stop = Instant.now();
            System.out.println("DURATION: Create Data Volume Procedure "  + Duration.between(start, stop));

            start = Instant.now();
            handoff.restore(docker,"dbdata");
            stop = Instant.now();
            System.out.println("DURATION: Data Startup Procedure "  + Duration.between(start, stop));

            start = Instant.now();
            handoff.startContainerWithBackup(docker, "nonoroazoro/rpi-mongo", "dbdata", "some-mongo");
            stop = Instant.now();
            System.out.println("DURATION: Service Startup Procedure "  + Duration.between(start, stop));

            //Restore docker connection
            currentDockerConnector.setConnection();

            Instant endHandoff = Instant.now();
            System.out.println("DURATION: All handoff procedure "  + Duration.between(startHandoff, endHandoff));

            return "{\"message\":\"Handoff Completed\"}";

        });

        get("/handoffDump", "application/json", (req, res) -> {

            DockerClient docker = currentDockerConnector.getConnection();
            final CloseableHttpClient httpclient = HttpClients.createDefault();

            Instant startHandoff = Instant.now();

            //Open Socket communication
            final String uriSocket = "http://" + Conf.remoteHostIP + ":" + Conf.remoteHostPORT + "/socket";
            HttpGet httpGet = new HttpGet(uriSocket);
            CloseableHttpResponse response = httpclient.execute(httpGet);
            response.close();

            //Handoff procedure on local host
            HandoffImpl handoff = new HandoffImpl();

            Instant start = Instant.now();
            handoff.createDump(docker,"some-mongo");
            Instant stop = Instant.now();
            System.out.println("DURATION: Create Dump Procedure "  + Duration.between(start, stop));

            start = Instant.now();
            handoff.sendBackup("localhost", Conf.remoteHostIP);
            stop = Instant.now();
            System.out.println("DURATION: Transfer Backup Procedure "  + Duration.between(start, stop));

            //Start handoff procedure on remote host
            docker = currentDockerConnector.setConnection(Conf.remoteHostIP, Conf.dockerCertificatesPath);

            start = Instant.now();
            handoff.startContainerWithBackup(docker, "nonoroazoro/rpi-mongo", "", "some-mongo");
            handoff.restoreDump(docker,"dbdata","some-mongo");
            stop = Instant.now();
            System.out.println("DURATION: Dump Start up Procedure "  + Duration.between(start, stop));

            //Restore docker connection
            currentDockerConnector.setConnection();

            Instant endHandoff = Instant.now();
            System.out.println("DURATION: All handoff procedure "  + Duration.between(startHandoff, endHandoff));

            return "{\"message\":\"Handoff Dump Completed\"}";

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
            handoff.sendBackup("localhost", Conf.remoteHostIP);

            return "{\"message\":\"Handoff Sent\"}";

        });

        get("/createDump", "application/json", (req, res) -> {

            DockerClient docker = currentDockerConnector.getConnection();

            HandoffImpl handoff = new HandoffImpl();
            handoff.createDump(docker,"some-mongo");

            return "{\"message\":\"dump created\"}";

        });

    }

}
