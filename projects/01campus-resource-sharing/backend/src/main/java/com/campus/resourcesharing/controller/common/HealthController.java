package com.campus.resourcesharing.controller.common;

import com.campus.resourcesharing.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/common")
public class HealthController {

    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", "ok");
        payload.put("service", "campus-resource-sharing-backend");
        return Result.success(payload);
    }
}
