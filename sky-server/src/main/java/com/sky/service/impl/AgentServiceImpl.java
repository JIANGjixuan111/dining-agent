package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.StatusConstant;
import com.sky.dto.AgentChatDTO;
import com.sky.dto.AgentFeedbackDTO;
import com.sky.entity.Dish;
import com.sky.properties.AgentProperties;
import com.sky.service.AgentService;
import com.sky.service.DishService;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class AgentServiceImpl implements AgentService {

    @Autowired
    private AgentProperties agentProperties;
    @Autowired
    private DishService dishService;
    @Autowired
    private ReportService reportService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public JSONObject chatForUser(Long userId, AgentChatDTO agentChatDTO) {
        return executeForUser(userId, agentChatDTO, "chat");
    }

    @Override
    public JSONObject executeForUser(Long userId, AgentChatDTO agentChatDTO, String intent) {
        JSONObject context = new JSONObject();
        Dish dish = Dish.builder().status(StatusConstant.ENABLE).build();
        List<DishVO> dishes = dishService.listWithFlavor(dish);
        context.put("dishes", JSON.parseArray(JSON.toJSONString(dishes)));
        agentChatDTO.setIntent(intent);
        return invoke("user", userId, agentChatDTO, context);
    }

    @Override
    public JSONObject chatForAdmin(Long employeeId, AgentChatDTO agentChatDTO) {
        return executeForAdmin(employeeId, agentChatDTO, "chat");
    }

    @Override
    public JSONObject executeForAdmin(Long employeeId, AgentChatDTO agentChatDTO, String intent) {
        LocalDate end = LocalDate.now();
        LocalDate begin = end.minusDays(6);
        JSONObject context = new JSONObject();
        TurnoverReportVO turnover = reportService.getTurnoverStatistics(begin, end);
        OrderReportVO orders = reportService.getOrderStatistics(begin, end);
        SalesTop10ReportVO top10 = reportService.getSalesTop10(begin, end);
        context.put("dateRange", begin + " to " + end);
        context.put("turnover", JSON.parseObject(JSON.toJSONString(turnover)));
        context.put("orders", JSON.parseObject(JSON.toJSONString(orders)));
        context.put("salesTop10", JSON.parseObject(JSON.toJSONString(top10)));
        agentChatDTO.setIntent(intent);
        return invoke("admin", employeeId, agentChatDTO, context);
    }

    private JSONObject invoke(String role, Long actorId, AgentChatDTO dto, JSONObject context) {
        if (dto == null || !StringUtils.hasText(dto.getMessage())) {
            throw new IllegalArgumentException("message must not be empty");
        }
        JSONObject request = new JSONObject();
        request.put("role", role);
        request.put("actor_id", actorId);
        request.put("session_id", dto.getSessionId());
        request.put("message", dto.getMessage().trim());
        request.put("intent", StringUtils.hasText(dto.getIntent()) ? dto.getIntent() : "chat");
        request.put("context", context);
        try {
            String url = agentProperties.getBaseUrl() + "/v1/agent/chat";
            String responseBody = restTemplate.postForObject(url, request, String.class);
            if (!StringUtils.hasText(responseBody)) {
                throw new IllegalStateException("empty agent response");
            }
            return JSON.parseObject(responseBody);
        } catch (Exception ex) {
            log.error("agent service unavailable", ex);
            throw new IllegalStateException("AI agent service is unavailable. Start ai-agent-service first.");
        }
    }

    @Override
    public JSONObject getSession(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            throw new IllegalArgumentException("sessionId must not be empty");
        }
        return restTemplate.getForObject(agentProperties.getBaseUrl() + "/v1/agent/sessions/" + sessionId, JSONObject.class);
    }

    @Override
    public JSONObject submitFeedback(Long userId, AgentFeedbackDTO feedbackDTO) {
        if (feedbackDTO == null || !StringUtils.hasText(feedbackDTO.getSessionId()) || feedbackDTO.getRating() == null) {
            throw new IllegalArgumentException("sessionId and rating are required");
        }
        JSONObject request = new JSONObject();
        request.put("actor_id", userId);
        request.put("session_id", feedbackDTO.getSessionId());
        request.put("rating", feedbackDTO.getRating());
        request.put("comment", feedbackDTO.getComment());
        return restTemplate.postForObject(agentProperties.getBaseUrl() + "/v1/agent/feedback", request, JSONObject.class);
    }

    @Override
    public JSONObject createTask(String role, Long actorId, AgentChatDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getMessage())) {
            throw new IllegalArgumentException("message must not be empty");
        }
        JSONObject request = new JSONObject();
        request.put("role", role);
        request.put("actor_id", actorId);
        request.put("session_id", dto.getSessionId());
        request.put("message", dto.getMessage().trim());
        request.put("intent", StringUtils.hasText(dto.getIntent()) ? dto.getIntent() : "chat");
        request.put("context", new JSONObject());
        return restTemplate.postForObject(agentProperties.getBaseUrl() + "/v1/agent/tasks", request, JSONObject.class);
    }

    @Override
    public JSONObject getTask(String role, Long actorId, String taskId) {
        JSONObject task = restTemplate.getForObject(agentProperties.getBaseUrl() + "/v1/agent/tasks/" + taskId, JSONObject.class);
        assertTaskOwner(task, role, actorId);
        return task;
    }

    @Override
    public JSONObject confirmTask(String role, Long actorId, String taskId) {
        JSONObject task = getTask(role, actorId, taskId);
        assertTaskOwner(task, role, actorId);
        return restTemplate.postForObject(agentProperties.getBaseUrl() + "/v1/agent/tasks/" + taskId + "/confirm", new JSONObject(), JSONObject.class);
    }

    @Override
    public JSONObject getProfile(Long userId) {
        return restTemplate.getForObject(agentProperties.getBaseUrl() + "/v1/agent/profiles/" + userId, JSONObject.class);
    }

    @Override
    public JSONObject searchKnowledge(String query) {
        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException("query must not be empty");
        }
        String url = UriComponentsBuilder.fromHttpUrl(agentProperties.getBaseUrl() + "/v1/knowledge/search")
                .queryParam("q", query).encode().toUriString();
        return restTemplate.getForObject(url, JSONObject.class);
    }

    private void assertTaskOwner(JSONObject task, String role, Long actorId) {
        if (task == null || !role.equals(task.getString("role")) || !actorId.equals(task.getLong("actorId"))) {
            throw new SecurityException("agent task does not belong to current actor");
        }
    }
}
