package com.github.springAi.controller;

import com.github.springAi.domain.RagRequest;
import com.github.springAi.domain.RagResponse;
import com.github.springAi.service.RAGService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rag")
@RequiredArgsConstructor
public class RAGController {

    private final RAGService ragService;

    @PostMapping("/query")
    public RagResponse query(@RequestBody RagRequest request) {
        return ragService.ask(request);
    }
}