import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) throws IOException {
        try {
            final URL url = new URL(args[0]);

            int numberOfThreads = Integer.parseInt(args[1]);
            Executor threadPool = Executors.newFixedThreadPool(numberOfThreads);

            int totalRequests = Integer.parseInt(args[2]);
            final AtomicInteger totalSuccesses = new AtomicInteger();

            CompletableFuture[] futures = new CompletableFuture[totalRequests];
            System.out.printf("Sending %d requests with %d threads to %s\n", totalRequests, numberOfThreads, url.toString());

            final long start = System.currentTimeMillis();

            for (int i = 0; i < totalRequests; i++) {
                final int ii = i;
                futures[i] = (CompletableFuture.runAsync(() -> {
                    try {
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        int statusCode = connection.getResponseCode();
                        System.out.printf("Request %d, code %d\n",ii,statusCode);
                        if (statusCode == 200) {
                            totalSuccesses.getAndIncrement();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, threadPool));
            }
            CompletableFuture.allOf(futures).get();
            final long end = System.currentTimeMillis();
            System.out.printf("Send %d requests, succese %d in %d ms\n", totalRequests, totalSuccesses.get(), end - start);

        } catch (Exception e) {
            System.err.println("args must be: {url}, {numberOfThreads}, {totalRequests}");
            e.printStackTrace();
        }
    }
}
