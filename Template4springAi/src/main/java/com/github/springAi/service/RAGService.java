package com.github.springAi.service;

import com.github.springAi.Retriever.VectorStoreRetriever;
import com.github.springAi.config.RagServiceProperties;
import com.github.springAi.domain.DocumentSegment;
import com.github.springAi.domain.RagRequest;
import com.github.springAi.domain.RagResponse;
import com.github.springAi.domain.SourceDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RAGService {

    // A token budget for the context to avoid exceeding the LLM's limit.
    // This is a rough character count; a proper tokenizer would be more accurate.
    private static final int CONTEXT_TOKEN_BUDGET = 4000;
    private final ChatModel chatModel; // <-- DEPENDENCY CHANGE
    private final RagServiceProperties properties; // Inject properties for token budget
    private final VectorStoreRetriever retriever;
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

        // 4. Call the LLM using the ChatModel
        // The call is identical to ChatClient when using a Prompt object.
        String llmAnswer = chatModel.call(prompt).getResult().getOutput().getContent();
        // 5. Create the response object with sources
        List<SourceDocument> sources = relevantSegments.stream()
                .map(seg -> new SourceDocument(seg.getDocumentId(), seg.getId(), seg.getContent(), seg.getMetadata()))
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


    public int[] countBits(int n) {
        int[] res = new int[n + 1];

        res[0] = 0;
        for(int i = 0; i <= n; i++){
            if(i % 2 == 1){
                res[i] = res[i - 1] + 1;
            }else{
                res[i] = res[i / 2];
            }
        }
        return res;
    }


    /**
     * TODO donot notice no-relevant code
     * just on the free time
     * @param tasks
     * @param n
     * @return
     */
    public int leastInterval(char[] tasks, int n) {

        int[] count = new int[26];
        for(int i = 0; i < tasks.length; i++){
            count[tasks[i] - 'A']++;
        }

        Arrays.sort(count);

        int maxTime= count[25];

        int maxCount = 0;
        for(int i = 25; i >= 0; i++) {
            if(count[i] == count[i - 1]){
                maxCount++;
            }else {
                break;
            }
        }


        int res =  (maxTime - 1) * (n + 1) + maxCount;
        return Math.max(tasks.length, res);
}


}