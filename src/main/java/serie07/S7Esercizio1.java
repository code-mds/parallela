package serie07;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class TestWorker implements Runnable {
	private final int id;
	private final static Map<String, Integer> sharedMap = new HashMap<String, Integer>();
	private final static ConcurrentMap<String, Integer> sharedMapConcurrent = new ConcurrentHashMap<>();
	private int counter = 0;

	public TestWorker(final int id) {
		this.id = id;
	}

	@Override
	public void run() {
		final Random random = new Random();
		final Integer int1 = new Integer(1);
		final Integer int5 = new Integer(5);
		final Integer int10 = new Integer(10);
		int cnt = 100000;

		while (--cnt > 0) {
			final String key = getClass().getSimpleName() + random.nextInt(S7Esercizio1.NUM_WORKERS);
			updateCounter(random.nextBoolean());
			//updateMapSynchronized(int1, int5, int10, key);
			updateMapConcurrentColl(int1, int5, int10, key);
		}

	}

	private void updateMapConcurrentColl(Integer int1, Integer int5, Integer int10, String key) {
			if (counter == 0) {
				// se counter=0, rimuovi dalla mappa se valore corrente=1
				if(sharedMapConcurrent.remove(key, int1))
					log("{" + key + "} remove 1");
			} else if (counter == 1) {
				// se counter=1, inserisci 1 se chiave non presente
				if(sharedMapConcurrent.putIfAbsent(key, int1) == null)
					log("{" + key + "} put 1");
			} else if (counter == 5) {
				// se counter=5, aggiorna mappa con 5 se valore attuale=10
				if(sharedMapConcurrent.replace(key, 10, int5))
					log("{" + key + "} replace " + 10 + " with 5");
			} else if (counter == 10) {
				// se counter=10, aggiorna mappa con 10 indipendentemente dal valore attuale
				sharedMapConcurrent.computeIfPresent(key, (k,v) -> {
					log("{" + key + "} replace " + v.intValue() + " with 10");
					return sharedMapConcurrent.put(k, int10);
				});
			}
	}

	private void updateMapSynchronized(Integer int1, Integer int5, Integer int10, String key) {
		synchronized (sharedMap) {
			if (counter == 0) {
				// se counter=0, rimuovi dalla mappa se valore corrente=1
				if (sharedMap.containsKey(key) && sharedMap.get(key).equals(int1)) {
//					if (!sharedMap.containsKey(key)) {
//						logErr("0: chiave non presente");
//					}
//					if (!sharedMap.get(key).equals(int1)) {
//						logErr("0: valore cambiato");
//					}

					sharedMap.remove(key);
					log("{" + key + "} remove 1");
				}
			} else if (counter == 1) {
				// se counter=1, inserisci 1 se chiave non presente
				if (!sharedMap.containsKey(key)) {
//					if (sharedMap.containsKey(key))
//						logErr("1: chiave presente");

					sharedMap.put(key, int1);
					log("{" + key + "} put 1");
				}
			} else if (counter == 5) {
				// se counter=5, aggiorna mappa con 5 se valore attuale=10
				if (sharedMap.containsKey(key) && sharedMap.get(key).equals(10)) {
//					if (!sharedMap.containsKey(key)) {
//						logErr("5: chiave non presente");
//					}
//					if (!sharedMap.get(key).equals(10)) {
//						logErr("5: valore cambiato");
//					}

					final Integer prev = sharedMap.put(key, int5);
					log("{" + key + "} replace " + prev.intValue() + " with 5");
				}
			} else if (counter == 10) {
				// se counter=10, aggiorna mappa con 10 indipendentemente dal valore attuale
				if (sharedMap.containsKey(key)) {
//					if (!sharedMap.containsKey(key))
//						logErr("10: chiave non presente");

					final Integer prev = sharedMap.put(key, int10);
					log("{" + key + "} replace " + prev.intValue() + " with 10");
				}
			}
		}
	}

	private void logErr(String s) {
		System.err.println(s);
//		throw new Exception(s);
	}

	// counter e' membro di classe, non e' condiviso tra i thread
	// il counter va su e giu in maniera casuale (random.nextBoolean)
	// se supera le soglie viene resettato a 0 o 10
	private final void updateCounter(final boolean increment) {
		if (increment) {
			if (++counter > 10)
				counter = 0;
		} else {
			if (--counter < 0)
				counter = 10;
		}
	}

	private final void log(final String msg) {
		System.out.println(getClass().getSimpleName() + id + ": " + msg);
	}
}

public class S7Esercizio1 {
	static final int NUM_WORKERS = 50;

	public static void main(final String[] args) {
		final List<Thread> allThreads = new ArrayList<Thread>();

		for (int i = 0; i < NUM_WORKERS; i++) {
			allThreads.add(new Thread(new TestWorker(i)));
		}

		System.out.println("---------------------------");
		for (final Thread t : allThreads)
			t.start();

		for (final Thread t : allThreads)
			try {
				t.join();
			} catch (final InterruptedException e) {
				/* do nothing */
			}
		System.out.println("---------------------------");
	}
}
