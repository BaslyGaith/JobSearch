package com.jobfinder.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String frontendUrl = "http://localhost:5173";
    private LinkedIn linkedin = new LinkedIn();

    @Data
    public static class LinkedIn {
        private boolean mockMode = true;
        private String clientId;
        private String clientSecret;
        private String redirectUri;
    }
}
