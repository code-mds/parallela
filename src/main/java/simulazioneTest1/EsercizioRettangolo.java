package simulazioneTest1;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

// rendo la classe Immutable
final class Rectangle {
    private final int x1;
    private final int y1;
    private final int x2;
    private final int y2;

    public Rectangle(final int newX1, final int newY1, final int newX2, final int newY2) {
        x1 = newX1;
        y1 = newY1;
        x2 = newX2;
        y2 = newY2;
    }

    public int getX1() {
        return x1;
    }

    public int getX2() {
        return x2;
    }

    public int getY1() {
        return y1;
    }

    public int getY2() {
        return y2;
    }

    // MDS
    // new method resize that return a new new instance of Rectangle
    // with the previous x1,y1 and the new size
    public Rectangle resize(int deltaX2, int deltaY2) {
        // calcola nuove coordinate x2 e y2
        final int newX2 = getX2() + deltaX2;
        final int newY2 = getY2() + deltaY2;

        // La trasformazione non deve rendere il rettangolo una linea, un
        // punto o avere le coordinate del secondo punto 'dietro' al punto
        // iniziale
        final boolean isLine = (getX1() == newX2) || (getY1() == newY2);
        final boolean isPoint = (getX1() == newX2) && (getY1() == newY2);
        final boolean isNegative = (getX1() > newX2) || (getY1() > newY2);
        if(isLine || isPoint || isNegative)
            return null;

        return new Rectangle(this.x1, this.y1, newX2, newY2);
    }

// MDS
//    public void setX2(final int newX2) {
//        this.x2 = newX2;
//    }
//
//    public void setY2(final int newY2) {
//        this.y2 = newY2;
//    }

    @Override
    public String toString() {
        return "[" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + "]";
    }
}

class Resizer implements Runnable {
    @Override
    public void run() {
        final Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            try {
                Thread.sleep(random.nextInt(3) + 2);
            } catch (final InterruptedException e) { }


            Rectangle oldRect, newRect;
            do {
                // genera variazione per punti tra -2 e 2
                final int deltaX2 = random.nextInt(5) - 2;
                final int deltaY2 = random.nextInt(5) - 2;

                oldRect = EsercizioRettangolo.rect.get();
                newRect = oldRect.resize(deltaX2, deltaY2);
            } while (newRect == null || !EsercizioRettangolo.rect.compareAndSet(oldRect, newRect));
            System.out.println(EsercizioRettangolo.rect);
        }
    }
}

/**
 * Programma che simula variazioni continue delle dimensioni di un rettangolo
 */
public class EsercizioRettangolo {
    // shared object: missing volatile
    //static volatile Rectangle rect = new Rectangle(10, 10, 20, 20);
    static AtomicReference<Rectangle> rect = new AtomicReference<>(new Rectangle(10, 10, 20, 20));

    public static void main(final String[] args) {
        final List<Thread> allThreads = new ArrayList<>();
        for (int i = 0; i < 5; i++)
            allThreads.add(new Thread(new Resizer()));  // 5 thread -> 5 resizer

        System.out.println("Simulation started");
        allThreads.forEach(Thread::start);
        for (final Thread t : allThreads) {
            try {
                t.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Simulation finished");
    }
}

/*
Nel programma ci sono 5 thread che provano a modificare un oggetto condiviso Rectangle cambiandone le dimensioni.
L'oggetto rect viene istanziato prima che partano i thread, tutti vedono la stessa istanza.

La classe rect ha due metodi setX2 e setY2 che devono essere eseguiti entrambi in maniera atomica per avere
uno stato consistenze dell'oggetto
       EsercizioRettangolo.rect.setX2(newX2);
       EsercizioRettangolo.rect.setY2(newY2);

La classe Resizer esegue compound actions (check then act) che devono essere eseguite in maniera atomica.
Le possibili soluzioni sono:
- una sincronizzazione esplicita sull'oggetto condiviso rect
- rendere l'oggetto rect immutable e usare la tecnica di stack confinment

La piu' efficace e' quella di non usare lock, ma trasformare Rect in immutable e usare lo stack confinment
 */