package com.sbeve.relaytiming.mqtt;

import java.nio.charset.StandardCharsets;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sbeve.relaytiming.config.Config;
import com.sbeve.relaytiming.services.TagReadService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class TagReadListener implements MqttCallback {
    private static final Logger log = LoggerFactory.getLogger(TagReadListener.class);

    private MqttClient mqttClient;
    private final TagReadService tagReadService;

    public TagReadListener(TagReadService tagReadService) {
        this.tagReadService = tagReadService;
    }

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
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);

        JSONObject json;
        try {
            json = new JSONObject(payload);        String timestamp = json.getString("timestamp");
            JSONObject tagInventoryEvent = json.getJSONObject("tagInventoryEvent");
            String epcHex = tagInventoryEvent.getString("epcHex");
            int peakRssiCdbm = tagInventoryEvent.getInt("peakRssiCdbm");

            tagReadService.handleTagRead(epcHex, timestamp, peakRssiCdbm);
        } catch (Exception e) {
            log.warn("Failed to parse tag event on '{}': {}", topic, payload, e);
            return;
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}
