package serie08;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Adder implements Runnable {
	private final int idx;

	Adder(int idx) {
		this.idx = idx;
	}

	@Override
	public void run() {
		// Calcola somma delle righe
		S8Esercizio1.rowSum[idx] = S8Esercizio1.sumRow(idx);
		S8Esercizio1.colSum[idx] = S8Esercizio1.sumColumn(idx);
	}
}

class Matrix {

}

public class S8Esercizio1 {
	final static int[][] matrix = new int[10][10];
	static volatile int rowsToSum = matrix.length;
	final static int[] rowSum = new int[matrix.length];
	final static int[] colSum = new int[matrix[0].length];

	public static void main(String[] args) {
		// Inizializza matrice con valori random
		initMatrix();

		// Stampa matrice
		System.out.println("Matrice:");
		printMatrix();

		List<Thread> threads = new ArrayList<>();
		List<Adder> adders = new ArrayList<>();
		for (int i=0;i<matrix.length; i++) {
			Adder adder = new Adder(i);
			adders.add(adder);
			threads.add(new Thread(adder));
		}
		threads.forEach(Thread::start);


//		for (int row = 0; row < matrix.length; row++) {
//			synchronized (S8Esercizio1.rowSum) {
//				try {
//					S8Esercizio1.rowSum.wait();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}


//			rowSum[row] = sumRow(row);

		// Stampa somma delle righe
		System.out.println("Somme delle righe:");
		printArray(rowSum);

		// Calcola somma delle colonne
//		for (int col = 0; col < matrix[0].length; col++)
//			for (int row = 0; row < matrix.length; row++) {
//				synchronized (S8Esercizio1.colSum) {
//					try {
//						S8Esercizio1.colSum.wait();
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			}


		// Stampa somma delle colonne
		System.out.println("Somme delle colonne:");
		printArray(colSum);
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
