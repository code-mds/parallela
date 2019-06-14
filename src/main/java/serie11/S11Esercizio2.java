package serie11;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public class S11Esercizio2 {
    public static void main(final String[] args) {
        final String URI = S11Esercizio1.class.getResource("/2014-2015.csv").toString();
        //final String URI = "/resources/2014-2015.csv";
        final long startTime = System.currentTimeMillis();

        final CompletableFuture<List<Earthquake>> future =
                CompletableFuture.supplyAsync(() -> EarthquakeProcessor.loadEarthquakeDB(URI, false));
        final Coordinate supsi = new Coordinate(46.0234, 8.9172);


        List<CompletableFuture> tasks = new ArrayList<>();

        tasks.add(future.thenAccept( quakes -> EarthquakeProcessor.findNearest(supsi, quakes.stream())));
        tasks.add(future.thenAccept( quakes -> EarthquakeProcessor.findFarest(supsi, quakes.stream())));
        tasks.add(future.thenAccept( quakes -> EarthquakeProcessor.findStrongest(quakes.stream())));
        tasks.add(future.thenAccept( quakes -> EarthquakeProcessor.findTopTenFromSupsi(supsi, quakes.stream())));
        tasks.add(future.thenAccept( quakes -> EarthquakeProcessor.findLatitude46(quakes.stream())));
        tasks.add(future.thenAccept( quakes -> EarthquakeProcessor.findLongitude8(quakes.stream())));
        tasks.add(future.thenAccept( quakes -> EarthquakeProcessor.groupByDepth(quakes.stream())));
        tasks.add(future.thenAccept( quakes -> EarthquakeProcessor.groupByMagnitude(quakes.stream())));

        CompletableFuture all = CompletableFuture.allOf(tasks.toArray(new CompletableFuture[tasks.size()]));

        final List<Earthquake> quakes;
        try {
            all.get();
//            quakes = future.get();
//
//            if (quakes.isEmpty()) {
//                System.out.println("No earthquakes found!");
//                return;
//            }
//            System.out.println("Loaded " + quakes.size() + " earthquakes");

            final long totalEndTime = System.currentTimeMillis();
            System.out.println("Completed in " + ((totalEndTime - startTime)) + " ms");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
