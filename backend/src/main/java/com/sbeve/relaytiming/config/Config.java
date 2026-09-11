package com.sbeve.relaytiming.config;

public class Config {
    public static final String BROKER_URL = System.getenv().getOrDefault("MQTT_BROKER_URL", "tcp://127.0.0.1:1883");
    public static final String CLIENT_ID = "TagReadListener";
    public static final String TAG_READS_TOPIC = "relay/read";

    public static final Long HANDOFF_WINDOW = 1000L; // in milliseconds
    public static final Long READ_WINDOW = 15L; // in seconds
    public static final Long HANDOFF_ENABLED_WINDOW = 3L; // in minutes
    public static final Long LEG_TIME = 30L; // in minutes
    public static final double LAP_DISTANCE_KM = 0.4;
    public static final Long LEG_TIMEOUT = 30L; // in minutes

    private Config() {
    }
}
