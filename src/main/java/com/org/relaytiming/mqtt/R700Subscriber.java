package com.org.relaytiming.mqtt;

import com.org.relaytiming.config.MqttProperties;
import com.org.relaytiming.services.R700EventService;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class R700Subscriber {

    private static final Logger logger = LoggerFactory.getLogger(R700Subscriber.class);

    private final ObjectMapper mapper;
    private final R700EventService r700EventService;
    private final MqttProperties mqttProperties;

    private MqttClient client;

    public R700Subscriber(ObjectMapper mapper, R700EventService r700EventService, MqttProperties mqttProperties) {
        this.mapper = mapper;
        this.r700EventService = r700EventService;
        this.mqttProperties = mqttProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        if (!mqttProperties.enabled()) {
            logger.info("MQTT subscriber disabled via mqtt.enabled=false");
            return;
        }

        try {
            client = new MqttClient(mqttProperties.brokerUrl(), mqttProperties.clientId(), new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    logger.warn("MQTT broker connection lost", cause);
                }

                @Override
                public void messageArrived(String topicName, MqttMessage message) {
                    try {
                        String payload = new String(message.getPayload());
                        R700TagEvent event = mapper.readValue(payload, R700TagEvent.class);
                        r700EventService.handleTagEvent(event);
                    } catch (Exception e) {
                        logger.warn("Failed to parse MQTT tag payload: {}", new String(message.getPayload()), e);
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // no-op for subscriber
                }
            });

            logger.info("Connecting to MQTT broker {} on topic {}", mqttProperties.brokerUrl(), mqttProperties.topic());
            client.connect(options);
            client.subscribe(mqttProperties.topic(), mqttProperties.qos());
            logger.info("Subscribed to MQTT topic {}", mqttProperties.topic());

        } catch (MqttException e) {
            logger.error("Failed to start MQTT subscriber", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
                client.close();
                logger.info("MQTT subscriber disconnected");
            } catch (MqttException e) {
                logger.warn("Error disconnecting MQTT subscriber", e);
            }
        }
    }

}
