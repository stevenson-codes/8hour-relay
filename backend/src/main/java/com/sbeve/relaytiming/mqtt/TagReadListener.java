package com.sbeve.relaytiming.mqtt;

import java.nio.charset.StandardCharsets;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class TagReadListener implements MqttCallback {
    private static final Logger log = LoggerFactory.getLogger(TagReadListener.class);

    private MqttClient mqttClient;

    @PostConstruct
    public void connect() throws MqttException {
        mqttClient = new MqttClient(Config.BROKER_URL, Config.CLIENT_ID, new MemoryPersistence());
        mqttClient.setCallback(this);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);

        mqttClient.connect(options);
        mqttClient.subscribe(Config.TAG_READS_TOPIC);
        log.info("Subscribed to MQTT topic '{}' on broker {}", Config.TAG_READS_TOPIC, Config.BROKER_URL);
    }

    @PreDestroy
    public void disconnect() throws MqttException {
        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.disconnect();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("Lost connection to MQTT broker at {}", Config.BROKER_URL, cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String epcHex = new String(message.getPayload(), StandardCharsets.UTF_8);
        log.info("Tag read received on '{}': {}", topic, epcHex);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}
