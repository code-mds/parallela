package serie08;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class AdderCondition implements Runnable {

	private final int idx;

	AdderCondition(int idx) {
		this.idx = idx;
	}

	@Override
	public void run() {
		System.out.println(idx + ") SOMMO RIGHE");
		int sum = S8Esercizio1.sumRow(idx);

		S8Esercizio1.lock.lock();
		try{
			S8Esercizio1.rowsToSum--;
			S8Esercizio1.rowSum[idx] = sum;
			while (S8Esercizio1.rowsToSum > 0) {
				try {
					S8Esercizio1.rowsCondition.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			S8Esercizio1.rowsCondition.signalAll();
		} finally {
			S8Esercizio1.lock.unlock();
		}

		System.out.println(idx + ") SOMMO COLONNE");
		sum = S8Esercizio1.sumColumn(idx);
		S8Esercizio1.lock.lock();
		try{
			S8Esercizio1.colsToSum--;
			S8Esercizio1.colSum[idx] = sum;
			while (S8Esercizio1.colsToSum > 0) {
				try {
					S8Esercizio1.colsCondition.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			S8Esercizio1.colsCondition.signalAll();
		} finally {
			S8Esercizio1.lock.unlock();
		}
	}
}

public class S8Esercizio1Condition {
	final static int[][] matrix = new int[10][10];
	final static int[] rowSum = new int[matrix.length];
	final static int[] colSum = new int[matrix[0].length];
	static int rowsToSum = rowSum.length;
	static int colsToSum = colSum.length;

	final static Lock lock = new ReentrantLock();
	final static Condition rowsCondition = lock.newCondition();
	final static Condition colsCondition = lock.newCondition();


	public static void main(String[] args) {
		// Inizializza matrice con valori random
		initMatrix();

		// Stampa matrice
		System.out.println("Matrice:");
		printMatrix();

		List<Thread> threads = new ArrayList<>();
		for (int i=0;i<matrix.length; i++) {
			threads.add(new Thread(new AdderCondition(i)));
		}

		threads.forEach(Thread::start);

		S8Esercizio1.lock.lock();
		try{
			while (rowsToSum > 0) {
				try {
					rowsCondition.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} finally {
			S8Esercizio1.lock.unlock();
		}

		// Stampa somma delle righe
		System.out.println("Somme delle righe:");
		printArray(rowSum);
		stampaTotaleRighe();

		S8Esercizio1.lock.lock();
		try{
			while (colsToSum > 0) {
				try {
					colsCondition.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} finally {
			S8Esercizio1.lock.unlock();
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
