package com.ateeksh.nexus_ai_package.controllers;

import com.ateeksh.nexus_ai_package.service.HardwareService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/system")
class SystemInfoController{

    private final HardwareService hs;

    @GetMapping("/ram")
    public String showRam(){
        return String.format("%.2f", hs.getAvailableRam());
    }

    @GetMapping("/info")
    public String showSystemInfo(){
        return hs.get_system_info();
    }
    

}