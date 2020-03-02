import java.util.concurrent.*;

class Threads {
    private static final BlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();
    private static Runnable task = new Runnable() {
            @Override
            public void run() {
                queue.add(1);
            }
        };

    public static void main(String[] args) {
        final int numberOfRuns = 20000;

        try {
            for (int t = 0; t < 3; t++) {
                System.out.printf("Starting test run %d...\n", t + 1);
                runThreads(numberOfRuns);
                runSmallThreads(numberOfRuns);
                runThreadPool(numberOfRuns, 1);
                runThreadPool(numberOfRuns, -1);
                runSelf(numberOfRuns);
            }
        } catch (InterruptedException e) {
            System.out.printf("Caught %s", e);
        }
    }

    private static void runSmallThreads(int numberOfRuns) throws InterruptedException {
        long start = System.nanoTime();
            for (int i = 0; i < numberOfRuns; i++) {
                new Thread(null, task, "your name here", 1).start();
            }
            for (int i = 0; i < numberOfRuns; i++) {
                queue.take();
            }
        long time = System.nanoTime() - start;
        System.out.printf("Time for a task to complete in a new small Thread:         %6.2f \u00b5s\n",
                          (time / numberOfRuns) / 1000.0);
    }

    private static void runThreads(int numberOfRuns) throws InterruptedException {
        long start = System.nanoTime();
            for (int i = 0; i < numberOfRuns; i++) {
                new Thread(task).start();
            }
            for (int i = 0; i < numberOfRuns; i++) {
                queue.take();
            }
        long time = System.nanoTime() - start;
        System.out.printf("Time for a task to complete in a new Thread:               %6.2f \u00b5s\n",
                          (time / numberOfRuns) / 1000.0);
    }

    private static void runThreadPool(int numberOfRuns, int numberOfThreads) throws InterruptedException {
        long start = System.nanoTime();
        if (numberOfThreads < 0) {
            numberOfThreads = Runtime.getRuntime().availableProcessors();
        }
        ExecutorService es = Executors.newFixedThreadPool(numberOfThreads);
        long initTime = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < numberOfRuns; i++) {
            es.execute(task);
        }
        for (int i = 0; i < numberOfRuns; i++) {
            queue.take();
        }
        long time = System.nanoTime() - start;
        System.out.printf("Time for a task to complete in a thread pool of %d threads: %6.2f \u00b5s",
                          numberOfThreads,
                          (time / numberOfRuns) / 1000.0);

        System.out.printf(" (initialisation time was %.0f \u00b5s)\n", initTime / 1000.0);
        es.shutdown();
    }

    private static void runSelf(int numberOfRuns) throws InterruptedException {
        long start = System.nanoTime();
        for (int i = 0; i < numberOfRuns; i++) {
            task.run();
        }
        for (int i = 0; i < numberOfRuns; i++) {
            queue.take();
        }
        long time = System.nanoTime() - start;
        System.out.printf("Time for a task to complete in the same thread:            %6.2f \u00b5s%n",
                          (time / numberOfRuns) / 1000.0);
    }
}
