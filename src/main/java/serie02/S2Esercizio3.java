package serie02;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ContoBancario {
    ContoBancario(int saldo) { this.saldo = saldo; }

    private int saldo;
    int getSaldo() { return saldo; }

    void preleva(int richiesta) {
        if(saldo < richiesta)
            throw new IllegalArgumentException("prelievo insufficiente");

        saldo -= richiesta;
    }
}

class UtenteBancario implements Runnable {
    private static final Lock lock = new ReentrantLock();
    private int contante = 0;
    private final int id;
    private final int delay;
    private final ContoBancario conto;

    UtenteBancario(ContoBancario conto, int id, int delay) {
        this.conto = conto;
        this.id = id;
        this.delay = delay;
    }

    int getContante() {
        return contante;
    }

    @Override
    public void run() {
        try {
            boolean saldoInsufficiente = false;
            do {
                int richiesta = ThreadLocalRandom.current().nextInt(5, 50);
                int prelievo = richiesta;
                int saldo;

                lock.lock();
                try {
                    saldo = conto.getSaldo();
                    if (saldo - prelievo < 0) {
                        prelievo = saldo;
                        saldoInsufficiente = true;
                    }
                    conto.preleva(prelievo);

                } finally {
                    lock.unlock();
                }

                contante += prelievo;
                if(saldoInsufficiente) {
                    String msg = String.format("%s: sono riuscito a prelevare solo %d$ invece di %d$",
                            this, prelievo, richiesta);
                    System.out.println(msg);
                } else {
                    String msg = String.format("%s: prelevo %d$ dal conto contenente %d$. Nuovo saldo %d$",
                            this, prelievo, saldo, saldo-prelievo);
                    System.out.println(msg);

                    Thread.sleep(delay);
                }
            } while(!saldoInsufficiente);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Utente" + id;
    }
}

public class S2Esercizio3 {
    private final static int NUM_UTENTI = 5;
    private final static int SALDO_INIZIALE = 4000;

    public static void main(final String[] args) {
        ContoBancario conto = new ContoBancario(SALDO_INIZIALE);
        UtenteBancario[] utenti = new UtenteBancario[NUM_UTENTI];
        Thread[] threads = new Thread[NUM_UTENTI];

        for(int i=0; i<threads.length; i++) {
            int delay = ThreadLocalRandom.current().nextInt(5, 20);
            utenti[i] = new UtenteBancario(conto, i, delay);
            System.out.println("Creato utente: " + utenti[i]);

            threads[i] = new Thread(utenti[i]);
            threads[i].start();
        }

        for(Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        int saldoFinale = 0;
        for(UtenteBancario utente : utenti) {
            saldoFinale += utente.getContante();
        }

        System.out.println("*** Riepilogo Finale ***");
        System.out.println("Saldo conto iniziale: " + SALDO_INIZIALE + "$");
        System.out.println("Saldo conto finale: " + conto.getSaldo() + "$");
        System.out.println("Total saldo utenti: " + saldoFinale + "$");
    }
}
