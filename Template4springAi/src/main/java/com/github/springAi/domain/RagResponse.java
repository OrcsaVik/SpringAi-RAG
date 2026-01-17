package com.github.springAi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RagResponse {
    private String answer;
    private List<SourceDocument> sources;
}