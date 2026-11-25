package com.queueless;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class QueueLessAlgorithmsTest {

    @Test
    void emptyHistoryUsesFallback() {
        assertEquals(7.0, Intelligence.weightedDuration(List.of(), 7));
    }

    @Test
    void newerDurationsHaveMoreWeight() {
        // 240s (4m) and 480s (8m): with weights 2 and 1: (4*2 + 8*1) / 3 = 16/3 = 5.333
        assertEquals(5.3, Intelligence.weightedDuration(List.of(240, 480), 5), 0.1);
    }

    @Test
    void moreCountersReduceWaiting() {
        var a = Intelligence.runDigitalTwinSimulation(18, 0, 1, 6.0, 12, 10, 60);
        var b = Intelligence.runDigitalTwinSimulation(18, 0, 4, 6.0, 12, 10, 60);
        assertTrue(b.avgWaitMinutes() < a.avgWaitMinutes());
    }

    @Test
    void recommendationsStayWithinBounds() {
        var comp = Intelligence.evaluateAndRecommendCounters(20, 0, 2, 6.0, 25);
        assertNotNull(comp.recommendationSummary());
        assertTrue(comp.proposed().activeCounters() >= 1 && comp.proposed().activeCounters() <= 8);
    }
}
