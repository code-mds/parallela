package serie08;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class S8Esercizio4 {
    private final static int NR_FANTINI = 10;

    public static void main(String[] args) {
        List<Thread> threads = new ArrayList<>();
        for (int i=0; i< NR_FANTINI; i++)
            //threads.add(new Thread(new FantinoCondition(i)));
            threads.add(new Thread(new FantinoSynchronizer(i)));

        threads.forEach(Thread::start);
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private static class FantinoCondition implements Runnable {
        private static final Lock lock = new ReentrantLock();
        private static final Condition condition = lock.newCondition();
        private static int inLinea = 0;

        private final int idx;
        FantinoCondition(int idx) {
            this.idx = idx;
        }

        @Override
        public void run() {
            arrivoLinea();
            Instant startTime = Instant.now();
            waitAll();
            long elapsed = Duration.between(startTime, Instant.now()).toMillis();
            System.out.printf("%s: ha atteso %d ms\n", this, elapsed);
        }

        private void waitAll() {
            lock.lock();
            try {
                inLinea++;
                while(inLinea < NR_FANTINI) {
                    condition.await();
                }
                condition.signalAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

        private void arrivoLinea() {
            int delay = ThreadLocalRandom.current().nextInt(1000, 1051);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(this + ": arrivo alla linea di partenza");
        }

        @Override
        public String toString() {
            return "Fantino" + idx;
        }
    }

    private static class FantinoSynchronizer implements Runnable {
        private static final Phaser phaser = new Phaser();

        private final int idx;
        FantinoSynchronizer(int idx) {
            this.idx = idx;
            phaser.register();
        }

        @Override
        public void run() {
            arrivoLinea();
            Instant startTime = Instant.now();
            waitAll();
            long elapsed = Duration.between(startTime, Instant.now()).toMillis();
            System.out.printf("%s: ha atteso %d ms\n", this, elapsed);

            phaser.arriveAndDeregister();
        }

        private void waitAll() {
            phaser.arriveAndAwaitAdvance();
        }

        private void arrivoLinea() {
            int delay = ThreadLocalRandom.current().nextInt(1000, 1051);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(this + ": arrivo alla linea di partenza");
        }

        @Override
        public String toString() {
            return "Fantino" + idx;
        }
    }

}
