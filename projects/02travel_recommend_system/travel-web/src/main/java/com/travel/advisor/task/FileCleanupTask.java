package com.travel.advisor.task;

import com.travel.advisor.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileCleanupTask {

    // 文件清理
    private final FileService fileService;

    /**
     * 每小时执行一次清理过期临时文件的任务
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupTempFiles() {
        long start = System.currentTimeMillis();
        int count = 0;
        try {
            count = fileService.cleanupTempFiles();
            long cost = System.currentTimeMillis() - start;
            if (count > 0) {
                log.info("清理过期临时文件任务执行完成，共清理 {} 个文件，耗时 {} ms", count, cost);
            }
        } catch (Exception e) {
            log.error("清理过期临时文件任务执行异常", e);
        }
    }
}