package com.main_group_ekn47.eventlib.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetryPolicy {
    private int maxRetries = 3;
    private long initialDelayMillis = 1000;
    private long maxDelayMillis = 10000;
    private double backoffMultiplier = 2.0;
}