package main.web;

import lombok.RequiredArgsConstructor;
import main.services.AiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AiController {
    private final AiService aiService;

    @GetMapping("/description")
    public Map<String, String> generateDescription(@RequestParam String title) {
        String description = aiService.generateEventDescription(title);
        return Map.of("description", description);
    }
}

