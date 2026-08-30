package com.sbeve.relaytiming.config;

public class Config {
    public static final String BROKER_URL = "tcp://127.0.0.1:1883";
    public static final String CLIENT_ID = "TagReadListener";
    public static final String TAG_READS_TOPIC = "relay/read";

    public static final Long HANDOFF_WINDOW = 1L;
    public static final Long READ_WINDOW = 5L;
    public static final Long LAP_TIMEOUT = 10L;

    private Config() {
    }
}
