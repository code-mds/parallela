package serie11;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

final class Coordinate {
	private final double lat;
	private final double lon;

	public Coordinate(final double lat, final double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	/**
	 * Returns the distance (expressed in km) between two coordinates
	 *
	 * @param from
	 * @return Returns the distance expressed in km
	 */
	public double distance(final Coordinate from) {
		final double earthRadius = 6371.000; // km
		final double dLat = Math.toRadians(from.lat - this.lat);
		final double dLng = Math.toRadians(from.lon - this.lon);
		final double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(from.lat))
				* Math.cos(Math.toRadians(this.lat)) * Math.sin(dLng / 2.0) * Math.sin(dLng / 2.0);
		final double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return (earthRadius * c);
	}

	@Override
	public String toString() {
		return String.format("[%.5f, %.5f]", lat, lon);
	}
}

class Earthquake {
	private final static String CSV_REGEX = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

	private final Date time;
	private final Coordinate position;
	private final double depth;
	private final double magnitude;
	private final String place;

	public Earthquake(final Date time, final Coordinate pos, final double depth, final double mag, final String place) {
		this.time = time;
		this.position = pos;
		this.depth = depth;
		this.magnitude = mag;
		this.place = place;
	}

	public Coordinate getPosition() {
		return position;
	}

	public double getMagnitude() {
		return magnitude;
	}

	public double getDepth() {
		return depth;
	}

	public static Earthquake parse(final String csvLine) {
		final String[] splits = csvLine.split(CSV_REGEX);
		if (splits.length != 15) {
			System.out.println("Failed to parse: " + csvLine);
			return null;
		}

		final Date time;

		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		try {
			time = sdf.parse(splits[0]);
		} catch (final ParseException e) {
			return null;
		}

		final double lat = tryParseDouble(splits[1]);
		final double lon = tryParseDouble(splits[2]);
		final double depth = tryParseDouble(splits[3]);
		final double mag = tryParseDouble(splits[4]);
		final String place = splits[13];

		return new Earthquake(time, new Coordinate(lat, lon), depth, mag, place);
	}

	private static Double tryParseDouble(final String str) {
		try {
			return Double.parseDouble(str);
		} catch (final NumberFormatException e) {
			return new Double(0);
		}
	}

	@Override
	public String toString() {
		return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(time) + " mag: " + magnitude
				+ " depth: " + depth + "km @ " + position + " " + place;
	}
}

public class S11Esercizio1 {

	private static List<Earthquake> loadEarthquakeDB(final String address, final boolean isLocalFile) {
		final List<Earthquake> quakes = new ArrayList<Earthquake>();

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
		
		final List<Earthquake> quakes = loadEarthquakeDB(URI, false);

		if (quakes.isEmpty()) {
			System.out.println("No earthquakes found!");
			return;
		}

		System.out.println("Loaded " + quakes.size() + " earthquakes");
		final Coordinate supsi = new Coordinate(46.0234, 8.9172);

		findNearest(supsi, quakes);
		findFarest(supsi, quakes);
		findStrongest(quakes);
		findTopTenFromSupsi(supsi, quakes);

		final long totalEndTime = System.currentTimeMillis();
		System.out.println("Completed in " + ((totalEndTime - startTime)) + " ms");
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

	// computation time=86 ms (Parallel Stream)
	// computation time=219 ms (Stream)
	private static void findFarest(Coordinate supsi, List<Earthquake> quakes) {
		final long computeTime = System.currentTimeMillis();
		System.out.println("Searching for farest earthquake ...");

		Earthquake curNearestQuake = quakes
				.parallelStream()
				//.stream()
				.max(Comparator.comparingDouble(q -> q.getPosition().distance(supsi))).get();

		double curNearestDistance = curNearestQuake.getPosition().distance(supsi);
		final long endTime = System.currentTimeMillis();
		System.out.println("Farest  : " + curNearestQuake + " distance: " + curNearestDistance + " (computation time=" + (endTime - computeTime) + " ms)");
	}

	// computation time=9 ms (Parallel Stream)
	// computation time=12 ms (Stream)
	private static void findStrongest(List<Earthquake> quakes) {
		final long computeTime = System.currentTimeMillis();
		System.out.println("Searching for farest earthquake ...");

		Earthquake result = quakes
				.parallelStream()
				//.stream()
				.max(Comparator.comparingDouble(Earthquake::getMagnitude)).get();

		final long endTime = System.currentTimeMillis();
		System.out.println("Strongest  : " + result + " (computation time=" + (endTime - computeTime) + " ms)");
	}

	private static void findTopTenFromSupsi(Coordinate supsi, List<Earthquake> quakes) {
		final long computeTime = System.currentTimeMillis();
		System.out.println("Searching for farest earthquake ...");

		List<Earthquake> topTen = quakes
				.parallelStream()
				.filter(q -> q.getPosition().distance(supsi) > 2000)
				.filter(q -> q.getMagnitude() >= 4 && q.getMagnitude() <=6)
				.sorted(Comparator.comparingDouble(q -> q.getPosition().distance(supsi)))
				.limit(10)
				.collect(Collectors.toList());

		final long endTime = System.currentTimeMillis();
		for (int i = 0; i < topTen.size(); i++) {
			System.out.println(i + ") " + topTen.get(i) + " distance: " + topTen.get(i).getPosition().distance(supsi));
		}

		//  computation time=505 ms (Stream)
		//  computation time=269 ms (ParallelStream)
		System.out.println(" (computation time=" + (endTime - computeTime) + " ms)");
	}

}
