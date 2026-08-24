package com.sbeve.relaytiming.config;

public class Config {
    public static final String BROKER_URL = "tcp://192.168.1.204:1883";
    public static final String CLIENT_ID = "TagReadListener";
    public static final String TAG_READS_TOPIC = "relay/tags/read";

    public static final Long HANDOFF_WINDOW = 1L;
    public static final Long READ_WINDOW = 5L;

    private Config() {
    }
}
