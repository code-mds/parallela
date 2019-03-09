package serie03;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

class S3Worker implements Runnable {
    private final static int ITERATIONS = 10_000;
    private final static int MAX_SUM = 500;

    private final int id;
    private final int[] numbers;

    S3Worker(int id, int[] numbers) {
        this.id = id;
        this.numbers = numbers;
    }

    @Override
    public void run() {
        for (int i=0; i<ITERATIONS; i++) {
            int idx = ThreadLocalRandom.current().nextInt(0, numbers.length);
            int value = ThreadLocalRandom.current().nextInt(10, 51);
            numbers[idx] += value;
            if(numbers[idx] > MAX_SUM)
                numbers[idx] = 0;

            int delay = ThreadLocalRandom.current().nextInt(2, 6);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
public class S3Esercizio3 {
    private final static int ARRAY_LEN = 5;
    private final static int THREAD_COUNT = 10;

    public static void main(final String[] args) {
        int[] numbers = new int[ARRAY_LEN];

        final List<S3Worker> allWorkers = new ArrayList<>();
        final List<Thread> allThreads = new ArrayList<>();

        for (int i=0; i<THREAD_COUNT; i++) {
            final S3Worker worker = new S3Worker(i, numbers);
            allWorkers.add(worker);
            final Thread thread = new Thread(worker);
            allThreads.add(thread);
            thread.start();
        }

        for (Thread thread : allThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
