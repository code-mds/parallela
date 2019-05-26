package serie10;

import jdk.nashorn.internal.codegen.CompilerConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class S10Esercizio1 {
	public static final int NUM_OPERATIONS = 100_000;
	public static final int MATRIX_SIZE = 64;

	public static void main(final String[] args) {
		ExecutorService executorService = Executors.newFixedThreadPool(10);

		final List<Future<Integer>> futures = new ArrayList<>();

		final Random rand = new Random();
		System.out.println("Simulazione iniziata");
		for (int operation = 0; operation < NUM_OPERATIONS; operation++) {
			futures.add(executorService.submit(new MatrixProduct()));
		}
		executorService.shutdown();

		//int idx = 0;
		int max = 0;
		for (final Future<Integer> future : futures) {
			try {
				final Integer result = future.get();
				//System.out.println("Somma_" + idx++ + " = " + result);

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