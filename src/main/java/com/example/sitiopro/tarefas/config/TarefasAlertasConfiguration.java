package com.example.sitiopro.tarefas.config;

import com.example.sitiopro.tarefas.service.TarefasAlertasProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TarefasAlertasProperties.class)
public class TarefasAlertasConfiguration {
}
