package com.nestlabs.sdk.rest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FibonacciBackOffTest {

    @Test
    public void testNextInterval_shouldReturnCorrectSequence() {
        FibonacciBackOff backOff = new FibonacciBackOff.Builder()
                .setInitialDelayMillis(1)
                .setMaxDelayMillis(7)
                .build();

        //According to Fibonacci seq. last value should be 8, but we capped it at 7
        long[] values = {1, 1, 2, 3, 5, 7};
        for (long v : values) {
            assertEquals(backOff.nextInterval(), v);
        }

        //Reset and retry
        backOff.reset();
        for (long v : values) {
            assertEquals(backOff.nextInterval(), v);
        }
    }
}
