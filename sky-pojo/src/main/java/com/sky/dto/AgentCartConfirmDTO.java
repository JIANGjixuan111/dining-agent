package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AgentCartConfirmDTO implements Serializable {

    private String sessionId;
    private List<ShoppingCartDTO> items;
}
