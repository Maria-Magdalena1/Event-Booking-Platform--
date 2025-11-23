package main.services;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final OpenAiChatModel chatModel;

    public AiService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String generateEventDescription(String title) {
        try {
            String prompt = """
                    Write a short description for the event titled: "%s".
                    """.formatted(title);

            return chatModel.call(prompt);
        } catch (NonTransientAiException e) {
            return "AI description unavailable right now. Please try later.";
        } catch (Exception e) {
            return "An unexpected error occurred while generating the AI description.";
        }
    }
}
