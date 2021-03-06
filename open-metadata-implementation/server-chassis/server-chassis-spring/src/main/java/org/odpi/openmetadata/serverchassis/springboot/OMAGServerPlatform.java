/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.serverchassis.springboot;

import org.odpi.openmetadata.adminservices.OMAGServerOperationalServices;
import org.odpi.openmetadata.adminservices.rest.SuccessMessageResponse;
import org.odpi.openmetadata.http.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.*;

@SpringBootApplication
@ComponentScan({"org.odpi.openmetadata.*"})
@EnableSwagger2
@Configuration
public class OMAGServerPlatform
{
    @Value("${strict.ssl}")
    Boolean strictSSL;

    @Value("${startup.user}")
    String sysUser;

    @Value("${startup.server.list}")
    String startupServers;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private boolean triggeredRuntimeHalt = false;
    private String startupMessage = "";
    private OMAGServerOperationalServices operationalServices = new OMAGServerOperationalServices();

    private static final Logger log = LoggerFactory.getLogger(OMAGServerPlatform.class);

    public static void main(String[] args) {
        SpringApplication.run(OMAGServerPlatform.class, args);
    }

    @Bean
    public InitializingBean getInitialize()
    {
        return () -> {
            if (!strictSSL)
            {
                log.warn("strict.ssl is set to false! Invalid certificates will be accepted for connection!");
                HttpHelper.noStrictSSL();
            }
            autoStartConfig();
        };
    }

    /**
     * Extract the list of servers to auto start along with the administration userId.
     * The userId is in property "sysUser" and the list of server names are in property
     * "startupServers".  If either are null then no servers are auto started.
     */
    List<String>  getAutoStartList()
    {
        if (!startupServers.trim().isEmpty())
        {
            String[] splits = startupServers.split(",");
            //remove eventual duplicates
            HashSet<String> serverSet = new HashSet<>(Arrays.asList(splits));

            if (! serverSet.isEmpty())
            {
                return new ArrayList<>(serverSet);
            }
        }

        return null;
    }

    /**
     * Starts the servers specified in the startup.server.list property
     */
    private void autoStartConfig()
    {
        List<String>  servers = getAutoStartList();

        if (servers != null)
        {
            log.info("Startup detected for servers: {}", startupServers);
        }

        SuccessMessageResponse response = operationalServices.activateServerListWithStoredConfig(sysUser.trim(), servers);

        if (response.getRelatedHTTPCode() == 200)
        {
            startupMessage = response.getSuccessMessage();
        }
        else
        {
            startupMessage = response.getExceptionErrorMessage();

            StartupFailEvent customSpringEvent = new StartupFailEvent(this, startupMessage);
            applicationEventPublisher.publishEvent(customSpringEvent);
            triggeredRuntimeHalt = true;
        }
    }


    /**
     *  Deactivate all servers that were started automatically
     */
    private void temporaryDeactivateServers()
    {
        List<String>  servers = getAutoStartList();

        if (servers != null)
        {
            log.info("Temporarily deactivating the auto-started servers '{}'", servers.toString());

            System.out.println(new Date().toString() + " OMag Server Platform shutdown requested. Temporarily deactivating the following " +
                                       "auto-started servers: " + servers.toString());

            operationalServices.deactivateTemporarilyServerList(sysUser, servers);
        }
    }


    /**
     * Enable swagger.
     *
     * @return Swagger documentation bean used to show API documentation
     */
    @Bean
    public Docket swaggerDocumentationAPI() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }


    @Component
    public class ApplicationContextListener
    {

        @EventListener
        public void onApplicationEvent(ContextRefreshedEvent event)
        {
            System.out.println();
            System.out.println(OMAGServerPlatform.this.startupMessage);
            if(triggeredRuntimeHalt){
                Runtime.getRuntime().halt(43);
            }
            System.out.println(new Date().toString() + " OMAG server platform ready for more configuration");
        }

        @EventListener
        public void onApplicationEvent(ContextClosedEvent event)
        {
            temporaryDeactivateServers();
        }
    }

    @Component
    public class CustomSpringEventListener implements ApplicationListener<StartupFailEvent>
    {
        @Override
        public void onApplicationEvent(StartupFailEvent event) {
            log.info("Received startup fail event with message: {} " + event.getMessage());
            temporaryDeactivateServers();
        }

    }

}
