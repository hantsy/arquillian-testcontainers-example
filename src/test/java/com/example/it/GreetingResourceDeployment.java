package com.example.it;

import com.example.GreetingMessage;
import com.example.GreetingResource;
import com.example.GreetingService;
import com.example.JaxrsActivator;
import org.jboss.arquillian.container.test.api.DeploymentConfiguration;
import org.jboss.arquillian.container.test.api.DeploymentConfiguration.DeploymentContentBuilder;
import org.jboss.arquillian.container.test.spi.client.deployment.AutomaticDeployment;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GreetingResourceDeployment implements AutomaticDeployment {
    private final Logger LOGGER = Logger.getLogger(GreetingResourceDeployment.class.getName());
    @Override
    public DeploymentConfiguration generateDeploymentScenario(TestClass testClass) {
        var war = ShrinkWrap.create(WebArchive.class)
                .addClass(GreetingMessage.class)
                .addClass(GreetingService.class)
                .addClass(GreetingResource.class)
                .addClass(JaxrsActivator.class)
                // Enable CDI (Optional since Java EE 7.0)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        LOGGER.log(Level.INFO, "generated deployment files: {}", war.toString(true));

        return new DeploymentContentBuilder(war)
                .withDeployment()
                //.withTestable(false)// client test.
                .build()
                .get();
    }
}