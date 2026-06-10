package com.cybershield.api;

import com.cybershield.api.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<String> chat(@RequestBody Map<String, Object> payload) {
        List<Map<String, String>> history = (List<Map<String, String>>) payload.get("history");
        String response = chatService.processChat(history);
        return ResponseEntity.ok(response);
    }
}
