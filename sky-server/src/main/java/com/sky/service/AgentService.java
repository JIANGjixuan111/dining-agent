package com.sky.service;

import com.alibaba.fastjson.JSONObject;
import com.sky.dto.AgentChatDTO;
import com.sky.dto.AgentFeedbackDTO;

public interface AgentService {

    JSONObject chatForUser(Long userId, AgentChatDTO agentChatDTO);

    JSONObject chatForAdmin(Long employeeId, AgentChatDTO agentChatDTO);

    JSONObject executeForUser(Long userId, AgentChatDTO agentChatDTO, String intent);

    JSONObject executeForAdmin(Long employeeId, AgentChatDTO agentChatDTO, String intent);

    JSONObject getSession(String sessionId);

    JSONObject submitFeedback(Long userId, AgentFeedbackDTO feedbackDTO);

    JSONObject createTask(String role, Long actorId, AgentChatDTO dto);

    JSONObject getTask(String role, Long actorId, String taskId);

    JSONObject confirmTask(String role, Long actorId, String taskId);

    JSONObject getProfile(Long userId);

    JSONObject searchKnowledge(String query);
}
