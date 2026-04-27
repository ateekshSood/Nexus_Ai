package com.ateeksh.nexus_ai_package.service;

import org.springframework.stereotype.Service;

import com.ateeksh.nexus_ai_package.repository.ChatSessionRepository;

import org.springframework.beans.factory.annotation.Value;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.AllArgsConstructor;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@AllArgsConstructor
@Service
public class AiTalk {

    @Value("${langchain4j.ollama.chat-model.base-url:http://localhost:11434}")
    private String ollamaBaseurl;

    private final RoutingService routingService;
    private final HardwareService hardwareService;
    private final PersistentChatMemoryStore chatMemoryStore;
    private final ChatSessionRepository chatSessionRepository;
    
    // model cache

    private final ConcurrentHashMap<String , ChatLanguageModel> modelCache = new ConcurrentHashMap<>();

    // model memory share 

    private final ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(20);

    interface Assistant {
        String chat(String message);
    }

    public String getAiResponse(String prompt , String chatId){

        try{

            String bestModel = routingService.determineBestModel(prompt);

            if("No models found".equals(bestModel)){
                double ram = hardwareService.getAvailableRam();
                List<String> options = routingService.getRecommendedModels(ram);
                return "no compatable model found for your ram , models recommended for your ram " + String.join("," , options);

            }

            log.info("Using model : {} for session : {} " , bestModel , chatId);

            ChatLanguageModel model = modelCache.computeIfAbsent(bestModel, name -> 
                OllamaChatModel.builder()
                    .baseUrl(ollamaBaseurl)
                    .modelName(bestModel)
                    .timeout(Duration.ofMinutes(2))
                    .build()
            );

            MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                .maxMessages(20)
                .id(chatId)
                .chatMemoryStore(chatMemoryStore)
                .build();

            Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(model)
                .chatMemory(chatMemory)
                .build();

            return assistant.chat(prompt);
        }

        catch(Exception e){
            log.error("Error has occured" + e.getMessage());
            return "An error has occured ensure that you have the models downloaded or enough free ram ";
        }
       
        
    }




    

}
