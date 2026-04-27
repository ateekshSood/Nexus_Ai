package com.ateeksh.nexus_ai_package.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Slf4j
@Service
public class OllamaModelService{
    @Value("${langchain4j.ollama.chat-model.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    private final RestTemplate restTemplate;

    public OllamaModelService(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    public String pullModel(String modelName){
        try{
            String url = ollamaBaseUrl + "/api/pull";

            Map<String , Object> requestBody = Map.of(
                "name" , modelName, 
                "stream" , false
            );

            restTemplate.postForEntity(url, requestBody, String.class);
            log.info("successfully downloaded models");
            return "Successfuly downloaded" + modelName;
        }

        catch(Exception e){
            log.error("error has occured with pulling models" + e.getMessage());
            return "Failed to downlaod model";
        }
    }

    public List<String> getInstalledModels(){
        try{
            String url = ollamaBaseUrl + "/api/tags";
            ResponseEntity<Map<String , Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String , Object>>(){}
            );

            Map<String , Object> body = response.getBody();
            List<String> modelNames = new ArrayList<>();

            if (body != null && body.containsKey("models")){
                @SuppressWarnings("unchecked")
                List<Map<String , Object>> models = (List<Map<String , Object>>) body.get("models");
                
                for(Map<String , Object> model : models){
                    modelNames.add((String) model.get("name"));
                }
            }

            log.info("found {} installed ollama modesl", modelNames.size());

            return modelNames;
            
        }

        catch(Exception e){
            log.error("Failed to connect to the ollama api");
            return new ArrayList<>();
        }
    }

}