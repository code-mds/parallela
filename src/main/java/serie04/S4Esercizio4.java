package serie04;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Coordinate {
	// MDS: membri pubblici non incapsulati di un oggetto condiviso
	public double lat = 0.0;
	public double lon = 0.0;

	/**
	 * Returns the distance (expressed in km) between two coordinates
	 */
	public double distance(final Coordinate from) {
		final double dLat = Math.toRadians(from.lat - this.lat);
		final double dLng = Math.toRadians(from.lon - this.lon);
		final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(from.lat))
				* Math.cos(Math.toRadians(this.lat)) * Math.sin(dLng / 2)
				* Math.sin(dLng / 2);
		return (6371.000 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
	}

	@Override
	public String toString() {
		// MDS: stampo anche reference
		return String.format("<%d> [%.3f,%.3f]", hashCode(), lat, lon);
		//return "<" + hashCode() + "> [" + lat + ", " + lon + "]";
	}
}

class GPS implements Runnable {
	@Override
	public void run() {
		
		while (!S4Esercizio4.completed) {

			S4Esercizio4.lock.lock();
			try {
				S4Esercizio4.curLocation = new Coordinate();
				//System.out.println("MDS: curLocation changed");
			} finally {
				S4Esercizio4.lock.unlock();
			}

			// Update curLocation with first coordinate
			// MDS: update non sincronizzato di lat e lon
			//      i due valori vengono aggiornati separatamente
			//      il main thread potrebbe leggere un dato parzialmente aggiornato
			S4Esercizio4.curLocation.lat = ThreadLocalRandom.current().nextDouble(-90.0, +90.0);
			S4Esercizio4.curLocation.lon = ThreadLocalRandom.current().nextDouble(-180.0, +180.0);

			// Wait before updating position
			try {
				Thread.sleep(ThreadLocalRandom.current().nextLong(1, 5));
			} catch (final InterruptedException e) {
				System.out.println("INTERRUPTED THREAD");
				Thread.currentThread().interrupt();
			}
		}
	}
}

public class S4Esercizio4 {
	// MDS: dovrebbe essere volatile per assicurare corretta visibilitac'
	static boolean completed = false;

	// MDS: oggetto condiviso tra Main thread e GPSThread
	static Coordinate curLocation = null;
	static Lock lock = new ReentrantLock();

	public static void main(final String[] args) {
		// Create and start GPS thread
		final Thread gpsThread = new Thread(new GPS());
		gpsThread.start();

//		for (int i=0;i<100;i++)
//			new Thread(new GPS()).start();

		System.out.println("Simulation started");

		Coordinate prevLocation = null;

		// Wait until location changes
		do {
			// MDS: basterebbe un read lock in questo do/while
			lock.lock();
			try {
				// MDS: copio riferimento all'oggetto condiviso curLocation
				prevLocation = curLocation;
			} finally {
				lock.unlock();
			}
			System.out.println("MDS: check prevLoc ");
		}
		while (prevLocation == null);

		System.out.println("Initial position received");

		// Request 10 position updates
		for (int i = 0; i < 100; i++) {
			Coordinate lastLocation;
			do {
				// MDS: basterebbe un read lock in questo do/while
				//      per ottenere un riferimento a curLocation
				lock.lock();
				try {
					lastLocation = curLocation;
				} finally {
					lock.unlock();
				}
			} while (lastLocation == prevLocation);

			// Write distance between firstLocation and secondLocation position
			System.out.println("Distance from " + prevLocation + " to "
					+ lastLocation + " is "
					+ prevLocation.distance(lastLocation) + " , curLoc " + curLocation);

			prevLocation = lastLocation;
		}

		completed = true;

		// Stop GPS thread and wait until it finishes
		try {
			// MDS: attiva memory barriers
			gpsThread.join();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Simulation completed");
	}
}
