package com.ateeksh.nexus_ai_package.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import com.ateeksh.nexus_ai_package.config.nexus_properties;

@Slf4j
@AllArgsConstructor
@Service
public class RoutingService {

    private final HardwareService hardwareService;
    private final OllamaModelService ollamaModelService;
    private final PromptAnalyzerService prompmAnalyzerService;
    private final nexus_properties nexusProperites;


    public List<String> getRecommendedModels(double ram){

        List <nexus_properties.ModelEntry> allModels = new ArrayList<>();
        allModels.addAll(nexusProperites.getModels().getCode());
        allModels.addAll(nexusProperites.getModels().getGeneral());

        return allModels.stream()
        .filter(m -> ram > m.getMinRamGb())
        .map(nexus_properties.ModelEntry::getName)
        .distinct()
        .collect(Collectors.toList());

    }

    public String determineBestModel(String prompt){

        String prompt_type = prompmAnalyzerService.analyzePrompt(prompt);
        double ramgb = hardwareService.getAvailableRam();
        List<String> installed_models = ollamaModelService.getInstalledModels();

        log.info("Routing prompt ki type : {} , The available ram in system is : {} GB and The installed models are : {}",
            prompt_type , ramgb , installed_models
        );

        if(installed_models == null || installed_models.isEmpty()  ){
            log.warn("no installed models are found in ollama");
            return "No models found";
        }

        List<nexus_properties.ModelEntry> candidates = "CODE".equals(prompt_type)
        ? nexusProperites.getModels().getCode()
        : nexusProperites.getModels().getGeneral();

        for(nexus_properties.ModelEntry model : candidates){
            if (ramgb > model.getMinRamGb() && installed_models.contains(model.getName())){
                log.info("Selected model : {} " , model);
                return model.getName();
            }
        }

        log.warn("no ideal model found so fall back to the first installed model : {} " , installed_models.get(0));
        return installed_models.get(0);



    }





}
