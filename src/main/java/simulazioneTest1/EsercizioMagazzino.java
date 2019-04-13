package simulazioneTest1;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

class Cliente implements Runnable {
    private int fallimenti = 0;
    private int soldi = 20;
    private final int id;

    public Cliente(int id) {
        this.id = id;
    }


    @Override
    public void run() {

        // aspetta l'apertura del magazzino
        while(!EsercizioMagazzino.magazzinoAperto);

        // fino a quando chiude il magazzino
        while(EsercizioMagazzino.magazzinoAperto) {
            int productId = ThreadLocalRandom.current().nextInt(10);

            Product p = EsercizioMagazzino.products.compute(productId, (k,v) -> {
                if(v.getQuantita() != 0 && soldi >= v.getPrezzo()) {
                    fallimenti = 0;
                    System.out.println("Cliente" + id + ": compra " + v + ". Rimangono " + soldi);
                    soldi -= v.getPrezzo();
                    return v.compra();
                }

                fallimenti++;
                return v;
            });

            if(fallimenti == 10) {
                System.out.println("Cliente" + id + ": 10 fallimenti. stop shopping");
                break;
            }

            if(soldi == 0) {
                System.out.println("Cliente" + id + ": finiti soldi");
                break;
            }

            int delay = 1 + ThreadLocalRandom.current().nextInt(5);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public int getSoldi() {
        return soldi;
    }

    @Override
    public String toString() {
        return "Cliente" + id + " soldi: " + soldi;
    }
}

final class Product {
    private final int quantita;
    private final int prezzo;

    public int getQuantita() {
        return quantita;
    }

    public int getPrezzo() {
        return prezzo;
    }

    Product(int quantita, int prezzo) {
        this.quantita = quantita;
        this.prezzo = prezzo;
    }

    Product compra() {
        if(quantita == 0)
            return null;

        return new Product(this.quantita-1, prezzo);
    }

    @Override
    public String toString() {
        return "Quantita:" + quantita + " Prezzo: " + prezzo;
    }
}

public class EsercizioMagazzino {
    volatile static boolean magazzinoAperto = false;
    static ConcurrentMap<Integer, Product> products = new ConcurrentHashMap<>();

    public static void main(final String[] args) {

        for(int i=0; i<10; i++) {
            int quantita = ThreadLocalRandom.current().nextInt(10) + 1;
            int prezzo = ThreadLocalRandom.current().nextInt(5) + 1;

            products.put(i, new Product(quantita, prezzo));
        }

        final List<Cliente> allClients = new ArrayList<>();
        final List<Thread> allThreads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Cliente cliente = new Cliente(i);      // 5 clienti
            allClients.add(cliente);
            allThreads.add(new Thread(cliente));  // 5 thread
        }

        System.out.println("Simulation started");
        allThreads.forEach(Thread::start);

        magazzinoAperto = true;

        for (final Thread t : allThreads) {
            try {
                t.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        for(Cliente c : allClients)
            System.out.println(c);


        for(Integer k : products.keySet())
            System.out.println(k + ": " + products.get(k));

        System.out.println("Simulation finished");
    }


}
