package com.sky.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class AgentFeedbackDTO implements Serializable {
    private String sessionId;
    private Integer rating;
    private String comment;
}
