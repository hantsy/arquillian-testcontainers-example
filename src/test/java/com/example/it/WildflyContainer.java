package com.example.it;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

public class WildflyContainer extends GenericContainer<WildflyContainer> {
    
    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("quay.io/wildfly/wildfly");
    public static final int HTTP_PORT = 8080;
    public static final int ADMIN_PORT = 9990;
    public static final String ADMIN_USER = "admin";
    public static final String ADMIN_PASS = "Admin@123";
    
    public WildflyContainer() {
        this(DEFAULT_IMAGE_NAME);
    }
    
    public WildflyContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);
        
        withExposedPorts(HTTP_PORT, ADMIN_PORT);
        withCommand("/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0");
    }
    
    @Override
    protected void containerIsStarting(InspectContainerResponse containerInfo) {
        super.containerIsStarting(containerInfo);
        
        // Set the container address and mapped host ports here, to be picked up by Arquillian container definition
        System.setProperty("tc.wildfly.host", this.getHost());
        System.setProperty("tc.wildfly.admin.port", this.getMappedPort(ADMIN_PORT)+"");
        System.setProperty("tc.wildfly.http.port", this.getMappedPort(HTTP_PORT)+"");
        
        //Transfer admin user and password, too
        System.setProperty("tc.wildfly.admin.user", ADMIN_USER);
        System.setProperty("tc.wildfly.admin.pass", ADMIN_PASS);
    }
    
    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        super.containerIsStarted(containerInfo);
        
        // execute a command to create the admin user
        try {
            var output = this.execInContainer("/opt/jboss/wildfly/bin/add-user.sh", ADMIN_USER, ADMIN_PASS).getStdout();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
