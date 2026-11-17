import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
public class ResultProcessor {
    static final int TOTAL = 10000;
    static final Path DIR = Paths.get("scores");
    static class Result {
        final int id, marks;
        Result(int id, int marks) { this.id = id; this.marks = marks; }
    }
    static void createFiles() throws IOException {
        Files.createDirectories(DIR);
        for (int i = 1; i <= TOTAL; i++) {
            Path file = DIR.resolve(i + ".txt");
            if (i % 100 == 0) { Files.deleteIfExists(file); continue; }
            Files.writeString(file, i % 75 == 0 ? "INVALID" : String.valueOf(i % 101));
        }
    }
    static Callable<Result> task(int id) {
        return () -> {
            int marks = Integer.parseInt(Files.readString(DIR.resolve(id + ".txt")).trim());
            if (marks < 0 || marks > 100) throw new IllegalArgumentException("Invalid marks");
            return new Result(id, marks);
        };
    }
    static void process(int poolSize) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        ConcurrentHashMap<Integer, Result> success = new ConcurrentHashMap<>();
        ConcurrentHashMap<Integer, String> failure = new ConcurrentHashMap<>();
        List<Future<Result>> futures = new ArrayList<>();
        long start = System.nanoTime();
        try {
            for (int i = 1; i <= TOTAL; i++) futures.add(executor.submit(task(i)));
            for (int i = 0; i < futures.size(); i++) {
                int id = i + 1;
                try { success.put(id, futures.get(i).get()); }
                catch (ExecutionException e) { failure.put(id, String.valueOf(e.getCause())); }
                catch (CancellationException e) { failure.put(id, "Cancelled"); }
            }
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) executor.shutdownNow();
        }
        long passed = success.values().stream().filter(r -> r.marks >= 50).count();
        System.out.println("Threads: " + poolSize + ", Success: " + success.size()
                + ", Failure: " + failure.size() + ", Passed: " + passed);
        System.out.println("Summary correct: " + (success.size() + failure.size() == TOTAL));
        System.out.printf("Time: %.2f ms%n", (System.nanoTime() - start) / 1_000_000.0);
    }
    public static void main(String[] args) throws Exception {
        createFiles();
        for (int p : new int[]{1,2,4,8}) process(p);
    }
}
