package br.com.kauanallyson.kcrm.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("kcrm.rate-limit")
public record RateLimitProperties(Limit auth, Limit api) {

    public Limit limitFor(RateLimitBucket bucket) {
        return switch (bucket) {
            case AUTH -> auth;
            case API -> api;
        };
    }

    public record Limit(int requests, Duration period) {
    }
}
