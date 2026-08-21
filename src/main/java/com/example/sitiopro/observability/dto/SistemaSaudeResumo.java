package com.example.sitiopro.observability.dto;

import java.time.Duration;

public record SistemaSaudeResumo(
        String aplicacao,
        String banco,
        Duration uptime,
        String versao,
        String ambiente,
        String observabilidade,
        String requestId) {

    public String uptimeFormatado() {
        long totalSeconds = uptime == null ? 0 : uptime.toSeconds();
        long days = totalSeconds / 86_400;
        long hours = (totalSeconds % 86_400) / 3_600;
        long minutes = (totalSeconds % 3_600) / 60;
        if (days > 0) {
            return "%dd %02dh %02dm".formatted(days, hours, minutes);
        }
        return "%02dh %02dm".formatted(hours, minutes);
    }
}
