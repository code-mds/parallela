package serie08;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class S8Esercizio3 {
    static final Phaser phaser = new Phaser(1);
    public static void main(final String[] args) {
        List<Squadra> squadre = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        for(int idxSquadra=0;idxSquadra<4;idxSquadra++) {
            Squadra squadra = new Squadra(idxSquadra);
            for (int idxMembro=0;idxMembro<2;idxMembro++) {
                Corridore corridore = new Corridore(squadra, idxMembro);
                squadra.add(corridore);
                threads.add(new Thread(corridore));
            }
            squadre.add(squadra);
        }

        threads.forEach(Thread::start);
        phaser.arriveAndAwaitAdvance();

        System.out.println("Pronti... Partenza... Via!");
        phaser.arriveAndAwaitAdvance();
        System.out.println("---------------------------");
        System.out.println("Classifica finale:");
        for(int i = 0; i<4; i++) {
            int tempoSquadra = squadre.get(i).getTempoTotale();
            System.out.printf("Squadra%d elapsedTime: %d ms\n", i, tempoSquadra);
        }

    }

    private static class Squadra {
        private final Lock lock = new ReentrantLock();
        private final Condition readyToStart = lock.newCondition();

        private List<Corridore> squadra = new ArrayList<>();
        private int idxSquadra;
        private int idxTestimone;

        public Squadra(int idxSquadra) {
            this.idxSquadra = idxSquadra;
            this.idxTestimone = 0;
        }

        public void passaTestimone(Corridore c) {
            lock.lock();
            try{
                System.out.printf("%s: passo testimone!\n", c);
                idxTestimone++;
                readyToStart.signalAll();
            } catch(Exception e) {
                e.printStackTrace();
                lock.unlock();
            }
        }

        public void syncStart(int idxMembro) {
            lock.lock();
            try{
                while(idxTestimone != idxMembro) {
                    try {
                        readyToStart.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
                lock.unlock();
            }
        }

        public int getIdxSquadra() {
            return idxSquadra;
        }

        void add(Corridore corridore) {
            squadra.add(corridore);
        }

        int getTempoTotale() {
            return squadra.stream().mapToInt(Corridore::getTempoCorsa).sum();
        }

    }

    private static class Corridore implements Runnable {
        private final int idxMembro;
        private Squadra squadra;
        private int tempoCorsa;


        public Corridore(Squadra squadra, int idxMembro) {
            this.idxMembro = idxMembro;
            this.squadra = squadra;
            phaser.register();
        }

        @Override
        public void run() {
            if(idxMembro == 0) {
                System.out.printf("%s: in attesa del segnale di partenza!\n", this);
            }
            else {
                System.out.printf("%s: in attesa del testimone!\n", this);
            }
            phaser.arriveAndAwaitAdvance();

            squadra.syncStart(idxMembro);

            corri();
            squadra.passaTestimone(this);
            System.out.println(this + " arrivato");
            phaser.arriveAndDeregister();
        }

        private void corri() {
            tempoCorsa = ThreadLocalRandom.current().nextInt(100, 150);
            System.out.printf("%s: corro per %d ms\n", this, tempoCorsa);
            try {
                Thread.sleep(tempoCorsa);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String toString() {
            return String.format("Corridore%d_Squadra%d", idxMembro, squadra.getIdxSquadra());
        }

        public int getTempoCorsa() {
            return tempoCorsa;
        }
    }

}
