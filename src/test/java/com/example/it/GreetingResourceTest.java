package com.example.it;

import com.example.GreetingMessage;
import com.example.GreetingResource;
import com.example.GreetingService;
import com.example.JaxrsActivator;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;


@Testcontainers
@ExtendWith(ArquillianExtension.class)
public class GreetingResourceTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(GreetingResourceTest.class);
    private final static Slf4jLogConsumer consumer = new Slf4jLogConsumer(LOGGER);
    
    @Container
    static WildflyContainer wildfly = new WildflyContainer();//.withLogConsumer(consumer);
    
    @Deployment
    public static WebArchive getDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClass(GreetingMessage.class)
            .addClass(GreetingService.class)
            .addClass(GreetingResource.class)
            .addClass(JaxrsActivator.class)
            // Enable CDI (Optional since Java EE 7.0)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
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
    void should_create_greeting() throws MalformedURLException {
        LOGGER.info(" client: {}, baseURL: {}", client, base);
        final var greetingTarget = this.client.target(new URL(this.base, "api/greeting/JakartaEE").toExternalForm());
        try (final Response greetingGetResponse = greetingTarget.request()
                .accept(MediaType.APPLICATION_JSON)
                .get()) {
            assertThat(greetingGetResponse.getStatus()).isEqualTo(200);
            assertThat(greetingGetResponse.readEntity(GreetingMessage.class).getMessage()).startsWith("Say Hello to JakartaEE");
        }
    }
}
