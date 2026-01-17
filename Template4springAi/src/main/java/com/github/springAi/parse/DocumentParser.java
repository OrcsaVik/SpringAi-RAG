package com.github.springAi.parse;

import com.github.springAi.domain.IngestionCandidate;
import org.springframework.core.io.Resource;
public interface DocumentParser {
    boolean supports(String mimeType, String extension);
    IngestionCandidate parse(Resource resource);
}