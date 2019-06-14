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

public class S11Esercizio2 {
    private static List<Earthquake> loadEarthquakeDB(final String address, final boolean isLocalFile) {
        final List<Earthquake> quakes = new ArrayList<>();

        final Reader reader;
        if (isLocalFile) {
            try {
                final File file = new File(address);
                reader = new FileReader(file);
            } catch (final FileNotFoundException e2) {
                System.out.println("Failed to open file: " + address);
                return Collections.emptyList();
            }

        } else {
            final URL url;
            try {
                url = new URL(address);
            } catch (final MalformedURLException e) {
                System.out.println("Failed to create URL for address: " + address);
                return Collections.emptyList();
            }
            final InputStream is;
            try {
                is = url.openStream();
            } catch (final IOException e) {
                System.out.println("Failed to open stream for: " + address);
                return Collections.emptyList();
            }
            reader = new InputStreamReader(is);
        }

        System.out.println("Requesting earthquake data from: " + address + " ...");

        String line;
        try {
            final BufferedReader br = new BufferedReader(reader);
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                final Earthquake quake = Earthquake.parse(line);
                if (quake != null)
                    quakes.add(quake);
                else
                    System.out.println("Failed to parse: " + line);
            }
            br.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return quakes;
    }

    public static void main(final String[] args) {
        final String URI = S11Esercizio1.class.getResource("/2014-2015.csv").toString();
        //final String URI = "/resources/2014-2015.csv";
        final long startTime = System.currentTimeMillis();

        final CompletableFuture<List<Earthquake>> future =
                CompletableFuture.supplyAsync(() -> loadEarthquakeDB(URI, false));

        final List<Earthquake> quakes;
        try {
            quakes = future.get();

            if (quakes.isEmpty()) {
                System.out.println("No earthquakes found!");
                return;
            }

            System.out.println("Loaded " + quakes.size() + " earthquakes");
            final Coordinate supsi = new Coordinate(46.0234, 8.9172);

            findNearest(supsi, quakes);

            final long totalEndTime = System.currentTimeMillis();
            System.out.println("Completed in " + ((totalEndTime - startTime)) + " ms");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    // computation time=129 ms (classic loop)
    // computation time=188 ms (Parallel Stream)
    // computation time=243 ms (Stream)
    private static void findNearest(Coordinate supsi, List<Earthquake> quakes) {
        final long computeTime = System.currentTimeMillis();
        System.out.println("Searching for nearest earthquake ...");

        Earthquake curNearestQuake = quakes
                .parallelStream()
                //.stream()
                .min(Comparator.comparingDouble(q -> q.getPosition().distance(supsi))).get();

        double curNearestDistance = curNearestQuake.getPosition().distance(supsi);
        //Nearest  : Optional[28.11.15 21:29 mag: 3.0 depth: 5.0km @ [45.79000, 9.79000] "2km N of Albino, Italy"] distance: 72.34566016141176

        final long endTime = System.currentTimeMillis();
        System.out.println("Nearest  : " + curNearestQuake + " distance: " + curNearestDistance + " (computation time=" + (endTime - computeTime) + " ms)");
    }
}
