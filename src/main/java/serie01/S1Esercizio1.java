package serie01;

import java.util.ArrayList;
import java.util.Collection;

public class S1Esercizio1 {
	static class MyRunnable implements Runnable {
		private String name;
		MyRunnable(String name) {
			this.name = name;
		}

		public void run() {
			System.out.println(name + ": " + getFibonacci());
		}
	}

	static class MyThread extends Thread {
		@Override
		public void run() {
			System.out.println(this + ": " + getFibonacci());
		}
	}

	interface ThreadCreator {
		Thread create(String name);
	}

	public static void main(String[] args) {
		testAnonymous();

		testMyThread();

		testMyRunnable();

		testLambda();

	}

	private static void testAnonymous() {
		System.out.println("*********** TEST ANONYMOUS");
		test((name) -> new Thread() {
			@Override
			public void run() {
				/* Stampa risultato */
				System.out.println(this + ": " + getFibonacci());
			}
		});
	}

	private static void testLambda() {
		System.out.println("*********** TEST LAMDA");
		test((name) -> new Thread(() -> System.out.println(name + ": " + getFibonacci())));
	}

	private static void testMyRunnable() {
		System.out.println("*********** TEST RUNNABLE");
		test((name) -> new Thread(new MyRunnable(name)));
	}

	private static void testMyThread() {
		System.out.println("*********** TEST MY THREAD");
		test((name) -> new MyThread());
	}


	private static void test(ThreadCreator tc) {
		Collection<Thread> allThreads = new ArrayList<>();

		/* Creazione dei threads */
		for (int i = 1; i <= 5; i++) {
			System.out.println("Main: creo thread " + i);
			String name = "Thread-" + i;
			Thread t = tc.create(name);
			allThreads.add(t);
		}
		/* Avvio dei threads */
		for (Thread t : allThreads)
			t.start();

		/* Attendo terminazione dei threads */
		for (Thread t : allThreads) {
			try {
				System.out.println("Attendo la terminazione di " + t);
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static long getFibonacci() {
		long fibo1 = 1, fibo2 = 1, fibonacci = 1;
		for (int i = 3; i <= 700; i++) {
			fibonacci = fibo1 + fibo2;
			fibo1 = fibo2;
			fibo2 = fibonacci;
		}
		return fibonacci;
	}
}
