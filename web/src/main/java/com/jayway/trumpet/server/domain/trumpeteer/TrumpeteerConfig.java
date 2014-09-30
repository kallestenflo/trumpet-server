package com.jayway.trumpet.server.domain.trumpeteer;

import com.jayway.trumpet.server.domain.subscriber.SubscriberConfig;
import org.aeonbits.owner.Config;

public interface TrumpeteerConfig extends Config {

    static final String MAX_TRUMPET_DISTANCE = "trumpeteer.maxDistance";
    static final String MESSAGE_MAX_LENGTH = "trumpeteer.maxMessageLength";



    @Config.Key(MAX_TRUMPET_DISTANCE)
    @Config.DefaultValue("200")
    int trumpeteerMaxDistance();


    @Config.Key(MESSAGE_MAX_LENGTH)
    @Config.DefaultValue("320")
    int maxMessageLength();

}
