package serie02;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Autostrada {
	public int entrate = 0;
	public int uscite = 0;
	public int pedaggi = 0;

	public void incrEntrate(){ entrate++; }
	public void incrUscite(){ uscite++; }
	public void addPedaggio(int pedaggio) { pedaggi += pedaggio; }
}


class AutostradaSyncBlock extends Autostrada {
	public void incrEntrate(){
		synchronized (this) {
			entrate++;
		}
	}
	public void incrUscite(){
		synchronized (this) {
			uscite++;
		}
	}

	public void addPedaggio(int pedaggio) {
		synchronized (this) {
			pedaggi += pedaggio;
		}
	}
}

class AutostradaSyncMethod extends Autostrada {
	synchronized public void incrEntrate(){
		entrate++;
	}
	synchronized public void incrUscite(){
		uscite++;
	}

	synchronized public void addPedaggio(int pedaggio) {
		pedaggi += pedaggio;
	}
}

class AutostradaExplicit extends Autostrada {
	Lock lock = new ReentrantLock();

	public void incrEntrate(){
		lock.lock();
		try{
			entrate++;
		} finally {
			lock.unlock();
		}
	}
	synchronized public void incrUscite(){
		lock.lock();
		try{
			uscite++;
		} finally {
			lock.unlock();
		}
	}

	synchronized public void addPedaggio(int pedaggio) {
		lock.lock();
		try{
			pedaggi += pedaggio;
		} finally {
			lock.unlock();
		}
	}
}

class Automobilista implements Runnable {
	protected final int id;
	protected final Autostrada autostrada;
	private final int delay;
	protected int pedaggiPagati = 0;

	public Automobilista(int id, Autostrada state, int delay) {
		this.autostrada = state;
		this.delay = delay;
		this.id = id;
		this.pedaggiPagati = 0;
		System.out.println("Automobilista " + id + ": creato con " + delay
				+ " ms di percorrenza");
	}

	public int getPedaggiPagati() {
		return pedaggiPagati;
	}

	public int getID() {
		return id;
	}

	@Override
	public void run() {
		System.out.println("Automobilista " + id + ": partito");

		for (int i = 0; i < 500; i++) {
			vaiVersoAutostrada();

			/*
			La modifica dei 3 membri dell'oggetto austrada avviene in modo concorrenziale.
			Il valore dei 3 membri sono indipendenti l'uno dall'altro.
			I 3 incrementi sono stati rifattorizzati come metodi della classe Austrada
			AutostradaSyncBlock, AutostradaSyncMethod e AutostradaExplicit fannno override di questi 3 metodi
			e proteggono la modifica dei valori in modo Thread Safe.
			In questo esercizio non ci sono altri punti di accesso ai 3 membri da proteggere,
			visto che la lettura finale avviene nel Main thread solo dopo che gli altri thread sono joinati.
			 */

			//autostrada.entrate++;
			autostrada.incrEntrate();

			int pedaggioTratta = percorriAutostrada();
			
			//autostrada.uscite++;
			autostrada.incrUscite();

			//autostrada.pedaggi += pedaggioTratta;
			autostrada.addPedaggio(pedaggioTratta);

			pedaggiPagati += pedaggioTratta;
		}
		System.out.println("Automobilista " + id + ": terminato");
	}

	protected void vaiVersoAutostrada() {
		try {
			Thread.sleep(ThreadLocalRandom.current().nextLong(1, 5));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected int percorriAutostrada() {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return ThreadLocalRandom.current().nextInt(10, 20);
	}
}



public class S2Esercizio1 {
	public static void main(final String[] args) {
		final Collection<Automobilista> workers = new ArrayList<Automobilista>();
		final Collection<Thread> threads = new ArrayList<Thread>();
		final Random rand = new Random();

		// Crea l'oggetto condiviso

		//final Autostrada autostrada = new Autostrada();
		//final Autostrada autostrada = new AutostradaSyncBlock();
		//final Autostrada autostrada = new AutostradaSyncMethod();
		final Autostrada autostrada = new AutostradaExplicit();

		for (int i = 0; i < 10; i++) {
			// Genera numero casuale tra 1 e 5 ms
			final int delay = 1 + rand.nextInt(5);
			// Crea nuovo automobilista con oggetto condiviso e delay
			//final Automobilista a = new Automobilista(i, autostrada, delay);
			final Automobilista a = new Automobilista(i, autostrada, delay);
			workers.add(a);
			// Aggiungi alla lista di threads un nuovo thread con il nuovo
			// worker
			threads.add(new Thread(a));
		}

		System.out.println("Simulation started");
		System.out.println("------------------------------------");

		// fa partire tutti i threads
		threads.forEach(Thread::start);
		
		try {
			// Resta in attesa che tutti i threads abbiamo terminato
			for (final Thread t : threads)
				t.join();
		} catch (final InterruptedException e) {
			return;
		}

		// Stampa i risultati
		System.out.println("------------------------------------");
		System.out.println("Simulation finished");

		int totalePedaggiUtenti = 0;
		for (final Automobilista a : workers) {
			int pedaggiPagati = a.getPedaggiPagati();
			totalePedaggiUtenti += pedaggiPagati;
			System.out.println("Automobilista " + a.getID() + " ha pagato " + pedaggiPagati);
		}
		
		System.out.println("Automobilisti totale pedaggi: " + totalePedaggiUtenti);
		System.out.println("Autostrada totale pedaggi   : " + autostrada.pedaggi);
		System.out.println("Autostrada totale entrate :" + autostrada.entrate);
		System.out.println("Autostrada totale uscite  :" + autostrada.uscite);
	}
}
