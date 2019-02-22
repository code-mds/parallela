package serie01;

import java.time.Duration;
import java.time.Instant;

public class S1Esercizio2 {
    static class Sleeper implements Runnable {
        private final long sleepingTime;
        private final int threadNumber;

        Sleeper(long sleepingTime, int threadNumber) {
            this.sleepingTime = sleepingTime;
            this.threadNumber = threadNumber;
        }


        @Override
        public void run() {
            try {
                Instant startTime = Instant.now();
                Thread.sleep(sleepingTime);
                Instant endTime = Instant.now();
                long elapsed = Duration.between(startTime, endTime).toMillis();

                System.out.println("Thread " + threadNumber + " risveglio dopo " + elapsed + "ms");
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted while sleeping" + e);
            }
        }
    }

    public static void main(String [] args) {

        Thread[] threads = new Thread[2];
        for (int i=0; i<threads.length; i++){
            long ms = calcRandomSleepingTime();
            threads[i] = new Thread(new Sleeper(ms, i));
        }

        System.out.println("Partono tutti i threads.");
        for (Thread thread : threads) {
            thread.start();
        }
        System.out.println("In attesa che i threads abbiano terminato.");
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Tutti i threads hanno terminato.");
    }

    private static long calcRandomSleepingTime() {
        return (long) (1500 + (int)(Math.random() * 500.0));
    }
}
