package serie01;

public class S1Esercizio3 {
    private final static int MAX_DATA = 10_000;
    private final static int MAX_THREAD = 10;
    private final static int INTERVAL_SIZE = MAX_DATA / MAX_THREAD;

    static class Calculator implements Runnable {
        private final int start;
        private final int end;
        private final int[] data;

        Calculator(int[] data, int start, int end) {
            this.data = data;
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            int total = 0;
            for (int i = start; i<=end; i++)
                total += data[i];

            System.out.printf("Somma degli elementi nell'intervallo [%d;%d] = %d\n", start, end, total);
        }
    }

    public static void main(String[] args) {
        int[] data = new int[MAX_DATA];
        for (int i=0; i<data.length; i++)
            data[i] = calcRandom();

        Thread[] threads = new Thread[MAX_THREAD];
        for(int i=0; i<threads.length; i++) {
            int start = i * INTERVAL_SIZE;
            int end = (i+1) * INTERVAL_SIZE-1;

            threads[i] = new Thread(new Calculator(data, start, end));
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static int calcRandom() {
        return 1 + (int)(Math.random() * 100.0);
    }
}
