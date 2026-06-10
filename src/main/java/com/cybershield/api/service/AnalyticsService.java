package com.cybershield.api.service;

import com.cybershield.api.model.FIRObject;
import com.cybershield.api.model.FIRRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AnalyticsService {

    private final FIRRepository firRepository;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AnalyticsService(FIRRepository firRepository, ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.firRepository = firRepository;
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String generateAnalytics() {
        log.info("Fetching top 50 recent FIRs for analytics...");
        List<FIRObject> recentFirs = firRepository.findTop50ByOrderByCreatedAtDesc();

        List<Map<String, String>> mappedData = recentFirs.stream()
                .map(fir -> Map.of(
                        "subject", fir.getSubject() != null ? fir.getSubject() : "",
                        "placeOfOffence", fir.getPlaceOfOffence() != null ? fir.getPlaceOfOffence() : "",
                        "offenderDetails", fir.getOffenderDetails() != null ? fir.getOffenderDetails() : "",
                        "summarizedNarrative", fir.getChronologicalNarrative() != null ? fir.getChronologicalNarrative() : ""
                ))
                .collect(Collectors.toList());

        String dataJson;
        try {
            dataJson = objectMapper.writeValueAsString(mappedData);
        } catch (JsonProcessingException e) {
            log.error("Failed to map FIR data to JSON", e);
            throw new RuntimeException("Failed to process analytics data");
        }

        String systemPrompt = "You are a Cyber Threat Intelligence analyst. Review this batch of incident logs. " +
                "Perform semantic deduplication on the offender details, cross-reference platform patterns, " +
                "and return a strict JSON object matching these keys:\n" +
                "- `totalCases`: Integer\n" +
                "- `serialThreatActors`: Array of objects with `inferredHandle`, `caseCount`, and `semanticJustification`.\n" +
                "- `emergingTrends`: Array of strings detailing localized spikes.";

        log.info("Calling LLM for semantic deduplication on {} records...", mappedData.size());
        
        String response = chatClient.prompt()
                .system(systemPrompt)
                .user(dataJson)
                .call()
                .content();

        // Extract JSON if it's wrapped in markdown code blocks
        if (response != null) {
            int start = response.indexOf("{");
            int end = response.lastIndexOf("}");
            if (start != -1 && end != -1 && start <= end) {
                return response.substring(start, end + 1);
            }
        }

        return response;
    }
}
