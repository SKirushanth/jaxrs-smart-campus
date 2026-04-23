package com.smartcampus;

import java.io.IOException;
import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import com.smartcampus.config.AppConfig;

public final class Main {
    private static final URI BASE_URI = URI.create("http://0.0.0.0:8080/api/v1/");

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, new AppConfig(), false);
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));
        server.start();

        System.out.println("Smart Campus API is running at: " + BASE_URI);
        System.out.println("Press ENTER to stop the server.");
        System.in.read();

        server.shutdownNow();
    }
}
