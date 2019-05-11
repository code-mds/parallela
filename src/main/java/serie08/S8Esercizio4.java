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
    private final static boolean CONDITION_IMPL = true;


    public static void main(String[] args) {
        List<Thread> threads = new ArrayList<>();
        for (int i=0; i< NR_FANTINI; i++)
            threads.add(new Thread(Fantino.build(i)));

        threads.forEach(Thread::start);
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private abstract static class Fantino implements Runnable {
        private final int idx;
        Fantino(int idx) {
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

        protected abstract void waitAll();

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

        static Fantino build(int idx) {
            return CONDITION_IMPL ?
                    new FantinoCondition(idx):
                    new FantinoSynchronizer(idx);
        }
    }

    private static final class FantinoCondition extends Fantino {
        private static final Lock lock = new ReentrantLock();
        private static final Condition condition = lock.newCondition();
        private static int inLinea = 0;

        FantinoCondition(int idx) {
            super(idx);
        }

        protected void waitAll() {
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
    }

    private static class FantinoSynchronizer extends Fantino {
        private static final Phaser phaser = new Phaser();

        FantinoSynchronizer(int idx) {
            super(idx);
            phaser.register();
        }

        protected void waitAll() {
            phaser.arriveAndAwaitAdvance();
        }
    }

}
