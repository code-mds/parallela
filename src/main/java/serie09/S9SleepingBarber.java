package serie09;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Thread.interrupted;

public class S9SleepingBarber {
    private static BlockingQueue<Integer> clientQueue = new ArrayBlockingQueue<>(3);

    static class Clients implements Runnable {
        static int counter = 0;

        @Override
        public void run() {
            while(!interrupted()) {
                try {
                    if(newClient()) {
                        enter();
                    }
                    else
                        System.out.println("No space in room for client " + counter);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void enter() throws InterruptedException {
            int timeForEnter = ThreadLocalRandom.current().nextInt(80, 161);
            Thread.sleep(timeForEnter);
            System.out.println("Client " + counter + " waiting");
        }

        private boolean newClient() throws InterruptedException {
            int timeForNewClient = ThreadLocalRandom.current().nextInt(450, 701);
            Thread.sleep(timeForNewClient);

            return clientQueue.offer(counter++);
        }
    }

    static class Barber implements Runnable {
        private boolean sleeping = false;

        @Override
        public void run() {
            while(!interrupted()) {
                try {
                    int newClient = waitForNewClient();
                    cutHair(newClient);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void cutHair(int newClient) throws InterruptedException {
            System.out.println("Barber cut hair to: " + newClient);
            int timeForCut = ThreadLocalRandom.current().nextInt(500, 1001);
            Thread.sleep(timeForCut);
        }

        private int waitForNewClient() throws InterruptedException {
            int timeForCheck = ThreadLocalRandom.current().nextInt(50, 101);
            Thread.sleep(timeForCheck);

            return clientQueue.take();
        }
    }

    public static void main(String[] args) {
        Thread clients = new Thread(new Clients());
        Thread barber = new Thread(new Barber());
        barber.start();
        clients.start();

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        barber.interrupt();
        clients.interrupt();
    }
}
