package com.org.relaytiming.mqtt;

public record R700TagEvent(
        String timestamp,
        TagInventoryEvent tagInventoryEvent
) {
    public record TagInventoryEvent(
            String epcHex,
            int antennaPort,
            int peakRssiCdbm,
            int frequency,
            int transmitPowerCdbm
    ) {}
}
