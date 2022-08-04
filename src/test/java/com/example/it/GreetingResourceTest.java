package com.example.it;

import com.example.GreetingMessage;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(ArquillianExtension.class)
@Testcontainers
public class GreetingResourceTest {
    private final static Logger LOGGER = Logger.getLogger(GreetingResourceTest.class.getName());

    private final static DockerImageName IMAGE_NAME = DockerImageName
            .parse("quay.io/wildfly/wildfly")
            .asCompatibleSubstituteFor("jboss/wildfly");
    @Container
    static GenericContainer wildfly = new GenericContainer<>(IMAGE_NAME)
            .withExposedPorts(8080, 9990)
            .withCreateContainerCmdModifier(cmd -> {
                var createAdmin = cmd.withCmd("/opt/jboss/wildfly/bin/add-user.sh", "admin", "Admin@123", "--silent").exec().getRawValues();
                LOGGER.log(Level.INFO, "Creating admin user in WildFly container: {0}", createAdmin);
            })
            .withCommand("/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0");

    @BeforeDeployment
    public static Archive beforeDeployment(Archive archive) {
        Wait.forListeningPort().waitUntilReady(wildfly);
        LOGGER.log(Level.INFO, "deployment files: {}", archive.toString(true));
        return archive;
    }

    @ArquillianResource
    private URL base;

    private Client client;

    @BeforeEach
    public void setup() {
        LOGGER.info("call BeforeEach");
        this.client = ClientBuilder.newClient();
        //removed the Jackson json provider registry, due to OpenLiberty 21.0.0.1 switched to use Resteasy.
    }

    @AfterEach
    public void teardown() {
        LOGGER.info("call AfterEach");
        if (this.client != null) {
            this.client.close();
        }
    }

    @Test
    @RunAsClient
    @DisplayName("Given a name:`JakartaEE` should return `Say Hello to JakartaEE`")
    public void should_create_greeting() throws MalformedURLException {
        LOGGER.log(Level.INFO, " client: {0}, baseURL: {1}", new Object[]{client, base});
        final var greetingTarget = this.client.target(new URL(this.base, "api/greeting/JakartaEE").toExternalForm());
        try (final Response greetingGetResponse = greetingTarget.request()
                .accept(MediaType.APPLICATION_JSON)
                .get()) {
            assertThat(greetingGetResponse.getStatus()).isEqualTo(200);
            assertThat(greetingGetResponse.readEntity(GreetingMessage.class).getMessage()).startsWith("Say Hello to JakartaEE");
        }
    }
}
