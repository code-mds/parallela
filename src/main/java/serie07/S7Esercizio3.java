package serie07;

import java.text.DateFormat;
import java.util.*;

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
    private int lettereDisponibili = TOTALE_LETTERE;
    private int lettereInviate = 0;

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

        System.out.println("START ");
        while (lettereDisponibili > 0 || S7Esercizio3.riceviLettera(nomePersonale) != null) {
            //delay();
            if (lettereDisponibili > 0)
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
        lettereInviate++;
        lettereDisponibili--;
        System.out.printf("%s -> %s [%d/%d] %d\n", nomePersonale, nomeAmico, lettereInviate, TOTALE_LETTERE, lettereDisponibili);
    }
}


public class S7Esercizio3 {
    private final static String amico1 = "Amico1";
    private final static String amico2 = "Amico2";
    final static List<Lettera> casella1 = new ArrayList<>();
    final static List<Lettera> casella2 = new ArrayList<>();

    static void inviaLettera(String mittente, String destinatario) {
        Lettera lettera = new Lettera(mittente, destinatario);
        if (destinatario.equals(amico1)) {
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
        if (destinatario.equals(amico1)) {
            synchronized (casella1) {
                return getLettera(destinatario, casella1);
            }
        } else {
            synchronized (casella2) {
                return getLettera(destinatario, casella2);
            }
        }
    }

    private static Lettera getLettera(String destinatario, List<Lettera> casella) {
        int size = casella.size();
        //System.out.println(destinatario + " lettere:" + size);
        if (size > 0) {
            Lettera l = casella.remove(0);
            System.out.println("get " + l);
            return l;
        }
        System.out.println("RETURN NULL");
        return null;
    }


    public static void main(String[] args) {
        List<Thread> threads = new ArrayList<>();
        threads.add(new Thread(new Amico(amico1, amico2)));
        threads.add(new Thread(new Amico(amico2, amico1)));

        for (final Thread t : threads)
            t.start();

        for (final Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Simulazione finita: " + new Date().toString());

        printCasella1();
        printCasella2();
    }

    static void printCasella2() {
        System.out.println("casella2:");
        for (Lettera l : casella2)
            System.out.println(l);
    }

    static void printCasella1() {
        System.out.println("casella1:");
        for (Lettera l : casella1)
            System.out.println(l);
    }
}
