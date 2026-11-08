package com.sky.controller.admin;

import com.alibaba.fastjson.JSONObject;
import com.sky.context.BaseContext;
import com.sky.dto.AgentChatDTO;
import com.sky.result.Result;
import com.sky.service.AgentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController("adminAgentController")
@RequestMapping("/admin/agent")
@Api(tags = "AI business agent")
public class AgentController {

    @Autowired
    private AgentService agentService;

    @PostMapping("/chat")
    @ApiOperation("Chat with the AI business analyst")
    public Result<JSONObject> chat(@RequestBody AgentChatDTO agentChatDTO) {
        return Result.success(agentService.chatForAdmin(BaseContext.getCurrentId(), agentChatDTO));
    }

    @PostMapping("/insights")
    @ApiOperation("Generate business insights")
    public Result<JSONObject> insights(@RequestBody AgentChatDTO dto) {
        return Result.success(agentService.executeForAdmin(BaseContext.getCurrentId(), dto, "business_insights"));
    }

    @PostMapping("/menu-optimize")
    @ApiOperation("Generate menu optimization proposal")
    public Result<JSONObject> menuOptimize(@RequestBody AgentChatDTO dto) {
        return Result.success(agentService.executeForAdmin(BaseContext.getCurrentId(), dto, "menu_optimize"));
    }

    @PostMapping("/marketing-plan")
    @ApiOperation("Generate a marketing proposal")
    public Result<JSONObject> marketingPlan(@RequestBody AgentChatDTO dto) {
        return Result.success(agentService.executeForAdmin(BaseContext.getCurrentId(), dto, "marketing_plan"));
    }

    @PostMapping("/anomaly-detect")
    @ApiOperation("Detect business anomalies")
    public Result<JSONObject> anomalyDetect(@RequestBody AgentChatDTO dto) {
        return Result.success(agentService.executeForAdmin(BaseContext.getCurrentId(), dto, "anomaly_detect"));
    }

    @PostMapping("/forecast")
    public Result<JSONObject> forecast(@RequestBody AgentChatDTO dto) {
        return Result.success(agentService.executeForAdmin(BaseContext.getCurrentId(), dto, "prep_forecast"));
    }

    @PostMapping("/tasks")
    public Result<JSONObject> createTask(@RequestBody AgentChatDTO dto) {
        return Result.success(agentService.createTask("admin", BaseContext.getCurrentId(), dto));
    }

    @GetMapping("/tasks/{taskId}")
    public Result<JSONObject> task(@PathVariable String taskId) {
        return Result.success(agentService.getTask("admin", BaseContext.getCurrentId(), taskId));
    }

    @PostMapping("/tasks/{taskId}/confirm")
    public Result<JSONObject> confirmTask(@PathVariable String taskId) {
        return Result.success(agentService.confirmTask("admin", BaseContext.getCurrentId(), taskId));
    }

    @GetMapping("/knowledge")
    public Result<JSONObject> knowledge(@RequestParam String query) {
        return Result.success(agentService.searchKnowledge(query));
    }
}
