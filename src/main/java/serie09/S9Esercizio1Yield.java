package serie09;

import java.util.concurrent.CountDownLatch;

public class S9Esercizio1Yield extends Thread {
	private static final CountDownLatch cdl = new CountDownLatch(2);
	private static volatile boolean finished = false;
	private static volatile int sum = 0;
	private static volatile int cnt = 0;

	public static void main(final String[] args) {

		// Il Thread1 esegue chiamate I/O a System.out che cambiano lo stato del
		// thread da Running a Runnable.
		final Thread thread1 = new Thread(() -> {
			cdl.countDown();
			try {
				cdl.await();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
			int count = 0;
			while (!S9Esercizio1Yield.finished) {
				S9Esercizio1Yield.cnt = ++count;
				System.out.println("sum " + S9Esercizio1Yield.sum);
			}
		});

		// Il Thread2 invece ha un elevato consumo di CPU
		// ed utilizza tutto il time-slice messo a disposizione
		final Thread thread2 = new Thread(() -> {
			cdl.countDown();
			try {
				cdl.await();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
			for (int i = 1; i <= 50000; i++) {
				S9Esercizio1Yield.sum = i;

				// chiamando yield suggerisco allo scheduler che posso essere messo in pausa
				// permettendo un'esecuzione piu' frequente del Thread1.
				// Questo permette una visualizzazione a video delle somme piu' fluida.
				Thread.yield();
			}
			S9Esercizio1Yield.finished = true;
			System.out.println("cnt " + S9Esercizio1Yield.cnt);
		});

		thread1.start();
		thread2.start();

		try {
			thread1.join();
			thread2.join();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}
}
