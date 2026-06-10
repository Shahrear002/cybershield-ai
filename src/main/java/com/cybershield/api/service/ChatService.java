package com.cybershield.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            You are a Digital Advocate for the Bangladesh Cyber Crime Investigation Division.
            Your goal is to converse empathetically with the victim and collect EXACTLY these 4 data points naturally:
            1. Incident Type
            2. Date/Time
            3. Platform involved
            4. Offender Details

            Once you have collected all 4 items, evaluate the severity:
            - If it's a severe crime (hacking, blackmail, severe harassment, extortion), output JSON flag "requires_fir": true.
            - If it's minor, just provide actionable advice and set "requires_fir": false.

            You MUST output a valid JSON object in this exact format, with no markdown fences, no backticks, just the raw JSON string:
            {
              "response_text": "Your conversational response to the user here.",
              "requires_fir": true or false
            }
            """;

    public ChatService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String processChat(List<Map<String, String>> history) {
        List<Message> messages = history.stream().map(msg -> {
            String role = msg.get("role");
            String content = msg.get("content");
            if ("user".equalsIgnoreCase(role)) {
                return new UserMessage(content);
            } else {
                return new AssistantMessage(content);
            }
        }).collect(Collectors.toList());

        messages.add(0, new SystemMessage(SYSTEM_PROMPT));

        try {
            String rawOutput = chatClient.prompt()
                    .messages(messages)
                    .call()
                    .content();

            rawOutput = rawOutput.trim();

            int start = rawOutput.indexOf('{');
            int end = rawOutput.lastIndexOf('}');
            
            String finalJson = rawOutput;
            if (start != -1 && end != -1 && start <= end) {
                finalJson = rawOutput.substring(start, end + 1);
            } else {
                // Fallback: Model did not return JSON. Wrap the plain text in our expected JSON
                // structure.
                java.util.Map<String, Object> fallback = java.util.Map.of(
                        "response_text", rawOutput,
                        "requires_fir", false);
                finalJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(fallback);
            }

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(finalJson);

            if (rootNode.has("requires_fir") && rootNode.get("requires_fir").asBoolean()) {
                log.info("requires_fir is true, calling generateFIRSummary...");
                String summaryJsonStr = generateFIRSummary(messages);
                log.info("generateFIRSummary returned: " + summaryJsonStr);
                com.fasterxml.jackson.databind.JsonNode summaryNode = mapper.readTree(summaryJsonStr);

                if (rootNode instanceof com.fasterxml.jackson.databind.node.ObjectNode objectNode) {
                    if (summaryNode.has("summaryEn")) {
                        objectNode.put("summaryEn", summaryNode.get("summaryEn").asText());
                    }
                    if (summaryNode.has("summaryBn")) {
                        objectNode.put("summaryBn", summaryNode.get("summaryBn").asText());
                    }
                }
                return mapper.writeValueAsString(rootNode);
            }

            return finalJson;
        } catch (Exception e) {
            log.error("AI Chat failed", e);
            throw new RuntimeException("AI Chat failed: " + e.getMessage());
        }
    }

    private String generateFIRSummary(List<Message> chatHistory) {
        String transcript = chatHistory.stream()
                .filter(m -> m instanceof UserMessage || m instanceof AssistantMessage)
                .map(m -> (m instanceof UserMessage ? "User: " : "Bot: ") + m.getContent())
                .collect(Collectors.joining("\n"));

        String summarizationPrompt = """
                You are a legal assistant drafting a formal First Information Report (FIR). Read the provided chat transcript. Output a strict JSON object with two keys: `summaryEn` and `summaryBn`.
                1. `summaryEn`: A concise, formal, first-person narrative in English detailing the facts of the crime. Do not include dialogue or system messages. Only include the date, platform, offender identity, and specific actions taken against the victim.
                2. `summaryBn`: A highly accurate, formal, professional Bengali translation of that exact narrative, suitable for a local police 'এজাহার' (Ejahar).
                
                You MUST output a valid JSON object in this exact format, with no markdown fences, no backticks, just the raw JSON string:
                {
                  "summaryEn": "...",
                  "summaryBn": "..."
                }
                """;

        try {
            List<Message> summaryMessages = List.of(
                new SystemMessage(summarizationPrompt),
                new UserMessage("Here is the transcript:\n" + transcript)
            );

            String rawOutput = chatClient.prompt()
                    .messages(summaryMessages)
                    .call()
                    .content();
            
            rawOutput = rawOutput.trim();
            log.info("FIR Summary Raw LLM Output: " + rawOutput);
            int start = rawOutput.indexOf('{');
            int end = rawOutput.lastIndexOf('}');
            if (start != -1 && end != -1 && start <= end) {
                String extracted = rawOutput.substring(start, end + 1);
                log.info("Extracted FIR Summary JSON: " + extracted);
                return extracted;
            } else {
                log.error("Could not find JSON in FIR Summary output.");
            }
        } catch (Exception e) {
            log.error("Failed to generate FIR summary", e);
        }
        return "{}";
    }
}
