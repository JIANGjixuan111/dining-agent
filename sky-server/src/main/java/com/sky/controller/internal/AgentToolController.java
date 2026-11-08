package com.sky.controller.internal;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.entity.Dish;
import com.sky.properties.AgentProperties;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.ReportService;
import com.sky.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/agent")
public class AgentToolController {

    @Autowired private AgentProperties agentProperties;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private DishService dishService;
    @Autowired private SetmealService setmealService;
    @Autowired private ReportService reportService;

    private void authorize(String token) {
        if (!agentProperties.getInternalToken().equals(token)) {
            throw new SecurityException("invalid agent internal token");
        }
    }

    @GetMapping("/tools/dishes")
    public Result<List> dishes(@RequestHeader("X-Agent-Token") String token) {
        authorize(token);
        Dish query = Dish.builder().status(StatusConstant.ENABLE).build();
        return Result.success(dishService.listWithFlavor(query));
    }

    @GetMapping("/tools/user-profile/{userId}")
    public Result<JSONObject> userProfile(@RequestHeader("X-Agent-Token") String token, @PathVariable Long userId) {
        authorize(token);
        JSONObject result = new JSONObject();
        Map<String, Object> summary = jdbcTemplate.queryForMap(
                "select count(*) orderCount, coalesce(avg(amount),0) averageAmount, coalesce(max(order_time),'') lastOrderTime from orders where user_id = ?", userId);
        List<Map<String, Object>> favorites = jdbcTemplate.queryForList(
                "select od.name, sum(od.number) purchaseCount from order_detail od join orders o on od.order_id=o.id where o.user_id=? group by od.name order by purchaseCount desc limit 10", userId);
        result.put("summary", summary);
        result.put("favoriteDishes", favorites);
        result.put("preferenceSource", "order_history");
        return Result.success(result);
    }

    @GetMapping("/tools/business-metrics")
    public Result<JSONObject> businessMetrics(@RequestHeader("X-Agent-Token") String token) {
        authorize(token);
        LocalDate end = LocalDate.now();
        LocalDate begin = end.minusDays(6);
        JSONObject result = new JSONObject();
        result.put("dateRange", begin + " to " + end);
        result.put("turnover", JSON.toJSON(reportService.getTurnoverStatistics(begin, end)));
        result.put("orders", JSON.toJSON(reportService.getOrderStatistics(begin, end)));
        result.put("salesTop10", JSON.toJSON(reportService.getSalesTop10(begin, end)));
        return Result.success(result);
    }

    @GetMapping("/tools/prep-forecast")
    public Result<List<Map<String, Object>>> prepForecast(@RequestHeader("X-Agent-Token") String token) {
        authorize(token);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "select od.name, round(sum(od.number)/7,1) forecastDailyQuantity from order_detail od join orders o on od.order_id=o.id where o.order_time >= date_sub(now(), interval 7 day) group by od.name order by forecastDailyQuantity desc limit 20");
        return Result.success(rows);
    }

    @PostMapping("/actions/execute")
    public Result<JSONObject> execute(@RequestHeader("X-Agent-Token") String token, @RequestBody JSONObject action) {
        authorize(token);
        String type = action.getString("type");
        if ("DISH_STATUS".equals(type)) {
            dishService.startOrStop(action.getInteger("status"), action.getLong("dishId"));
        } else if ("SETMEAL_STATUS".equals(type)) {
            setmealService.startOrStop(action.getInteger("status"), action.getLong("setmealId"));
        } else if ("CREATE_SETMEAL".equals(type)) {
            SetmealDTO dto = action.getJSONObject("payload").toJavaObject(SetmealDTO.class);
            setmealService.saveWithDish(dto);
        } else if (!"REPORT_ONLY".equals(type)) {
            return Result.error("unsupported agent action: " + type);
        }
        JSONObject result = new JSONObject();
        result.put("executed", true);
        result.put("type", type);
        return Result.success(result);
    }
}
