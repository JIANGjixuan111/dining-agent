package com.sky.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.sky.context.BaseContext;
import com.sky.dto.AgentCartConfirmDTO;
import com.sky.dto.AgentChatDTO;
import com.sky.dto.AgentFeedbackDTO;
import com.sky.dto.ShoppingCartDTO;
import com.sky.result.Result;
import com.sky.service.AgentService;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController("userAgentController")
@RequestMapping("/user/agent")
@Api(tags = "AI ordering agent")
public class AgentController {

    @Autowired
    private AgentService agentService;
    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/chat")
    @ApiOperation("Chat with the AI ordering agent")
    public Result<JSONObject> chat(@RequestBody AgentChatDTO agentChatDTO) {
        return Result.success(agentService.chatForUser(BaseContext.getCurrentId(), agentChatDTO));
    }

    @PostMapping("/recommend")
    @ApiOperation("Personalized dish recommendation")
    public Result<JSONObject> recommend(@RequestBody AgentChatDTO dto) {
        return Result.success(agentService.executeForUser(BaseContext.getCurrentId(), dto, "recommend"));
    }

    @PostMapping("/plan-order")
    @ApiOperation("Build a complete budget-aware order plan")
    public Result<JSONObject> planOrder(@RequestBody AgentChatDTO dto) {
        return Result.success(agentService.executeForUser(BaseContext.getCurrentId(), dto, "plan_order"));
    }

    @GetMapping("/sessions/{sessionId}")
    @ApiOperation("Read Agent session history")
    public Result<JSONObject> session(@PathVariable String sessionId) {
        return Result.success(agentService.getSession(sessionId));
    }

    @PostMapping("/feedback")
    @ApiOperation("Submit recommendation feedback")
    public Result<JSONObject> feedback(@RequestBody AgentFeedbackDTO dto) {
        return Result.success(agentService.submitFeedback(BaseContext.getCurrentId(), dto));
    }

    @PostMapping("/confirm-cart")
    @ApiOperation("Confirm agent recommendations and add them to cart")
    public Result<Void> confirmCart(@RequestBody AgentCartConfirmDTO confirmDTO) {
        if (confirmDTO == null || confirmDTO.getItems() == null || confirmDTO.getItems().isEmpty()) {
            return Result.error("items must not be empty");
        }
        for (ShoppingCartDTO item : confirmDTO.getItems()) {
            if (item.getDishId() == null && item.getSetmealId() == null) {
                return Result.error("each item must contain dishId or setmealId");
            }
            shoppingCartService.addShoppingCart(item);
        }
        return Result.success();
    }

    @PostMapping("/tasks")
    @ApiOperation("Create an asynchronous Agent task")
    public Result<JSONObject> createTask(@RequestBody AgentChatDTO dto) {
        return Result.success(agentService.createTask("user", BaseContext.getCurrentId(), dto));
    }

    @GetMapping("/tasks/{taskId}")
    public Result<JSONObject> task(@PathVariable String taskId) {
        return Result.success(agentService.getTask("user", BaseContext.getCurrentId(), taskId));
    }

    @PostMapping("/tasks/{taskId}/confirm")
    public Result<JSONObject> confirmTask(@PathVariable String taskId) {
        return Result.success(agentService.confirmTask("user", BaseContext.getCurrentId(), taskId));
    }

    @GetMapping("/profile")
    public Result<JSONObject> profile() {
        return Result.success(agentService.getProfile(BaseContext.getCurrentId()));
    }

    @GetMapping("/knowledge")
    public Result<JSONObject> knowledge(String query) {
        return Result.success(agentService.searchKnowledge(query));
    }
}
