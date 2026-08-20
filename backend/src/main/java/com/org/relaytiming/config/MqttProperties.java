package com.org.relaytiming.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mqtt")
public record MqttProperties(
        boolean enabled,
        String brokerUrl,
        String clientId,
        String topic,
        int qos
) {
}
