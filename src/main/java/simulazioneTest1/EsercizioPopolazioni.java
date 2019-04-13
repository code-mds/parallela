package simulazioneTest1;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Classe che simula periodi di carestia per i paesi
 */
class Carestia implements Runnable {
    @Override
    public void run() {
        final Random random = new Random();
        // ad ogni giro introduce carestia per un determinato paese scelto a caso
        for (int i = 0; i < 50; i++) {
            final int popoloScelto = random.nextInt(EsercizioPopolazioni.popolazione.length);

            // 0.1 .. 0.7
            final double fattoreDecimazione = random.nextDouble() * 0.6 + 0.1;
            final long popolazioneAggiornata;

            // decima la popolazione
            //EsercizioPopolazioni.lock.lock(); //MDS
            long oldVal, newVal;
            do {
                 oldVal = EsercizioPopolazioni.popolazione[popoloScelto].get();
                 newVal = (long)(oldVal * fattoreDecimazione);
            } while(!EsercizioPopolazioni.popolazione[popoloScelto].compareAndSet(oldVal, newVal));
            //EsercizioPopolazioni.popolazione[popoloScelto] *= fattoreDecimazione;

            popolazioneAggiornata = EsercizioPopolazioni.popolazione[popoloScelto].get();
            //EsercizioPopolazioni.lock.unlock();   //MDS

            System.out.println("Carestia: Popolazione " + popoloScelto + " diminuita a " + popolazioneAggiornata
                    + " del fattore " + fattoreDecimazione);

            try {
                // pausa fra un periodo di carestia e l'altro
                Thread.sleep(100);
            } catch (final InterruptedException e) {
            }
        }
    }
}

/**
 * Classe che simula periodi di prosperita per i paesi
 */
class Prosperita implements Runnable {
    @Override
    public void run() {
        final Random random = new Random();
        // ad ogni giro introduce prosperità per un determinato paese scelto a caso
        for (int i = 0; i < 100; i++) {
            final int popoloScelto = random.nextInt(EsercizioPopolazioni.popolazione.length);

            final double fattoreCrescita = random.nextDouble() * 0.55 + 1.05;
            final long popolazioneAggiornata;

            // incrementa la popolazione
            //EsercizioPopolazioni.lock.lock(); //MDS
            long oldVal, newVal;
            do {
                oldVal = EsercizioPopolazioni.popolazione[popoloScelto].get();
                newVal = (long)(oldVal * fattoreCrescita);
            } while(!EsercizioPopolazioni.popolazione[popoloScelto].compareAndSet(oldVal, newVal));
            popolazioneAggiornata = newVal; //MDS

//            EsercizioPopolazioni.popolazione[popoloScelto] *= fattoreCrescita;        //MDS
//            popolazioneAggiornata = EsercizioPopolazioni.popolazione[popoloScelto];   //MDS
//            EsercizioPopolazioni.lock.unlock();                                       //MDS

            System.out.println("Prosperita: Popolazione " + popoloScelto + " cresciuta a " + popolazioneAggiornata
                    + " del fattore " + fattoreCrescita);

            try {
                // pausa fra un periodo di prosperità e l'altro
                Thread.sleep(50);
            } catch (final InterruptedException e) {
            }
        }
    }
}

/**
 * Programma che simula la variazione demografica di 5 paesi
 */
public class EsercizioPopolazioni {
    static volatile AtomicLong popolazione[] = new AtomicLong[5];   //MDS

//    static volatile long popolazione[] = new long[5]; //MDS
//    static ReentrantLock lock = new ReentrantLock();  //MDS

    public static void main(final String[] args) {
        // la popolazione iniziale è di 1000 abitanti per ogni paese
        for (int i = 0; i < 5; i++) {
            popolazione[i] = new AtomicLong(1000);  //MDS
            //popolazione[i] = 1000;   //MDS
        }

        final List<Thread> allThreads = new ArrayList<>();
        allThreads.add(new Thread(new Prosperita()));
        allThreads.add(new Thread(new Prosperita()));
        allThreads.add(new Thread(new Carestia()));

        System.out.println("Simulation started");
        for (final Thread t : allThreads)
            t.start();

        for (final Thread t : allThreads)
            try {
                t.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        System.out.println("Simulation finished");
    }
}