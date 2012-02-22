package de.lorbeer.helper;

import java.text.DecimalFormat;

/**
 * A Mensa offers dishes. Every work day, there are different dishes available.
 * The number of available dishes varies by Mensa and day.
 * 
 * @author Matthias Niederhausen
 * @author edited by: a.augsburg
 */

public class GeoMensa implements Comparable<GeoMensa> {
	/**
	 * Distance between last known Position of User Device and this GeoMensa
	 */
	public String distance = "0";
	/**
	 * The object id.
	 */
	public String id;
	/**
	 * The GeoMensa's name.
	 */
	public String name;
	/**
	 * The GeoMensa's geo location (latitude), in google-maps-compatible format,
	 * e.g., "51.618017", or 0 if not set
	 */
	public double geoLatitude;
	/**
	 * The GeoMensa's geo location (longitude), in google-maps-compatible
	 * format, e.g., "13.798828", or 0 if not set
	 */
	public double geoLongitude;

	/**
	 * A map assigning dishes to specific days.
	 */

	/**
	 * Creates a new mensa with an id and a name.
	 * 
	 * @param id
	 *            the object id
	 * @param name
	 *            the name, e.g., "Alte Mensa"
	 */

	public GeoMensa(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public GeoMensa() {

	}

	public String toString() {
		return this.name + " / " + this.distance + "m";
	}

	@Override
	public int compareTo(GeoMensa another) {
		double m1 = Double.parseDouble(this.distance);
		double m2 = Double.parseDouble(another.distance);
		if (m1 < m2) return -1;
		if (m1 < m2) return 1; 
		
		return this.name.compareTo(another.name);
	}

	public String getFormattedDistance() {
		Double dist = Double.parseDouble(this.distance);
		String format = "";
		if (dist <= 1000) {
			// distance in meters
			int i = new Double(dist).intValue();
			format = Integer.toString(i) + "m";
		} else {
			// distance in km
			DecimalFormat df = new DecimalFormat("#.0");
			dist = dist / 1000;
			format = df.format(dist) + "km";
		}

		return format;

	}
}
