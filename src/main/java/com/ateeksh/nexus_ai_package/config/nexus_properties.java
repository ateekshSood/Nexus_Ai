package com.ateeksh.nexus_ai_package.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "nexus")
public class nexus_properties {

    private Models models = new Models();

    @Data 
    public static class Models{
        private List<ModelEntry> code;
        private List<ModelEntry> general;
    }

    @Data 
    public static class ModelEntry{
        private String name;
        private double minRamGb;
    }

}