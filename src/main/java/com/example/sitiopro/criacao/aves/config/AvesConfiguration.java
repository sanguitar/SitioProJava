package com.example.sitiopro.criacao.aves.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AvesProperties.class)
public class AvesConfiguration {
}
