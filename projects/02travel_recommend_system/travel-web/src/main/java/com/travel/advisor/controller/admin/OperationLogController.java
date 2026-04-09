package com.travel.advisor.controller.admin;

import com.travel.advisor.common.page.PageResult;
import com.travel.advisor.common.result.Result;
import com.travel.advisor.dto.log.OperationLogQueryDTO;
import com.travel.advisor.entity.OperationLogE;
import com.travel.advisor.service.OperationLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/operation-logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping
    public Result<PageResult<OperationLogE>> page(@Valid OperationLogQueryDTO queryDTO) {
        return Result.success(operationLogService.page(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<OperationLogE> detail(@PathVariable Long id) {
        return Result.success(operationLogService.detail(id));
    }
}
