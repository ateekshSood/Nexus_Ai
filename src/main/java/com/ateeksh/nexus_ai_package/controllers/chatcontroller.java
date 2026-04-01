package com.ateeksh.nexus_ai_package.controllers;

import com.ateeksh.nexus_ai_package.service.AiTalk;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;


@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class chatcontroller{

    @Autowired
    private AiTalk aitalk;

    @PostMapping("/chat")
    public String chat(@RequestBody String msg){
        return aitalk.getAiResponse(msg);
    }
    
}