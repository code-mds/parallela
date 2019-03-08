package serie03;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

class Sensor implements Runnable {
    private final int id;
    private final int threshold;
    //public static AtomicInteger counter = new AtomicInteger();
    public static int counter;

    Sensor(int id, int threshold) {
        this.id = id;
        this.threshold = threshold;
    }

    @Override
    public void run() {
        //while (counter.get() < threshold);
        while (counter < threshold);


        System.out.println(String.format("Sensore%d: Soglia %d superata", id, threshold));
        //counter.set(0);
        counter = 0;
    }
}


public class S3Esercizio2 {
    private final static int NUM_SENSORI = 10;
    public static final int MAX_COUNTER = 120;

    public static void main(final String[] args) {
        final List<Sensor> allSensors = new ArrayList<>();
        final List<Thread> allThread = new ArrayList<>();
        for (int i = 1; i <= NUM_SENSORI; i++) {
            final int threshold = 100*(i+1);
            final Sensor target = new Sensor(i, threshold);
            allSensors.add(target);
            final Thread e = new Thread(target);
            allThread.add(e);
            e.start();
        }

        do {
            int delay = ThreadLocalRandom.current().nextInt(5, 10);
            int randomValue = ThreadLocalRandom.current().nextInt(1, 8);
            //Sensor.counter.set(randomValue);
            Sensor.counter = randomValue;
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        //} while (Sensor.counter.get() > MAX_COUNTER);
        } while (Sensor.counter > MAX_COUNTER);

    }
}
