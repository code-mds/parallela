package simulazioneTest2;

import java.util.concurrent.ThreadLocalRandom;

final class Coordinate {
    final private double lat;
    final private double lon;

    private Coordinate(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public static Coordinate create(final double lat, final double lon) {
        return new Coordinate(lat, lon);
    }

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
        return "[" + lat + ", " + lon + "]";
    }
}

class GPS implements Runnable {
    @Override
    public void run() {
        // Update curLocation with first coordinate
        double lat = ThreadLocalRandom.current().nextDouble(-90.0, +90.0);
        double lon = ThreadLocalRandom.current().nextDouble(-180.0, +180.0);
        GPSSimulator.curLocation = Coordinate.create(lat, lon);

        // Wait before updating position
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(10, 20));
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Update curLocation with second coordinate
        System.out.println("Update coordinate");
        lat = ThreadLocalRandom.current().nextDouble(-90.0, +90.0);
        lon = ThreadLocalRandom.current().nextDouble(-180.0, +180.0);
        GPSSimulator.curLocation = Coordinate.create(lat, lon);
    }
}

public class GPSSimulator {
    static volatile Coordinate curLocation = null;

    public static void main(final String[] args) {
        // Create and start GPS thread
        final Thread gpsThread = new Thread(new GPS());
        gpsThread.start();

        //System.out.println("Simulation started");
        while (curLocation == null) {
            // Wait until location changes
            System.out.println("Wait 1");
        }
        final Coordinate firstLocation = curLocation;

        while (curLocation == firstLocation) {
            // Wait until location changes
            System.out.println("Wait 2");
        }
        final Coordinate secondLocation = curLocation;

        // Write distance between firstLocation and secondLocation position
        System.out.println("Distance from " + firstLocation + " to "
                + secondLocation + " is "
                + firstLocation.distance(secondLocation));

        // Wait until GPS thread finishes
        try {
            gpsThread.join();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Simulation completed");
    }
}