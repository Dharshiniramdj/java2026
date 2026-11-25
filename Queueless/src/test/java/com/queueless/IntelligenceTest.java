package com.queueless;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class IntelligenceTest {

    @Test
    void testWeightedDurationFallback() {
        assertEquals(5.0, Intelligence.weightedDuration(List.of(), 5));
    }

    @Test
    void testWeightedDurationRecentBias() {
        // Recent is 120s (2m), older is 360s (6m) -> recent weight is higher, average < 4m
        double weighted = Intelligence.weightedDuration(List.of(120, 360), 5);
        assertTrue(weighted < 4.0 && weighted >= 2.0);
    }

    @Test
    void testExponentialMovingAverage() {
        double ema = Intelligence.exponentialMovingAverage(List.of(300, 360, 420), 0.3, 5.0);
        assertTrue(ema > 4.0 && ema < 8.0);
    }

    @Test
    void testWaitTimePredictionInvariants() {
        var pred = Intelligence.predictWaitTime(6, 2, 2, 5.0, false, 12.0);
        assertTrue(pred.estimatedMinutes() > 0);
        assertTrue(pred.lowerBoundMinutes() <= pred.estimatedMinutes());
        assertTrue(pred.upperBoundMinutes() >= pred.estimatedMinutes());
        assertNotNull(pred.explanationFactors());
    }

    @Test
    void testPriorityWaitReduction() {
        var standard = Intelligence.predictWaitTime(10, 1, 2, 5.0, false, 10.0);
        var priority = Intelligence.predictWaitTime(10, 1, 2, 5.0, true, 10.0);
        assertTrue(priority.estimatedMinutes() < standard.estimatedMinutes());
    }

    @Test
    void testMultipleLinearRegression() {
        double[][] X = {
            {2, 1, 5.0, 10.0},
            {4, 1, 5.0, 10.0},
            {6, 2, 5.0, 12.0},
            {8, 2, 5.0, 15.0},
            {10, 3, 5.0, 20.0},
            {12, 3, 5.0, 25.0}
        };
        double[] y = { 10.0, 20.0, 15.0, 21.0, 18.0, 22.0 };

        var ml = new Intelligence.MlRegression();
        ml.train(X, y);
        double pred = ml.predict(6, 2, 5.0, 12.0);
        assertTrue(pred > 0 && pred < 40);

        var eval = ml.evaluate(X, y);
        assertTrue(eval.mae() >= 0);
        assertTrue(eval.rmse() >= 0);
    }

    @Test
    void testDigitalTwinSimulationConcurrency() {
        var sim1 = Intelligence.runDigitalTwinSimulation(20, 0, 1, 5.0, 15, 10, 60);
        var sim3 = Intelligence.runDigitalTwinSimulation(20, 0, 3, 5.0, 15, 10, 60);
        assertTrue(sim3.avgWaitMinutes() < sim1.avgWaitMinutes());
        assertTrue(sim3.throughputPerHour() >= sim1.throughputPerHour());
    }

    @Test
    void testUtilizationCalculation() {
        double rho = Intelligence.calculateUtilization(20.0, 10.0, 2);
        assertEquals(1.0, rho);
    }
}
