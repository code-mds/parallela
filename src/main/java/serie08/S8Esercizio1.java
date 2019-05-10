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
		System.out.println(idx + ") SOMMO RIGHE");
		int sum = S8Esercizio1.sumRow(idx);
		synchronized (S8Esercizio1.rowSum) {
			S8Esercizio1.rowSum[idx] = sum;
			while (S8Esercizio1.rowsToSum > 0) {
				try {
					S8Esercizio1.rowSum.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			S8Esercizio1.rowSum.notifyAll();
		}

		System.out.println(idx + ") SOMMO COLONNE");
		sum = S8Esercizio1.sumColumn(idx);
		synchronized (S8Esercizio1.colSum) {
			S8Esercizio1.colSum[idx] = sum;
			while (S8Esercizio1.colsToSum > 0) {
				try {
					S8Esercizio1.colSum.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			S8Esercizio1.colSum.notifyAll();
		}
	}
}

class Matrix {

}

public class S8Esercizio1 {
	final static int[][] matrix = new int[10][10];
	final static int[] rowSum = new int[matrix.length];
	final static int[] colSum = new int[matrix[0].length];
	static int rowsToSum = rowSum.length;
	static int colsToSum = colSum.length;

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

		synchronized (rowSum){
			while (rowsToSum > 0) {
				try {
					rowSum.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		// Stampa somma delle righe
		System.out.println("Somme delle righe:");
		printArray(rowSum);
		stampaTotaleRighe();

		synchronized (colSum){
			while (colsToSum > 0) {
				try {
					colSum.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

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

		rowsToSum--;
		return result;
	}

	public static int sumColumn(final int row) {
		int temp = 0;
		for (int col = 0; col < matrix.length; col++)
			temp += matrix[col][row];

		colsToSum--;


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
