package com.ateeksh.nexus_ai_package.controllers;

import com.ateeksh.nexus_ai_package.service.HardwareService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class showSystem{

    private final HardwareService hs;

    public showSystem(HardwareService hs){
        this.hs = hs;
    }

    @GetMapping("/userram")
    public String showRam(){
        double ram = hs.getAvailableRam();
        String ram_string = String.valueOf(ram);
        return ram_string + " GB";
    }

}