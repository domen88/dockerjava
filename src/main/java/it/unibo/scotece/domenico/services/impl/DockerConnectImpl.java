package it.unibo.scotece.domenico.services.impl;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import it.unibo.scotece.domenico.services.DockerConnect;

import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DockerConnectImpl implements DockerConnect {

    private DockerClient docker;
    private List<DockerClient> cachedDocker = new ArrayList<>(1);

    public DockerConnectImpl(String... args) throws DockerCertificateException {
        this.docker = connect(args);
        this.cachedDocker.add(this.docker);
    }

    @Override
    public DockerClient connect(String... args) throws DockerCertificateException {
        //MAC environment
        if (args.length == 0){
            //Open the docker client
            final DockerClient docker = DefaultDockerClient.fromEnv().build();
            return docker;
        }

        //UBUNTU
        final String address = args[0];
        final String certPath = args[1];

        final DockerClient docker = DefaultDockerClient.builder()
                .uri(URI.create("https://"+address+":2376"))
                .dockerCertificates(new DockerCertificates(Paths.get(certPath)))
                .build();
        return docker;
    }

    @Override
    public void close() {
        this.cachedDocker.remove(this.docker);
        this.docker.close();
    }

    public DockerClient getConnection() throws DockerCertificateException {

        if (this.cachedDocker.isEmpty()){
            return connect();
        } else {
            return this.cachedDocker.get(0);
        }

    }
}
