package serie09;

import java.util.concurrent.CountDownLatch;

public class S9Esercizio1Yield extends Thread {
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
			while (!S9Esercizio1Yield.finished) {
				S9Esercizio1Yield.cnt = ++count;
				System.out.println("sum " + S9Esercizio1Yield.sum);
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
				S9Esercizio1Yield.sum = i;
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
