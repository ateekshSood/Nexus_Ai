package com.ateeksh.nexus_ai_package.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

@Slf4j
@Service
public class HardwareService {

    private final SystemInfo si = new SystemInfo();
    private final HardwareAbstractionLayer hal = si.getHardware();

    public String get_system_info(){

        GlobalMemory memory = hal.getMemory();
        CentralProcessor cpu = hal.getProcessor();
        OperatingSystem os = si.getOperatingSystem();

        double to_gb = 1024.0 * 1024.0 * 1024.0;

        double available_ram = memory.getAvailable()/to_gb;
        double total_ram = memory.getTotal() / to_gb;
        double cpu_load = cpu.getSystemCpuLoad(1000) * 100; //convert to % by mul by 100
        String current_process = os.getCurrentProcess().toString();

        return String.format("CPU Load : %.2f%% || Avaible Ram : %.2f GB / %.2f GB || Current Process : %s",
                cpu_load , available_ram , total_ram , current_process);
    }

    public double getAvailableRam(){
        return hal.getMemory().getAvailable() / (1024.0 * 1024.0 * 1024.0);
        // memory in gb yeah
        //basically will be used for like checking if the models can fit in the ram or not
        // basically for switching yeah
    }
}
