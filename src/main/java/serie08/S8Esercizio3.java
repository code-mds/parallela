package serie08;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class S8Esercizio3 {
    static final Phaser phaser = new Phaser(1);
    static final int NR_MEMBRI = 10;
    static final int NR_SQUADRE = 4;

    public static void main(final String[] args) {
        List<Squadra> squadre = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        for(int idxSquadra=0;idxSquadra<NR_SQUADRE;idxSquadra++) {
            Squadra squadra = new Squadra(idxSquadra);
            for (int idxMembro=0;idxMembro<NR_MEMBRI;idxMembro++) {
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

        squadre.sort(Comparator.comparingInt(Squadra::getTempoTotale));
        squadre.forEach(Squadra::stampaTempo);
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

        public void passaTestimone() {
            lock.lock();
            try{
                if(idxTestimone < squadra.size() -1)
                    System.out.printf("%s: passo testimone t%d a %s\n",
                        squadra.get(idxTestimone), idxTestimone, squadra.get(idxTestimone+1));

                idxTestimone++;
                readyToStart.signalAll();
            } finally {
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
            } finally {
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

        public void stampaTempo() {
            System.out.printf("Squadra%d elapsedTime: %d ms\n", idxSquadra, getTempoTotale());
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
            stampaAttesa();
            phaser.arriveAndAwaitAdvance();
            squadra.syncStart(idxMembro);

            if(idxMembro > 0)
                System.out.printf("%s: testimone ricevuto\n", this);

            corri();
            squadra.passaTestimone();

            phaser.arriveAndDeregister();
        }

        private void stampaAttesa() {
            if(idxMembro == 0) {
                System.out.printf("%s: in attesa del segnale di partenza!\n", this);
            }
            else {
                System.out.printf("%s: in attesa del testimone!\n", this);
            }
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
