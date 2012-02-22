package de.lorbeer.app;

import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import de.lorbeer.handler.MensaHandler;
import de.lorbeer.helper.ConnectionChecker;
import de.lorbeer.helper.GeoMensa;
import de.lorbeer.helper.MyLocation;
import de.lorbeer.helper.MyLocation.LocationResult;
import de.lorbeer.rest.RequestMethod;
import de.lorbeer.rest.RestClient;

public class MensaActivity extends Activity {
	private MensaHandler mensaHandler;
	public static String basicURI = "http://www.hyperadapt.net/FoodDispenser/";
	private List<GeoMensa> mensas;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// load data from REST
		if (ConnectionChecker.isNetworkReachable(getApplicationContext(), true)) {
			getData();
		} else {
			// no connection available
			Toast.makeText(getApplicationContext(), R.string.noConnection,
					Toast.LENGTH_SHORT).show();
		}
	}

	private void getData() {
		// get data from REST
		RestClient rc = new RestClient(basicURI + "mensas");
		try {
			rc.Execute(RequestMethod.GET);
		} catch (Exception e) {
			System.out.println("Fehler beim laden der Mensas");
		}

		// timeout handling
		if (!rc.timeout) {
			String resp = rc.getResponse();

			try {
				// create parser
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();

				mensaHandler = new MensaHandler();
				xr.setContentHandler(mensaHandler);

				InputSource is = new InputSource(new StringReader(resp));
				is.setEncoding("UTF-8");
				xr.parse(is);

			} catch (Exception e) {
				System.out.println("Fehler beim parsen / Mensas");
			}
			// load data from handler
			this.mensas = mensaHandler.getGeoMensas();
			Collections.sort(mensas);

			MyLocation myLocation = new MyLocation();

			LocationResult locationResult = new LocationResult() {
				@Override
				public void gotLocation(final Location location) {
					// nullchecker if no valid location was found
					if (location != null) {
						calculateDistances(location);
						Collections.sort(mensas);
					}

				}
			};

			myLocation.getLocation(this, locationResult);

			// set Data
			fillList();
		} else {
			Toast.makeText(getApplicationContext(), R.string.error,
					Toast.LENGTH_LONG).show();
		}

	}

	private void fillList() {
		// get ListView and set data
		ListView mensa_list = (ListView) findViewById(R.id.mensa_list);
		GeoMensaAdapter geoAdapter = new GeoMensaAdapter(
				getApplicationContext(), R.layout.single_row_mensa, mensas);
		mensa_list.setAdapter(geoAdapter);
		mensa_list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// get name and start dish intent
				GeoMensa g = mensas.get(position);
				int m_id = Integer.parseInt(g.id);
				String m_name = g.name;
				changeView(m_id, m_name);
			}
		});
	}

	private void changeView(Integer m_id, String m_name) {
		Intent i = new Intent(MensaActivity.this, DishActivity.class);
		i.putExtra("m_id", m_id);
		i.putExtra("m_name", m_name);
		startActivity(i);

	}

	/**
	 * Calculates the Distance from the Users current Position to all available
	 * Mensas
	 * 
	 * @param loc
	 */
	private void calculateDistances(Location loc) {
		for (GeoMensa g : mensas) {
			double distance = 0;
			try {

				float[] results = new float[3];
				Location.distanceBetween(loc.getLatitude(), loc.getLongitude(),
						g.geoLatitude, g.geoLongitude, results);
				distance = results[0];
			} catch (Exception e) {
				e.printStackTrace();
				distance = 0.0;
			}
			g.distance = Double.toString(distance);
		}
		// update list
		fillList();

	}

	public void showInfo(View v) {
		Intent i = new Intent(MensaActivity.this, InfoActivity.class);
		startActivity(i);
	}

	/**
	 * ListAdapter for a special 2-line ListView
	 * 
	 * @author Anton
	 * 
	 */
	private class GeoMensaAdapter extends ArrayAdapter<GeoMensa> {

		private List<GeoMensa> geoMensas;

		public GeoMensaAdapter(Context context, int textViewResourceId,
				List<GeoMensa> mensas) {
			super(context, textViewResourceId, mensas);
			this.geoMensas = mensas;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.single_row_mensa, null);
			}
			GeoMensa geo = geoMensas.get(position);
			if (geo != null) {
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				if (tt != null) {
					// mensa name
					tt.setText(geo.name);
				}
				if (bt != null) {
					// current distance is formatted
					bt.setText(geo.getFormattedDistance());

				}
			}
			return v;
		}
	}
}