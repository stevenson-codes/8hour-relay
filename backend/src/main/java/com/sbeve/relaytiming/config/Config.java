package com.sbeve.relaytiming.config;

public class Config {
    public static final String BROKER_URL = "tcp://127.0.0.1:1883";
    public static final String CLIENT_ID = "TagReadListener";
    public static final String TAG_READS_TOPIC = "relay/read";

    public static final Long HANDOFF_WINDOW = 500L; // in milliseconds
    public static final Long READ_WINDOW = 5L; // in seconds
    public static final Long HANDOFF_ENABLED_WINDOW = 3L; // in minutes
    public static final Long LEG_TIME = 4L; // in minutes
    public static final double LAP_DISTANCE_KM = 0.4;
    public static final Long LEG_TIMEOUT = 10L; // in minutes

    private Config() {
    }
}
