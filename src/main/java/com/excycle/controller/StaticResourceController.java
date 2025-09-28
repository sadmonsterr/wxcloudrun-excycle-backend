package com.excycle.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class StaticResourceController {

    @GetMapping(value = "/assets/{filename:.+}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> serveAsset(@PathVariable String filename) throws IOException {
        Resource resource = new ClassPathResource("static/assets/" + filename);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        Path path = Paths.get(resource.getURI());
        byte[] content = Files.readAllBytes(path);

        String contentType = determineContentType(filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(content);
    }

    @GetMapping(value = "/{filename:.+\\.css}", produces = "text/css")
    public ResponseEntity<byte[]> serveCss(@PathVariable String filename) throws IOException {
        Resource resource = new ClassPathResource("static/" + filename);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        Path path = Paths.get(resource.getURI());
        byte[] content = Files.readAllBytes(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/css")
                .body(content);
    }

    @GetMapping(value = "/{filename:.+\\.js}", produces = "application/javascript")
    public ResponseEntity<byte[]> serveJs(@PathVariable String filename) throws IOException {
        Resource resource = new ClassPathResource("static/" + filename);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        Path path = Paths.get(resource.getURI());
        byte[] content = Files.readAllBytes(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/javascript")
                .body(content);
    }

    private String determineContentType(String filename) {
        if (filename.endsWith(".css")) {
            return "text/css";
        } else if (filename.endsWith(".js") || filename.endsWith(".mjs")) {
            return "application/javascript";
        } else if (filename.endsWith(".png")) {
            return "image/png";
        } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filename.endsWith(".gif")) {
            return "image/gif";
        } else if (filename.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (filename.endsWith(".woff2")) {
            return "font/woff2";
        } else if (filename.endsWith(".woff")) {
            return "font/woff";
        } else if (filename.endsWith(".ttf")) {
            return "application/x-font-ttf";
        } else if (filename.endsWith(".eot")) {
            return "application/vnd.ms-fontobject";
        } else if (filename.endsWith(".json")) {
            return "application/json";
        } else {
            return "application/octet-stream";
        }
    }
}