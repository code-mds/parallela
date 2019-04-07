package serie07;

import java.time.DateTimeException;
import java.util.*;

class Lettera {
    private final Date data = new Date();
    private final String mittente;
    private final String destinatario;

    Lettera(String mittente, String destinatario) {
        this.mittente = mittente;
        this.destinatario = destinatario;
    }

    @Override
    public String toString() {
        return String.format("Mittente: %s, Destinatario: %s, Data: %s", mittente, destinatario, data);
    }
}

class Amico implements Runnable {
    private final String nome;
    private final String nomeAmico;
    private int lettereDisponibili = 150;

    public Amico(String nome, String nomeAmico) {
        this.nome = nome;
        this.nomeAmico = nomeAmico;
    }

    @Override
    public void run() {
        Random random = new Random();

        int lettere = 2 + random.nextInt(4);
        inviaLettere(lettere);

        while(lettereDisponibili > 0) {

            try {
                final int delay = 20 + random.nextInt(31);
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            rispondi();
        }
    }

    private void inviaLettere(int lettere) {
        for (int i=0; i<lettere && lettereDisponibili > 0; i++) {
            S7Esercizio3.inviaLettera(nome, nomeAmico);
            lettereDisponibili--;
        }
    }

    private void rispondi() {
        List<Lettera> lettere = S7Esercizio3.riceviLettere(nome);
        inviaLettere(lettere.size());
    }

}

public class S7Esercizio3 {
    private final static Map<String, List<Lettera>> caselleDiPosta = new HashMap<>();
    private final static String amico1 = "Amico1";
    private final static String amico2 = "Amico2";

    static void inviaLettera(String mittente, String destinatario) {
        Lettera lettara = new Lettera(mittente, destinatario);
        System.out.println("INVIO: " + lettara);

        S7Esercizio3.caselleDiPosta.get(destinatario).add(lettara);
    }

    static List<Lettera> riceviLettere(String destinatario) {
        List<Lettera> lettere = new ArrayList<>(caselleDiPosta.get(destinatario));

        System.out.println(destinatario + " riceve " + lettere.size()+ " lettere");

        caselleDiPosta.get(destinatario).clear();
        return lettere;
    }


    public static void main(String[] args) {
        List<Thread> threads = new ArrayList<>();

        threads.add(new Thread(new Amico(amico1, amico2)));
        caselleDiPosta.put(amico1, new ArrayList<>());

        threads.add(new Thread(new Amico(amico2, amico1)));
        caselleDiPosta.put(amico2, new ArrayList<>());


        for(final Thread t : threads) {
            t.start();
        }

        for(final Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Simulazione finita");

    }
}
