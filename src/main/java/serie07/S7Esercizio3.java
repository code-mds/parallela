package serie07;

import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

final class Lettera {
    private final DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
    private final Date data = new Date();
    private final String mittente;
    private final String destinatario;

    Lettera(String mittente, String destinatario) {
        this.mittente = mittente;
        this.destinatario = destinatario;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s [%s]", mittente, destinatario, formatter.format(data));
    }
}

class Amico implements Runnable {
    private final static int TOTALE_LETTERE = 150;
    private final String nomePersonale;
    private final String nomeAmico;
    AtomicInteger lettereDisponibili = new AtomicInteger(TOTALE_LETTERE);

    Amico(String nomePersonale, String nomeAmico) {
        this.nomePersonale = nomePersonale;
        this.nomeAmico = nomeAmico;
    }

    @Override
    public void run() {
        Random random = new Random();
        int lettereIniziali = 2 + random.nextInt(4);
        for (int i=0; i<lettereIniziali; i++)
            invia();

        while (!S7Esercizio3.completed) {
            Lettera lettera = S7Esercizio3.riceviLettera(nomePersonale);
            delay();

            if (lettera != null && lettereDisponibili.get() > 0)
                invia();
        }
    }

    private void delay() {
        Random random = new Random();
        final int delay = 20 + random.nextInt(31);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void invia() {
        S7Esercizio3.inviaLettera(nomePersonale, nomeAmico);
        int inviate = TOTALE_LETTERE - lettereDisponibili.decrementAndGet();
        System.out.printf("%s -> %s [%d/%d]\n", nomePersonale, nomeAmico, inviate, TOTALE_LETTERE);
    }
}

interface Casella {
    void add(String mittente, String destinatario);
    Lettera get();
    boolean isEmpty();
    String getProprietario();
}

final class CasellaArrayList implements Casella {
    private final String proprietario;
    private final List<Lettera> lettere = new ArrayList<>();

    CasellaArrayList(String proprietario) {
        this.proprietario = proprietario;
    }

    public synchronized void add(String mittente, String destinatario) {
        Lettera lettera = new Lettera(mittente, destinatario);
        lettere.add(lettera);
    }

    public synchronized Lettera get() {
        int size = lettere.size();
        if (size > 0) {
            return lettere.remove(0);
        }
        return null;
    }

    public synchronized boolean isEmpty() {
        return lettere.isEmpty();
    }

    public String getProprietario() {
        return proprietario;
    }
}

final class CasellaConcurrentQueue implements Casella {
    private final String proprietario;
    private final ConcurrentLinkedQueue<Lettera> lettere  = new ConcurrentLinkedQueue<>();

    CasellaConcurrentQueue(String proprietario) {
        this.proprietario = proprietario;
    }

    public void add(String mittente, String destinatario) {
        Lettera lettera = new Lettera(mittente, destinatario);
        lettere.add(lettera);
    }

    public Lettera get() {
        return lettere.poll();
    }

    public boolean isEmpty() {
        return lettere.isEmpty();
    }


    public String getProprietario() {
        return proprietario;
    }
}

public class S7Esercizio3 {
    private final static String AMICO_1 = "Amico1";
    private final static String AMICO_2 = "Amico2";
    volatile static boolean completed = false;

    private final static Casella casella1 = new CasellaConcurrentQueue(AMICO_1); //CasellaArrayList(AMICO_1);
    private final static Casella casella2 = new CasellaConcurrentQueue(AMICO_2); //CasellaArrayList(AMICO_2);

    static void inviaLettera(String mittente, String destinatario) {
        if (destinatario.equals(casella1.getProprietario())) {
            casella1.add(mittente, destinatario);
        } else {
            casella2.add(mittente, destinatario);
        }
    }

    static Lettera riceviLettera(String destinatario) {
        if (destinatario.equals(casella1.getProprietario())) {
            return casella1.get();
        } else {
            return casella2.get();
        }
    }

    public static void main(String[] args) {
        Amico amico1 = new Amico(AMICO_1, AMICO_2);
        Amico amico2 = new Amico(AMICO_2, AMICO_1);

        List<Thread> threads = new ArrayList<>();
        threads.add(new Thread(amico1));
        threads.add(new Thread(amico2));

        for (final Thread t : threads)
            t.start();

        while(!completed) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if( amico1.lettereDisponibili.get() == 0 &&
                amico2.lettereDisponibili.get() == 0 &&
                casella1.isEmpty() &&
                casella2.isEmpty()) {
                    completed = true;
            }
        }

        System.out.println("Simulazione finita: " + new Date().toString());

        System.out.println("casella1 vuota: " + casella1.isEmpty());
        System.out.println("casella2 vuota: " + casella2.isEmpty());
    }
}
