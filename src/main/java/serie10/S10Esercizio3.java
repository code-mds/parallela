package serie10;

import java.util.concurrent.*;

public class S10Esercizio3 {
	public static final int NUM_OPERATIONS = 100_000;
	public static final int MATRIX_SIZE = 64;

	public static void main(final String[] args) {
		ExecutorService executorService = Executors.newFixedThreadPool(10);

		final CompletionService<Integer> cs = new ExecutorCompletionService<>(executorService);
		System.out.println("Simulazione iniziata E3");
		for (int operation = 0; operation < NUM_OPERATIONS; operation++) {
			cs.submit(new MatrixProduct());
		}
		executorService.shutdown();

		int max = 0;
		for (int operation = 0; operation < NUM_OPERATIONS; operation++) {
			try {
				final Future<Integer> future = cs.take();
				final Integer result = future.get();
				//System.out.println("Somma_" + operation++ + " = " + result);

				max = Math.max(max, result);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Somma piu' grande: " + max);

		System.out.println("Simulazione terminata");
	}

	final static class MatrixProduct implements Callable<Integer> {
		@Override
		public Integer call() {
			ThreadLocalRandom rand = ThreadLocalRandom.current();
			// Crea matrici
			final int[][] m0 = new int[MATRIX_SIZE][MATRIX_SIZE];
			final int[][] m1 = new int[MATRIX_SIZE][MATRIX_SIZE];
			final int[][] result = new int[MATRIX_SIZE][MATRIX_SIZE];

			// Inizializza gli array con numeri random
			for (int i = 0; i < MATRIX_SIZE; i++)
				for (int j = 0; j < MATRIX_SIZE; j++) {
					m0[i][j] = rand.nextInt(10);
					m1[i][j] = rand.nextInt(10);
				}

			int sum = 0;

			// Moltiplica matrici
			for (int i = 0; i < m0[0].length; i++)
				for (int j = 0; j < m1.length; j++)
					for (int k = 0; k < m0.length; k++) {
						result[i][j] += m0[i][k] * m1[k][j];
						sum += result[i][j];
					}

			return sum;
		}
	}

}