package com.travel.advisor.controller.admin;

import com.travel.advisor.annotation.OperationLog;
import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.Result;
import com.travel.advisor.dto.config.SystemConfigQueryDTO;
import com.travel.advisor.dto.config.SystemConfigUpdateDTO;
import com.travel.advisor.entity.SystemConfig;
import com.travel.advisor.service.SystemConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/system-configs")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    public Result<PageResult<SystemConfig>> page(@Valid SystemConfigQueryDTO queryDTO) {
        return Result.success(systemConfigService.page(queryDTO));
    }

    @GetMapping("/{configKey}")
    public Result<SystemConfig> detail(@PathVariable String configKey) {
        return Result.success(systemConfigService.detail(configKey));
    }

    @PutMapping("/{configKey}")
    @OperationLog(module = "config", action = "update", description = "更新系统配置")
    public Result<Void> update(@PathVariable String configKey, @Valid @RequestBody SystemConfigUpdateDTO dto) {
        systemConfigService.update(configKey, dto);
        return Result.success();
    }
}
