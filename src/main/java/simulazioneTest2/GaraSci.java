package simulazioneTest2;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

class Sciatore implements Runnable {
    final int idxSciatore;
    final CountDownLatch latch;
    final List<Ispettore> ispettori;

    Sciatore(int idxSciatore, CountDownLatch latch, List<Ispettore> ispettori) {
        this.idxSciatore = idxSciatore;
        this.latch = latch;
        this.ispettori = ispettori;
    }

    @Override
    public void run() {
        System.out.printf("%s pronto\n", this);
        latch.countDown();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        System.out.printf("%s partito\n", this);

        for (int i = 0; i < GaraSci.NR_TAPPE; i++) {

            Instant start = Instant.now();
            // Wait before next
            try {
                Thread.sleep(ThreadLocalRandom.current().nextLong(4, 9));
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Instant end = Instant.now();

            long ms = Duration.between(start, end).toMillis();
            ispettori.get(i).addTempo(idxSciatore, ms);
        }

    }

    @Override
    public String toString() {
        return "Sciatore" + idxSciatore;
    }
}

final class TempoParziale {
    final int id;
    final long ms;

    TempoParziale(int id, long ms) {
        this.id = id;
        this.ms = ms;
    }

    @Override
    public String toString() {
        return String.format("Sciatore%d tempo=%d", id, ms);
    }
}

class Ispettore {
    ConcurrentLinkedQueue<TempoParziale> tempi = new ConcurrentLinkedQueue<>();
    final int idxTappa;
    public Ispettore(int idxTappa) {
        this.idxTappa = idxTappa;
    }

    void addTempo(int idxSciatore, long ms) {
        tempi.add(new TempoParziale(idxSciatore, ms));

        //System.out.printf("Sciatore%d Tappa%d tempo=%d pos=%d\n", idxSciatore, idxTappa, ms, tempi.size());
        if (tempi.size() == GaraSci.NR_SCIATORI) {

            System.out.println("********* Tempi tappa " + idxTappa);
            for (TempoParziale t : tempi)
                System.out.println(t);
        }

    }
}


public class GaraSci {
    static final int NR_SCIATORI = 5;
    static final int NR_TAPPE = 6;

    public static void main(String[] args) {
        CountDownLatch latch = new CountDownLatch(NR_SCIATORI);

        List<Ispettore> ispettori = new ArrayList<>();
        for (int i = 0; i < NR_TAPPE; i++) {
            ispettori.add(new Ispettore(i));
        }

        List<Thread> sciatori = new ArrayList<>();
        for (int i = 0; i < NR_SCIATORI; i++) {
            sciatori.add(new Thread(new Sciatore(i, latch, ispettori)));
        }
        sciatori.forEach(Thread::start);

        try {
            latch.await();
            System.out.println("GOOOO");
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        for (Thread t : sciatori) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("******* FINE GARA");
    }
}
