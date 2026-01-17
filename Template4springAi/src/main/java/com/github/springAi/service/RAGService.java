package com.github.springAi.service;

import com.github.springAi.Retriever.VectorStoreRetriever;
import com.github.springAi.domain.DocumentSegment;
import com.github.springAi.domain.RagRequest;
import com.github.springAi.domain.RagResponse;
import com.github.springAi.domain.SourceDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RAGService {

    // A token budget for the context to avoid exceeding the LLM's limit.
    // This is a rough character count; a proper tokenizer would be more accurate.
    private static final int CONTEXT_TOKEN_BUDGET = 4000; 

    private final VectorStoreRetriever retriever;
    private final ChatClient chatClient; // From Spring AI (e.g., OpenAiChatClient)

    @Value("classpath:/prompts/rag-prompt.st")
    private Resource ragPromptResource;

    public RagResponse ask(RagRequest request) {
        // 1. Retrieve relevant documents
        List<DocumentSegment> relevantSegments = retriever.retrieve(
                request.getQuestion(), request.getTopK(), request.getFilters());

        // 2. Build the context string, managing the token budget
        String context = buildContext(relevantSegments);

        // 3. Create the prompt using the template
        PromptTemplate promptTemplate = new PromptTemplate(ragPromptResource);
        Prompt prompt = promptTemplate.create(Map.of(
                "context", context,
                "question", request.getQuestion()
        ));

        // 4. Call the LLM
        String llmAnswer = chatClient.call(prompt).getResult().getOutput().getContent();

        // 5. Create the response object with sources
        List<SourceDocument> sources = relevantSegments.stream()
                .map(seg -> new SourceDocument(seg.getTalkId(), seg.getId(), seg.getContent(), seg.getMetadata()))
                .collect(Collectors.toList());

        return new RagResponse(llmAnswer, sources);
    }
    
    private String buildContext(List<DocumentSegment> segments) {
        StringBuilder contextBuilder = new StringBuilder();
        int currentLength = 0;

        for (int i = 0; i < segments.size(); i++) {
            DocumentSegment segment = segments.get(i);
            String content = segment.getContent();
            
            // Check if adding this segment exceeds the budget
            if (currentLength + content.length() > CONTEXT_TOKEN_BUDGET) {
                break; // Stop adding more segments
            }
            
            contextBuilder.append(String.format("[Source %d, file: %s]:\n", i + 1, segment.getMetadata().get("file_name")));
            contextBuilder.append(content);
            contextBuilder.append("\n---\n");
            currentLength += content.length();
        }
        return contextBuilder.toString();
    }
}