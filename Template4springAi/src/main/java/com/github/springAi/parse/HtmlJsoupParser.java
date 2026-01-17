package com.github.springAi.parse;

import com.github.springAi.domain.IngestionCandidate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class HtmlJsoupParser implements DocumentParser {

    @Override
    public boolean supports(String mimeType, String extension) {
        return "html".equalsIgnoreCase(extension) || "htm".equalsIgnoreCase(extension);
    }

    @Override
    public IngestionCandidate parse(Resource resource) {
        try {
            Document doc = Jsoup.parse(resource.getInputStream(), StandardCharsets.UTF_8.name(), "");
            
            // Clean up: remove scripts and styles
            doc.select("script, style, nav, footer").remove();
            
            String text = doc.body().text();
            String title = doc.title();

            Map<String, Object> meta = new HashMap<>();
            meta.put("title", title);
            meta.put("source", resource.getFilename());
            meta.put("type", "html");

            return IngestionCandidate.builder()
                    .sourcePath(resource.getURI().toString())
                    .fileName(resource.getFilename())
                    .content(text)
                    .metadata(meta)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse HTML", e);
        }
    }
}