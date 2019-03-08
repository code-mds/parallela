package serie03;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

class Sensor implements Runnable {
    private final int id;
    private final int threshold;
    //public static int counter;
    //public static volatile int counter;
    public static AtomicInteger counter = new AtomicInteger();


    Sensor(int id, int threshold) {
        this.id = id;
        this.threshold = threshold;
    }

    @Override
    public void run() {
        System.out.println("Sensor" + id + " started (threshold " + threshold + ") ");

        //while (counter < threshold);
        while (counter.get() < threshold);


        System.out.println(String.format("Sensor%d: Above threshold %d", id, threshold));
        //counter = 0;
        counter.set(0);
    }
}


public class S3Esercizio2 {
    private final static int THRESHOLD_RANGE = 10;
    private final static int NUM_SENSORI = 10;
    public static final int MAX_COUNTER = 120;

    public static void main(final String[] args) {
        final List<Sensor> allSensors = new ArrayList<>();
        final List<Thread> allThread = new ArrayList<>();
        for (int i = 1; i <= NUM_SENSORI; i++) {
            final int threshold = THRESHOLD_RANGE * i;
            final Sensor target = new Sensor(i, threshold);
            allSensors.add(target);
            final Thread e = new Thread(target);
            allThread.add(e);
            e.start();
        }

        do {
            //System.out.println(String.format("Counter %d", Sensor.counter));

            int delay = ThreadLocalRandom.current().nextInt(5, 10);
            int randomValue = ThreadLocalRandom.current().nextInt(1, 8);
            Sensor.counter.getAndAdd(randomValue);
            //Sensor.counter += randomValue;
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        //} while (Sensor.counter < MAX_COUNTER);
        } while (Sensor.counter.get() < MAX_COUNTER);
        System.out.println("Main thread completed");
    }
}
