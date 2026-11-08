package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.agent")
@Data
public class AgentProperties {

    private String baseUrl = "http://127.0.0.1:8001";
    private String internalToken = "sky-agent-local-token";
}
