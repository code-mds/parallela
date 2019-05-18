package serie09;

import java.util.concurrent.CountDownLatch;

public class S9Esercizio1Priority extends Thread {
	private static final CountDownLatch cdl = new CountDownLatch(2);
	private static volatile boolean finished = false;
	private static volatile int sum = 0;
	private static volatile int cnt = 0;

	public static void main(final String[] args) {
		final Thread thread1 = new Thread(() -> {
			cdl.countDown();
			try {
				cdl.await();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
			int count = 0;
			while (!S9Esercizio1Priority.finished) {
				S9Esercizio1Priority.cnt = ++count;
				System.out.println("sum " + S9Esercizio1Priority.sum);
			}
		});

		final Thread thread2 = new Thread(() -> {
			cdl.countDown();
			try {
				cdl.await();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
			for (int i = 1; i <= 50000; i++) {
				S9Esercizio1Priority.sum = i;
			}
			S9Esercizio1Priority.finished = true;
			System.out.println("cnt " + S9Esercizio1Priority.cnt);
		});

		// Per il thread1 definisco la piu' alta priorita'
		// per permetterne una piu' frequente esecuzione
		thread1.setPriority(Thread.MAX_PRIORITY);

		// Per il thread1 definisco la piu' bassa priorita'
		thread2.setPriority(Thread.MIN_PRIORITY);

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
