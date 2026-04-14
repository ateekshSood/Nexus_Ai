package com.ateeksh.nexus_ai_package.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

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


    public List<String> getRecommendedModels(double ram){

        List <String> options = new ArrayList<>();

        if (ram >= 16.0){
            options.add("llama3:8b");
            options.add("codellama:13b");
            options.add("mixtral:8x7b");
        } 

        else if (ram >= 12.0){
            options.add("llama3.2:3b");
            options.add("qwen2.5-coder:7b");
        } 

        else if (ram >= 8.0){
            options.add("llama3.2:3b");
            options.add("phi3:mini");
        } 

        else if (ram >= 4.0){
            options.add("llama3.2:1b");
            options.add("qwen2.5-coder:1.5b");
        } 

        else if (ram >= 2.0){
            options.add("qwen2.5:0.5b");
            options.add("tinyllama:1.1b");
        } 

        else{
            options.add("qwen2.5:0.5b"); 
        }

        return options;

    }

    public String determineBestModel(String prompt){

        String prompt_type = prompmAnalyzerService.analyzePrompt(prompt);
        double ramgb = hardwareService.getAvailableRam();

        List<String> model_list = ollamaModelService.getInstallModels();

        if(model_list == null || model_list.isEmpty()  ){
            return "No models found";
        }

        if("CODE".equals(prompt_type)){
            if(ramgb >= 16.0 && model_list.contains("codellama:13b")){
                return "codellama:13b";
            }
            else if(ramgb >= 12.0 && model_list.contains("qwen2.5-coder:7b")){
                return "qwen2.5-coder:7b";
            }
            else if(ramgb >= 4.0 && model_list.contains("qwen2.5-coder:1.5b")){
                return "qwen2.5-coder:1.5b";
            }
            else if(model_list.contains("qwen2.5:0.5b")){
                return "qwen2.5:0.5b";
            }
        }

        else {
            if(ramgb >= 16.0 && model_list.contains("mixtral:8x7b")){
                return "mixtral:8x7b";
            }
            else if(ramgb >= 12.0 && model_list.contains("llama3:8b")){
                return "llama3:8b";
            }
            else if(ramgb >= 8.0 && model_list.contains("llama3.2:3b")){
                return "llama3.2:3b";
            }
            else if(ramgb >= 4.0 && model_list.contains("llama3.2:1b")){
                return "llama3.2:1b";
            }
            else if(ramgb >= 2.0 && model_list.contains("tinyllama:1.1b")){
                return "tinyllama:1.1b";
            }
            else if(model_list.contains("qwen2.5:0.5b")){
                return "qwen2.5:0.5b";
            }
        }


        return model_list.get(0);



    }





}
