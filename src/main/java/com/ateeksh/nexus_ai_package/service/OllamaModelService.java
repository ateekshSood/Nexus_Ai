package com.ateeksh.nexus_ai_package.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class OllamaModelService{
    @Value("${langchain4j.ollama.chat-model.base-url:=}")
    private String ollamaBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<String> getInstallModels(){
        try{
            String url = ollamaBaseUrl + "/api/tags";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String , Object> body = response.getBody();
            List<String> modelNames = new ArrayList<>();

            if (body != null && body.containsKey("models")){
                List<Map<String , Object>> models = (List<Map<String , Object>>) body.get("models");
                

                for(Map<String , Object> model : models){
                    modelNames.add((String) model.get("name"));
                }
            }

            return modelNames;
            
        }

        catch(Exception e){
            System.err.println("Failed to connect to the ollama api");
            return new ArrayList<>();
        }
    }

}