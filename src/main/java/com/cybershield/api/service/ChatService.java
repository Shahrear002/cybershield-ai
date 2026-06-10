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
            
            // Clean markdown fences if any
            if (rawOutput != null && rawOutput.trim().startsWith("```json")) {
                rawOutput = rawOutput.substring(rawOutput.indexOf('\n') + 1);
                if (rawOutput.endsWith("```")) {
                    rawOutput = rawOutput.substring(0, rawOutput.length() - 3);
                }
            } else if (rawOutput != null && rawOutput.trim().startsWith("```")) {
                rawOutput = rawOutput.substring(rawOutput.indexOf('\n') + 1);
                if (rawOutput.endsWith("```")) {
                    rawOutput = rawOutput.substring(0, rawOutput.length() - 3);
                }
            }
            return rawOutput.trim();
        } catch (Exception e) {
            log.error("AI Chat failed", e);
            throw new RuntimeException("AI Chat failed: " + e.getMessage());
        }
    }
}
