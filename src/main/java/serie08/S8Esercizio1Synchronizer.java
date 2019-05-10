package serie08;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Phaser;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class AdderSynchronizer implements Runnable {

	private final int idx;

	AdderSynchronizer(int idx) {
		this.idx = idx;
		S8Esercizio1Synchronizer.phaser.register();
	}

	@Override
	public void run() {
		System.out.println(idx + ") SOMMO RIGHE");
		int sum = S8Esercizio1Synchronizer.sumRow(idx);

		S8Esercizio1Synchronizer.rowSum[idx] = sum;
		S8Esercizio1Synchronizer.phaser.arriveAndAwaitAdvance();

		System.out.println(idx + ") SOMMO COLONNE");
		sum = S8Esercizio1Synchronizer.sumColumn(idx);

		S8Esercizio1Synchronizer.colSum[idx] = sum;
		S8Esercizio1Synchronizer.phaser.arriveAndDeregister();
	}
}

public class S8Esercizio1Synchronizer {
	final static int[][] matrix = new int[10][10];
	final static int[] rowSum = new int[matrix.length];
	final static int[] colSum = new int[matrix[0].length];

	final static Phaser phaser = new Phaser(1);

	public static void main(String[] args) {
		// Inizializza matrice con valori random
		initMatrix();

		// Stampa matrice
		System.out.println("Matrice:");
		printMatrix();

		List<Thread> threads = new ArrayList<>();
		for (int i=0;i<matrix.length; i++) {
			threads.add(new Thread(new AdderSynchronizer(i)));
		}

		threads.forEach(Thread::start);

		phaser.arriveAndAwaitAdvance();

		// Stampa somma delle righe
		System.out.println("Somme delle righe:");
		printArray(rowSum);
		stampaTotaleRighe();

		phaser.arriveAndAwaitAdvance();

		// Stampa somma delle colonne
		System.out.println("Somme delle colonne:");
		printArray(colSum);
		stampaTotaleColonne();

	}

	private static void stampaTotaleColonne() {
		int totale = 0;
		for (int col = 0; col < colSum.length; col++) {
			totale += colSum[col];
		}
		System.out.println("Totale Colonne: " + totale);
	}

	private static void stampaTotaleRighe() {
		int totale = 0;
		for (int row = 0; row < rowSum.length; row++) {
			totale += rowSum[row];
		}
		System.out.println("Totale Righe: " + totale);
	}

	public static int sumRow(final int row) {
		int result = 0;
		for (int col = 0; col < matrix[row].length; col++)
			result += matrix[row][col];

		return result;
	}

	public static int sumColumn(final int row) {
		int temp = 0;
		for (int col = 0; col < matrix.length; col++)
			temp += matrix[col][row];

		return temp;
	}

	private static void initMatrix() {
		Random r = new Random();
		for (int row = 0; row < matrix.length; row++) {
			for (int col = 0; col < matrix[row].length; col++) {
				matrix[row][col] = 1 + r.nextInt(100);
			}
		}
	}

	private static void printMatrix() {
		for (int i = 0; i < matrix.length; i++)
			printArray(matrix[i]);
	}

	private static void printArray(final int[] array) {
		for (int i = 0; i < array.length; i++)
			System.out.print(array[i] + "\t");
		System.out.println();
	}
}
