package serie03;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Sensor implements Runnable {
    private final int id;
    private final int threshold;
    private final Counter counter;


    Sensor(int id, int threshold, Counter counter) {
        this.id = id;
        this.threshold = threshold;
        this.counter = counter;
    }

    @Override
    public void run() {
        System.out.println("Sensor" + id + " started (threshold " + threshold + ") ");

        // stop in case it has been reset
        while (!counter.resetAboveThreshold(threshold))
             /*do nothing*/;

        System.out.println("Sensor" + id + " completed");
    }
}

interface Counter {
    // return counter value
    int getValue();
    // add delta to the counter value
    void add(int delta);
    // Set counter value to 0 if counter > threshold
    boolean resetAboveThreshold(int threshold);
}

class CounterNoSync implements Counter {
    private int value;

    public int getValue() { return value; }
    public void add(int delta) { value += delta; }

    @Override
    public boolean resetAboveThreshold(int threshold) {
        if(value > threshold) {
            value = 0;
            return true;
        }

        return false;
    }
}

class CounterVolatile implements Counter {
    private volatile int value;

    public int getValue() { return value; }
    public void add(int delta) {
        // !!! DANGEROUS: non atomic operation
        value += delta;
    }

    @Override
    public boolean resetAboveThreshold(int threshold) {
        // !!! DANGEROUS: after the if another thread could change the value
        if(value > threshold) {
            if(value < threshold)
                System.err.println("Race condition. Other thread changed the counter value!!");

            value = 0;
            return true;
        }

        return false;
    }
}

class CounterAtomic implements Counter {
    private AtomicInteger value = new AtomicInteger();

    public int getValue() { return value.get(); }
    public void add(int delta) { value.getAndAdd(delta); }

    @Override
    public boolean resetAboveThreshold(int threshold) {
        // if above threshold the updateAndGet will return 0
        // compare the return value with 0 to inform the caller that the counter has been reset
        return value.updateAndGet(x -> x > threshold ? 0 : x) == 0;
    }
}

class CounterExplicitLock implements Counter {
    private int value;
    private Lock lock = new ReentrantLock();

    public int getValue() {
        lock.lock();
        try {
            return value;
        } finally {
            lock.unlock();
        }
    }

    public void add(int delta) {
        lock.lock();
        try {
            value += delta;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean resetAboveThreshold(int threshold) {
        lock.lock();
        try {
            if (value > threshold) {
                value = 0;
                return true;
            }

            return false;
        } finally {
            lock.unlock();
        }
    }
}

class CounterReadWriteLock implements Counter {
    private int value;
    private ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private Lock readLock = rwLock.readLock();
    private Lock writeLock = rwLock.writeLock();

    @Override
    public int getValue() {
        readLock.lock();
        try {
            return value;
        } finally {
            readLock.unlock();
        }
    }

    public void add(int delta) {
        writeLock.lock();
        try {
            value += delta;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean resetAboveThreshold(int threshold) {
        writeLock.lock();
        try {
            if (value > threshold) {
                value = 0;
                return true;
            }

            return false;
        } finally {
            writeLock.unlock();
        }
    }
}

public class S3Esercizio2 {
    private final static int THRESHOLD_RANGE = 10;
    private final static int NUM_SENSORS = 10;
    // this value should be greater than the highest threshold so that all Sensor threads are completed
    private static final int MAX_COUNTER = 12 * NUM_SENSORS;


    public static void main(final String[] args) {
        Instant startTime = Instant.now();

        Counter counter = new CounterNoSync();
        //Counter counter = new CounterVolatile();
        //Counter counter = new CounterAtomic();
        //Counter counter = new CounterExplicitLock();
        //Counter counter = new CounterReadWriteLock();

        final List<Sensor> allSensors = new ArrayList<>();
        final List<Thread> allThread = new ArrayList<>();
        for (int i = 1; i <= NUM_SENSORS; i++) {
            final int threshold = THRESHOLD_RANGE * i;
            if(threshold >= MAX_COUNTER) {
                System.err.println(String.format("Sensor%d cannot be created. Max threshold must be < %d", i, MAX_COUNTER));
                break;
            }

            final Sensor target = new Sensor(i, threshold, counter);
            allSensors.add(target);
            final Thread e = new Thread(target);
            allThread.add(e);
            e.start();
        }

        do {
            int delay = ThreadLocalRandom.current().nextInt(5, 11);
            int randomValue = ThreadLocalRandom.current().nextInt(1, 9);

            counter.add(randomValue);

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (counter.getValue() <= MAX_COUNTER);

        Instant endTime = Instant.now();
        long elapsed = Duration.between(startTime, endTime).toMillis();

        System.out.println("Test [" + counter.getClass().getTypeName() +
                "] completed after " + elapsed + "ms . Final counter: " + counter.getValue());

    }
}
