package com.database.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * GET /api/ai/chart/{chartId}: 返回 AI 生成的图表 PNG 图片
 */
@RestController
@RequestMapping("/api/ai")
@Slf4j
public class AiChartController {

    private final Path chartDir;

    public AiChartController(@Value("${aiagent.chart-dir:../charts}") String chartDirStr) {
        this.chartDir = Paths.get(chartDirStr).toAbsolutePath().normalize();
        log.info("图表目录: {}", this.chartDir);
    }

    /**
     * GET /api/ai/chart/{chartId}: 根据 chartId 返回对应的 PNG 图片
     * @param chartId 图表唯一标识（不含扩展名）
     * @return image/png 二进制内容，或 404
     */
    @GetMapping("/chart/{chartId}")
    public ResponseEntity<byte[]> getChart(@PathVariable String chartId) {
        if (!chartId.matches("^[a-zA-Z0-9_-]+$")) {
            return ResponseEntity.badRequest().build();
        }

        Path filePath = chartDir.resolve(chartId + ".png").normalize();
        if (!filePath.startsWith(chartDir)) {
            return ResponseEntity.badRequest().build();
        }

        if (!Files.exists(filePath)) {
            log.warn("图表文件不存在: {}", filePath);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            byte[] imageBytes = Files.readAllBytes(filePath);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .contentLength(imageBytes.length)
                    .body(imageBytes);
        } catch (IOException e) {
            log.error("读取图表文件失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
