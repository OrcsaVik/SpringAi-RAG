package com.github.springAi.parse;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;

@ShellComponent
@RequiredArgsConstructor
public class IngestCommands {

    private final IngestionService ingestionService;

    @ShellMethod(key = "ingest", value = "Ingest a file into the vector store")
    public String ingest(@ShellOption(help = "Path to the file") String path) {
        File file = new File(path);
        if (!file.exists()) {
            return "File not found: " + path;
        }
        
        try {
            ingestionService.ingestFile(file);
            return "Successfully ingested: " + file.getName();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}