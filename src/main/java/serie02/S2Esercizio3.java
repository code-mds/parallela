package serie02;

import java.util.concurrent.ThreadLocalRandom;

class ContoBancario {
    ContoBancario(int saldo) { this.saldo = saldo; }

    private int saldo;
    public int getSaldo() { return saldo; }

    public void preleva(int richiesta) {
        if(saldo < richiesta)
            throw new IllegalArgumentException("prelievo insufficiente");

        saldo -= richiesta;
    }
}

class UtenteBancario implements Runnable {
    private int contante = 0;
    private final int id;
    private final int delay;
    private final ContoBancario conto;

    UtenteBancario(ContoBancario conto, int id, int delay) {
        this.conto = conto;
        this.id = id;
        this.delay = delay;
    }

    public int getContante() {
        return contante;
    }

    @Override
    public void run() {
        try {
            while(true) {
                int richiesta = ThreadLocalRandom.current().nextInt(5, 50);
                int saldo = conto.getSaldo();
                int prelievo = richiesta;
                if (saldo - prelievo < 0) {
                    prelievo = saldo;
                    contante += prelievo;
                    String msg = String.format("%s: sono riuscito a prelevare solo %d$ invece di %d$",
                            this, prelievo, richiesta);
                    System.out.println(msg);
                    break;
                }
                conto.preleva(prelievo);
                contante += prelievo;
                int nuovoSaldo = conto.getSaldo();
                String msg = String.format("%s: prelevo %d$ dal conto contenente %d$. Nuovo saldo %d$",
                        this, prelievo, saldo, nuovoSaldo);
                System.out.println(msg);

                Thread.sleep(delay);
            }
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
    final static int NUM_UTENTI = 5;

    public static void main(final String[] args) {
        int saldoIniziale = 400;
        ContoBancario conto = new ContoBancario(saldoIniziale);
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
        System.out.println("Saldo iniziale: " + saldoIniziale);
        System.out.println("Saldo conto: " + conto.getSaldo());
        System.out.println("Saldo utenti: " + saldoFinale);
    }
}
