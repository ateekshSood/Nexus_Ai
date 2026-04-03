package com.ateeksh.nexus_ai_package.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class AiTalk {

    @Value("${langchain4j.ollama.chat-model.base-url:http://localhost:11434}")
    private String ollamaBaseurl;

    @Autowired
    private RoutingService routingService;
    @Autowired
    private HardwareService hardwareService;

    public String getAiResponse(String prompt){

        try{

            String bestModel = routingService.determineBestModel(prompt);

            if(bestModel.equals("No models found")){
                double ram = hardwareService.getAvailableRam();
                
                List<String> options = routingService.getRecommendedModels(ram);

                String recommendationText = "Recommended models : " + String.join("," , options);

                return recommendationText;
            }

            System.out.println("The model being used currently is " + bestModel);

            ChatLanguageModel model = OllamaChatModel.builder()
                                .baseUrl(ollamaBaseurl)
                                .modelName(bestModel)
                                .timeout(Duration.ofMinutes(2))
                                .build();


            return model.generate(prompt);
        }
        catch(Exception e){
            System.err.println("Error has occured" + e.getMessage());
            return "An error has occured ensure that you have the models downloaded or enough free ram ";
        }
       
        
    }




    

}
