package serie07;

import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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


public class S7Esercizio3 {
    private final static String AMICO_1 = "Amico1";
    private final static String AMICO_2 = "Amico2";
    volatile static boolean completed = false;

    private final static List<Lettera> casella1 = new ArrayList<>();
    private final static List<Lettera> casella2 = new ArrayList<>();

    static void inviaLettera(String mittente, String destinatario) {
        Lettera lettera = new Lettera(mittente, destinatario);
        if (destinatario.equals(AMICO_1)) {
            synchronized (casella1) {
                casella1.add(lettera);
            }
        } else {
            synchronized (casella2) {
                casella2.add(lettera);
            }
        }
    }

    static Lettera riceviLettera(String destinatario) {
        Lettera lettera;
        if (destinatario.equals(AMICO_1)) {
            synchronized (casella1) {
                lettera = getLettera(destinatario, casella1);
            }
        } else {
            synchronized (casella2) {
                lettera = getLettera(destinatario, casella2);
            }
        }
        return lettera;
    }

    private static Lettera getLettera(String destinatario, List<Lettera> casella) {
        int size = casella.size();
        if (size > 0) {
            return casella.remove(0);
        }
        return null;
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

            boolean isEmpty1;
            boolean isEmpty2;
            if( amico1.lettereDisponibili.get() == 0 &&
                amico2.lettereDisponibili.get() == 0) {
                synchronized (casella1) {
                    isEmpty1 = casella1.isEmpty();
                }
                synchronized (casella2) {
                    isEmpty2 = casella2.isEmpty();
                }

                if(isEmpty1 && isEmpty2)
                    completed = true;
            }
        }

        System.out.println("Simulazione finita: " + new Date().toString());

        System.out.println("casella1:");
        for (Lettera l : casella1)
            System.out.println(l);
        System.out.println("casella2:");
        for (Lettera l : casella2)
            System.out.println(l);
    }
}
