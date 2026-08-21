package com.sbeve.relaytiming.mqtt;

public class Config {
    public static final String BROKER_URL = "tcp://192.168.1.204:1883";
    public static final String CLIENT_ID = "TagReadListener";
    public static final String TAG_READS_TOPIC = "relay/tags/read";

    private Config() {
    }
}
