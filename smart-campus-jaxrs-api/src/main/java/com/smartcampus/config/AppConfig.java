package com.smartcampus.config;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class AppConfig extends ResourceConfig {
    public AppConfig() {
        register(JacksonFeature.class);
        packages("com.smartcampus.resource", "com.smartcampus.mapper", "com.smartcampus.filter");
    }
}
