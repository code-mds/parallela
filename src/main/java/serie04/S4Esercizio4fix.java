package serie04;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

final class CoordinateImmutable {
	private final double lat;
	private final double lon;

	CoordinateImmutable(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	/**
	 * Returns the distance (expressed in km) between two coordinates
	 */
	public double distance(final CoordinateImmutable from) {
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
		return String.format("<%d> [%.3f,%.3f]", hashCode(), lat, lon);
	}
}

class GPSfix implements Runnable {
	@Override
	public void run() {
		
		while (!S4Esercizio4fix.completed) {
			S4Esercizio4fix.writeLock.lock();
			try {
				double lat = ThreadLocalRandom.current().nextDouble(-90.0, +90.0);
				double lon = ThreadLocalRandom.current().nextDouble(-180.0, +180.0);
				// Update curLocation with first coordinate
				S4Esercizio4fix.curLocation = new CoordinateImmutable(lat, lon);
			} finally {
				S4Esercizio4fix.writeLock.unlock();
			}

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

public class S4Esercizio4fix {
	static volatile boolean completed = false;
	static CoordinateImmutable curLocation = null;
	static ReadWriteLock rwLock = new ReentrantReadWriteLock();
	static Lock readLock = rwLock.readLock();
	static Lock writeLock = rwLock.writeLock();
	//static Lock lock = new ReentrantLock();

	public static void main(final String[] args) {
		// Create and start GPS thread
		final Thread gpsThread = new Thread(new GPSfix());
		gpsThread.start();

		System.out.println("Simulation started");

		CoordinateImmutable prevLocation = null;

		// Wait until location changes
		do {
			readLock.lock();
			try {
				prevLocation = curLocation;
			} finally {
				readLock.unlock();
			}
		}
		while (prevLocation == null);

		System.out.println("Initial position received");

		// Request 10 position updates
		for (int i = 0; i < 100; i++) {
			CoordinateImmutable lastLocation;
			do {
				readLock.lock();
				try {
					lastLocation = curLocation;
				} finally {
					readLock.unlock();
				}
			} while (lastLocation == prevLocation);

			// Write distance between firstLocation and secondLocation position
			System.out.println(i + ") Distance from " + prevLocation + " to "
					+ lastLocation + " is "
					+ prevLocation.distance(lastLocation) + " , curLoc " + curLocation);

			prevLocation = lastLocation;
		}

		completed = true;

		// Stop GPS thread and wait until it finishes
		try {
			gpsThread.join();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Simulation completed");
	}
}
