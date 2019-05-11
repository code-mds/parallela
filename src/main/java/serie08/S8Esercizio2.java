package serie08;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class S8Esercizio2 {
    private static class Amico implements Runnable {
        private final String nome;
//        private final ConcurrentLinkedQueue<String> postaEntrata = new ConcurrentLinkedQueue<>();
        private final LinkedBlockingQueue<String> postaEntrata = new LinkedBlockingQueue<>();
        private Amico other;

        Amico(final String nome) {
            this.nome = nome;
        }

        @Override
        public void run() {
            final Random random = new Random();
            final int nextInt = 1 + random.nextInt(5);
            for (int i = 0; i < nextInt; i++) {
                final String msg = "Messaggio" + i + " da " + nome;
                // Mette la lettera nella bucalettere dell'amico
                other.postaEntrata.add(msg);
            }
            int lettere = 0;
            while (true) {
                String inMessge = getMessageBlockingQueue();
                //String inMessge = getMessageConcurrentQueue();

                log("Ricevuto " + inMessge);
                if (lettere == 150) {
                    log("Ho finito le lettere!");
                    break;
                }
                try {
                    Thread.sleep(5 + random.nextInt(46));
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                final String msg = "Risposta" + lettere + " da " + nome;
                // Metti la lettera nella bucalettere dell'amico.
                other.postaEntrata.add(msg);

                lettere++;
            }
        }

        private String getMessageConcurrentQueue() {
            String inMessge;
            do {
                // Controlla la propria bucalettera
                if (postaEntrata.isEmpty()) {
                    inMessge = null;
                    System.out.println("VUOTA....");
                }
                else
                    inMessge = postaEntrata.poll();
            } while (inMessge == null);
            return inMessge;
        }

        // la blocking queue mi permette di evitare di implementare un loop per sapere se sono arrivate lettere
        // Le blocking queue sono ideali quando abbiamo un Thread Producer e un Thread Consumer (come in questo caso)
        // finche' non ci sono lettere il thread e' messo in attesa e risvegliato solo quando ci sono nuovamente lettere
        // cosi' da risparmiare cicli di CPU
        private String getMessageBlockingQueue() {
            String inMessge = null;
            try {
                inMessge = postaEntrata.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return inMessge;
        }

        void setAmico(final Amico other) {
            this.other = other;
        }

        private void log(final String msg) {
            System.out.println(nome + ": " + msg);
        }
    }

    public static void main(final String[] args) {
        final Amico uno = new Amico("Pippo");
        final Amico due = new Amico("Peppa");
        uno.setAmico(due);
        due.setAmico(uno);
        final Thread tUno = new Thread(uno);
        final Thread tDue = new Thread(due);
        System.out.println("Simulation started!");
        tUno.start();
        tDue.start();
        try {
            tUno.join();
            tDue.join();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Simulation finished!");
    }
}