package com.ateeksh.nexus_ai_package.service;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RoutingService {

    private final HardwareService hardwareService;
    private final OllamaModelService ollamaModelService;
    private final PromptAnalyzerService prompmAnalyzerService;

    
    RoutingService(HardwareService hardwareService , OllamaModelService ollamaModelService,
        PromptAnalyzerService promptAnalyzerService){

            this.hardwareService = hardwareService;
            this.ollamaModelService = ollamaModelService;
            this.prompmAnalyzerService = promptAnalyzerService;

    }

    public String determineBestModel(String prompt){

        String prompt_type = prompmAnalyzerService.analyzePrompt(prompt);
        double ramgb = hardwareService.getAvailableRam();

        List<String> model_list = ollamaModelService.getInstallModels();

        if(model_list.isEmpty()){
            System.err.println("Model list is empty no model found so defaulting to llama3.2");
            return "llama3.2";
        }

        if("CODE".equals(prompt_type)){
            if(ramgb > 16 && model_list.contains("codellama")){
                return "codellama";
            }
            else if(model_list.contains("qwen2.5-coder")){
                return "qwen2.5-coder";
            }
        }
        
        else{
            if(ramgb > 8 && model_list.contains("llama3")){
                return "llana3";
            }
            else if(model_list.contains("phi3")){
                return "phi3";
            }
        }


        return model_list.get(0);



    }





}
