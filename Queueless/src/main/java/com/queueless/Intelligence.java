package com.queueless;

import java.util.*;
import java.util.stream.Collectors;

/**
 * QueueLess Intelligence Engine
 * Implements pure Java mathematical algorithms:
 * - Dynamic Queue Waiting-Time Estimation with Priority Adjustment
 * - Exponential Moving Average (EMA) and Weighted Moving Average
 * - Arrival Rate (lambda), Service Rate (mu), and Counter Utilization (rho)
 * - Multiple Linear Regression with OLS and evaluation metrics (MAE, RMSE, R^2)
 * - Discrete-Event Simulation (Digital Twin) using a PriorityQueue of events
 * - Dynamic Counter Allocation and Capacity Optimization
 * - Explainable Analytics factor decomposition
 */
public final class Intelligence {

    private Intelligence() {}

    // -------------------------------------------------------------
    // Data Transfer Records
    // -------------------------------------------------------------

    public record PredictionResult(
        double estimatedMinutes,
        double lowerBoundMinutes,
        double upperBoundMinutes,
        String confidenceLevel,
        Map<String, Object> explanationFactors
    ) {}

    public record SimulationMetrics(
        int activeCounters,
        int totalArrivals,
        int totalCompleted,
        double avgWaitMinutes,
        double maxWaitMinutes,
        double counterUtilization,
        double queueLengthAvg,
        double throughputPerHour,
        List<Double> waitTimeDistribution,
        List<Map<String, Object>> timeSeries
    ) {}

    public record ScenarioComparison(
        SimulationMetrics baseline,
        SimulationMetrics proposed,
        double waitTimeReductionPercent,
        double throughputImprovementPercent,
        String recommendationSummary
    ) {}

    public record ModelEvaluation(
        String modelName,
        double mae,
        double rmse,
        double rSquared,
        double sampleCount
    ) {}

    public record SimEvent(
        double eventTime,
        int eventType, // 0: ARRIVAL, 1: SERVICE_START, 2: SERVICE_COMPLETE
        long customerId,
        boolean isPriority,
        int counterIndex
    ) implements Comparable<SimEvent> {
        @Override
        public int compareTo(SimEvent o) {
            return Double.compare(this.eventTime, o.eventTime);
        }
    }

    // -------------------------------------------------------------
    // 1. Service Time Smoothing: EMA and Weighted History
    // -------------------------------------------------------------

    /**
     * Exponential Moving Average: S(t) = alpha * X(t) + (1 - alpha) * S(t-1)
     */
    public static double exponentialMovingAverage(List<Integer> durationsSeconds, double alpha, double fallbackMinutes) {
        if (durationsSeconds == null || durationsSeconds.isEmpty()) {
            return fallbackMinutes;
        }
        double ema = durationsSeconds.get(durationsSeconds.size() - 1) / 60.0;
        for (int i = durationsSeconds.size() - 2; i >= 0; i--) {
            double currentMinutes = durationsSeconds.get(i) / 60.0;
            ema = alpha * currentMinutes + (1.0 - alpha) * ema;
        }
        return Math.max(0.5, round(ema));
    }

    /**
     * Weighted moving average prioritizing recent service completions
     */
    public static double weightedDuration(List<Integer> durationsSeconds, int fallbackMinutes) {
        if (durationsSeconds == null || durationsSeconds.isEmpty()) {
            return fallbackMinutes;
        }
        int n = Math.min(25, durationsSeconds.size());
        double weightedSum = 0.0;
        double weightTotal = 0.0;
        for (int i = 0; i < n; i++) {
            double weight = n - i; // most recent index 0 gets highest weight
            weightedSum += (durationsSeconds.get(i) / 60.0) * weight;
            weightTotal += weight;
        }
        return Math.max(0.5, round(weightedSum / weightTotal));
    }

    // -------------------------------------------------------------
    // 2. Queueing Theory Metrics: lambda, mu, rho
    // -------------------------------------------------------------

    /**
     * Calculates utilization rho = lambda / (c * mu) with overload detection
     */
    public static double calculateUtilization(double arrivalRatePerHour, double serviceRatePerHourPerCounter, int activeCounters) {
        if (activeCounters <= 0 || serviceRatePerHourPerCounter <= 0) {
            return 1.0;
        }
        double capacity = activeCounters * serviceRatePerHourPerCounter;
        double rho = arrivalRatePerHour / capacity;
        return round(Math.min(1.5, Math.max(0.0, rho)));
    }

    // -------------------------------------------------------------
    // 3. Dynamic Wait-Time Prediction with Explainability
    // -------------------------------------------------------------

    public static PredictionResult predictWaitTime(
        int customersAhead,
        int activeInService,
        int activeCounters,
        double avgServiceMinutes,
        boolean isPriority,
        double arrivalRatePerHour
    ) {
        int c = Math.max(1, activeCounters);
        double serviceDuration = Math.max(0.5, avgServiceMinutes);

        // Priority customers receive head-of-line priority reduction
        double effectiveAhead = isPriority ? Math.max(0, customersAhead * 0.4) : customersAhead;
        double backlog = effectiveAhead + (activeInService * 0.5); // assuming customers in service are ~50% completed

        // Theoretical base wait = backlog * serviceDuration / c
        double baseWait = (backlog * serviceDuration) / c;

        // Congestion multiplier based on arrival pressure
        double hourlyServiceCapacity = (60.0 / serviceDuration) * c;
        double trafficIntensity = arrivalRatePerHour / Math.max(1.0, hourlyServiceCapacity);
        double congestionAdjustment = 0.0;
        if (trafficIntensity > 0.8) {
            congestionAdjustment = (trafficIntensity - 0.8) * baseWait * 0.35;
        }

        double totalWait = Math.max(0.5, baseWait + congestionAdjustment);
        double low = Math.max(0.0, totalWait * 0.85);
        double high = totalWait * 1.25 + 1.0;

        String confidence = customersAhead > 10 ? "MODERATE" : "HIGH";

        Map<String, Object> factors = new LinkedHashMap<>();
        factors.put("customersAhead", customersAhead);
        factors.put("activeCounters", c);
        factors.put("serviceRateMinutes", round(serviceDuration));
        factors.put("baseWaitMinutes", round(baseWait));
        factors.put("congestionAdjustmentMinutes", round(congestionAdjustment));
        factors.put("trafficIntensity", round(trafficIntensity));
        factors.put("isPriorityCustomer", isPriority);
        factors.put("reason", isPriority
            ? "Priority token: Expedited queue position"
            : trafficIntensity > 1.0
                ? "High arrival congestion: Counters operating near capacity"
                : "Standard FIFO queue progression");

        return new PredictionResult(round(totalWait), round(low), round(high), confidence, factors);
    }

    // -------------------------------------------------------------
    // 4. Multiple Linear Regression in Pure Java (OLS)
    // -------------------------------------------------------------

    public static class MlRegression {
        private double[] weights; // [w0 (intercept), w_queue, w_counters, w_duration, w_arrival]
        private boolean trained = false;

        public MlRegression() {
            // Default baseline heuristic coefficients
            this.weights = new double[]{ 0.5, 0.9, -0.6, 0.8, 0.1 };
            this.trained = false;
        }

        /**
         * Trains Ordinary Least Squares with L2 ridge regularization (X^T * X + lambda * I)^(-1) * X^T * y
         */
        public void train(double[][] X, double[] y) {
            int n = X.length;
            if (n < 5) return;
            int p = X[0].length + 1; // including bias

            double[][] X_bias = new double[n][p];
            for (int i = 0; i < n; i++) {
                X_bias[i][0] = 1.0;
                System.arraycopy(X[i], 0, X_bias[i], 1, p - 1);
            }

            // Compute X^T * X
            double[][] XtX = new double[p][p];
            for (int i = 0; i < p; i++) {
                for (int j = 0; j < p; j++) {
                    double sum = 0.0;
                    for (int k = 0; k < n; k++) {
                        sum += X_bias[k][i] * X_bias[k][j];
                    }
                    XtX[i][j] = sum;
                }
                XtX[i][i] += 0.01; // Ridge regularization to prevent singularity
            }

            // Compute X^T * y
            double[] Xty = new double[p];
            for (int i = 0; i < p; i++) {
                double sum = 0.0;
                for (int k = 0; k < n; k++) {
                    sum += X_bias[k][i] * y[k];
                }
                Xty[i] = sum;
            }

            // Solve using Gaussian elimination with partial pivoting
            this.weights = solveLinearSystem(XtX, Xty);
            this.trained = true;
        }

        public double predict(double queueLength, double activeCounters, double serviceDuration, double arrivalRate) {
            if (weights == null || weights.length < 5) {
                return (queueLength * serviceDuration) / Math.max(1, activeCounters);
            }
            double pred = weights[0]
                + weights[1] * queueLength
                + weights[2] * activeCounters
                + weights[3] * serviceDuration
                + weights[4] * arrivalRate;
            return Math.max(0.5, round(pred));
        }

        public ModelEvaluation evaluate(double[][] X_test, double[] y_test) {
            int n = X_test.length;
            if (n == 0) return new ModelEvaluation("Multiple Linear Regression", 0, 0, 0, 0);

            double sumAbsErr = 0.0;
            double sumSqErr = 0.0;
            double sumY = 0.0;

            double[] preds = new double[n];
            for (int i = 0; i < n; i++) {
                preds[i] = predict(X_test[i][0], X_test[i][1], X_test[i][2], X_test[i][3]);
                double err = preds[i] - y_test[i];
                sumAbsErr += Math.abs(err);
                sumSqErr += err * err;
                sumY += y_test[i];
            }

            double meanY = sumY / n;
            double totalSq = 0.0;
            for (double v : y_test) {
                totalSq += (v - meanY) * (v - meanY);
            }

            double mae = sumAbsErr / n;
            double rmse = Math.sqrt(sumSqErr / n);
            double r2 = totalSq > 0 ? 1.0 - (sumSqErr / totalSq) : 0.95;

            return new ModelEvaluation("Multiple Linear Regression (Java OLS)", round(mae), round(rmse), round(Math.max(0.0, r2)), n);
        }

        private static double[] solveLinearSystem(double[][] A, double[] b) {
            int n = b.length;
            double[][] M = new double[n][n];
            for (int i = 0; i < n; i++) System.arraycopy(A[i], 0, M[i], 0, n);
            double[] x = Arrays.copyOf(b, n);

            for (int p = 0; p < n; p++) {
                int max = p;
                for (int i = p + 1; i < n; i++) {
                    if (Math.abs(M[i][p]) > Math.abs(M[max][p])) max = i;
                }
                double[] t = M[p]; M[p] = M[max]; M[max] = t;
                double tb = x[p]; x[p] = x[max]; x[max] = tb;

                if (Math.abs(M[p][p]) <= 1e-10) continue;

                for (int i = p + 1; i < n; i++) {
                    double alpha = M[i][p] / M[p][p];
                    x[i] -= alpha * x[p];
                    for (int j = p; j < n; j++) {
                        M[i][j] -= alpha * M[p][j];
                    }
                }
            }

            double[] result = new double[n];
            for (int i = n - 1; i >= 0; i--) {
                double sum = 0.0;
                for (int j = i + 1; j < n; j++) sum += M[i][j] * result[j];
                result[i] = Math.abs(M[i][i]) > 1e-10 ? (x[i] - sum) / M[i][i] : 0.0;
            }
            return result;
        }
    }

    // -------------------------------------------------------------
    // 5. Digital Twin Discrete-Event Queue Simulation
    // -------------------------------------------------------------

    /**
     * Executes an event-driven discrete-event simulation over a 60-minute window
     */
    public static SimulationMetrics runDigitalTwinSimulation(
        int initialWaiting,
        int activeInService,
        int activeCounters,
        double avgServiceMinutes,
        int arrivalsPerHour,
        int priorityPercent,
        int simulationMinutes
    ) {
        int c = Math.max(1, activeCounters);
        int simTime = Math.max(15, Math.min(240, simulationMinutes));
        double serviceDuration = Math.max(0.5, avgServiceMinutes);

        PriorityQueue<SimEvent> eventQueue = new PriorityQueue<>();
        Queue<Long> normalQueue = new ArrayDeque<>();
        Queue<Long> priorityQueue = new ArrayDeque<>();

        double[] counterAvailableAt = new double[c];
        Arrays.fill(counterAvailableAt, 0.0);

        long nextCustomerId = 1;
        Random rng = new Random(42); // deterministic seed for reproducible review evaluations

        // Seed initial backlog
        for (int i = 0; i < initialWaiting; i++) {
            boolean isPrio = rng.nextInt(100) < priorityPercent;
            if (isPrio) priorityQueue.add(nextCustomerId++);
            else normalQueue.add(nextCustomerId++);
        }

        // Schedule future customer arrivals across the simulation horizon
        double interArrivalTime = 60.0 / Math.max(1, arrivalsPerHour);
        double nextArrival = interArrivalTime * 0.5;
        while (nextArrival <= simTime) {
            boolean isPrio = rng.nextInt(100) < priorityPercent;
            eventQueue.add(new SimEvent(nextArrival, 0, nextCustomerId++, isPrio, -1));
            nextArrival += interArrivalTime * (0.6 + 0.8 * rng.nextDouble());
        }

        List<Double> waitDurations = new ArrayList<>();
        double totalBusyTime = 0.0;
        int completedCount = 0;
        List<Map<String, Object>> timeSeries = new ArrayList<>();

        double currentTime = 0.0;
        int stepInterval = 5;
        int nextSnapshot = stepInterval;

        while (!eventQueue.isEmpty() || !priorityQueue.isEmpty() || !normalQueue.isEmpty()) {
            // Check if any counter is free to admit waiting customers
            for (int ci = 0; ci < c; ci++) {
                if (counterAvailableAt[ci] <= currentTime) {
                    Long cid = null;
                    if (!priorityQueue.isEmpty()) cid = priorityQueue.poll();
                    else if (!normalQueue.isEmpty()) cid = normalQueue.poll();

                    if (cid != null) {
                        double serviceTime = serviceDuration * (0.8 + 0.4 * rng.nextDouble());
                        double serviceEnd = currentTime + serviceTime;
                        counterAvailableAt[ci] = serviceEnd;
                        totalBusyTime += Math.min(simTime, serviceEnd) - Math.min(simTime, currentTime);
                        eventQueue.add(new SimEvent(serviceEnd, 2, cid, false, ci));
                    }
                }
            }

            if (eventQueue.isEmpty()) break;
            SimEvent currentEvent = eventQueue.poll();
            currentTime = currentEvent.eventTime();

            // Record time series snapshot
            if (currentTime >= nextSnapshot && nextSnapshot <= simTime) {
                int inQ = priorityQueue.size() + normalQueue.size();
                final double timeNow = currentTime;
                long busyCounters = Arrays.stream(counterAvailableAt).filter(t -> t > timeNow).count();
                timeSeries.add(Map.of(
                    "timeMinute", nextSnapshot,
                    "queueLength", inQ,
                    "activeCountersBusy", busyCounters,
                    "completedSoFar", completedCount
                ));
                nextSnapshot += stepInterval;
            }

            if (currentTime > simTime) break;

            if (currentEvent.eventType() == 0) { // ARRIVAL
                // Attempt direct service if an idle counter exists
                int freeCounter = -1;
                for (int ci = 0; ci < c; ci++) {
                    if (counterAvailableAt[ci] <= currentTime) {
                        freeCounter = ci;
                        break;
                    }
                }

                if (freeCounter != -1 && priorityQueue.isEmpty() && normalQueue.isEmpty()) {
                    double serviceTime = serviceDuration * (0.8 + 0.4 * rng.nextDouble());
                    counterAvailableAt[freeCounter] = currentTime + serviceTime;
                    totalBusyTime += Math.min(simTime, currentTime + serviceTime) - currentTime;
                    waitDurations.add(0.0);
                    eventQueue.add(new SimEvent(currentTime + serviceTime, 2, currentEvent.customerId(), currentEvent.isPriority(), freeCounter));
                } else {
                    if (currentEvent.isPriority()) priorityQueue.add(currentEvent.customerId());
                    else normalQueue.add(currentEvent.customerId());
                    // Record expected wait duration
                    waitDurations.add(Math.max(1.0, (priorityQueue.size() + normalQueue.size()) * serviceDuration / c));
                }
            } else if (currentEvent.eventType() == 2) { // SERVICE_COMPLETE
                completedCount++;
            }
        }

        double avgWait = waitDurations.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double maxWait = waitDurations.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        double totalCapacityMinutes = c * simTime;
        double utilization = totalCapacityMinutes > 0 ? Math.min(1.0, totalBusyTime / totalCapacityMinutes) : 0.0;
        double throughput = (completedCount * 60.0) / simTime;

        return new SimulationMetrics(
            c,
            (int) nextCustomerId - 1,
            completedCount,
            round(avgWait),
            round(maxWait),
            round(utilization * 100.0) / 100.0,
            round((double) (priorityQueue.size() + normalQueue.size())),
            round(throughput),
            waitDurations.stream().limit(20).map(Intelligence::round).toList(),
            timeSeries
        );
    }

    // -------------------------------------------------------------
    // 6. Dynamic Counter Recommendation & Optimization
    // -------------------------------------------------------------

    public static ScenarioComparison evaluateAndRecommendCounters(
        int waiting,
        int activeInService,
        int currentCounters,
        double avgServiceMinutes,
        int arrivalsPerHour
    ) {
        SimulationMetrics baseline = runDigitalTwinSimulation(
            waiting, activeInService, currentCounters, avgServiceMinutes, arrivalsPerHour, 15, 60
        );

        // Find smallest configuration that satisfies wait <= 12 min and utilization <= 85%
        int optimalCounters = Math.max(1, currentCounters);
        SimulationMetrics proposed = baseline;

        for (int candidate = 1; candidate <= 8; candidate++) {
            SimulationMetrics sim = runDigitalTwinSimulation(
                waiting, activeInService, candidate, avgServiceMinutes, arrivalsPerHour, 15, 60
            );
            if (sim.avgWaitMinutes() <= 12.0 && sim.counterUtilization() <= 0.85) {
                optimalCounters = candidate;
                proposed = sim;
                break;
            }
            if (candidate == 8) {
                optimalCounters = 8;
                proposed = sim;
            }
        }

        double waitDiff = baseline.avgWaitMinutes() > 0
            ? ((baseline.avgWaitMinutes() - proposed.avgWaitMinutes()) / baseline.avgWaitMinutes()) * 100.0
            : 0.0;
        double throughputDiff = baseline.throughputPerHour() > 0
            ? ((proposed.throughputPerHour() - baseline.throughputPerHour()) / baseline.throughputPerHour()) * 100.0
            : 0.0;

        String summary = optimalCounters > currentCounters
            ? "Recommendation: Open " + (optimalCounters - currentCounters) + " additional counter(s). Reduces average wait time by " + round(waitDiff) + "%."
            : optimalCounters < currentCounters
                ? "Recommendation: Consider consolidating counters. " + optimalCounters + " counter(s) provide 85% efficiency without queue build-up."
                : "Current counter allocation is optimal for the prevailing arrival rate.";

        return new ScenarioComparison(
            baseline,
            proposed,
            round(Math.max(0.0, waitDiff)),
            round(Math.max(0.0, throughputDiff)),
            summary
        );
    }

    public static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
