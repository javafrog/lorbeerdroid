package de.lorbeer.app;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import de.lorbeer.handler.DishHandler;
import de.lorbeer.helper.ConnectionChecker;
import de.lorbeer.rest.RequestMethod;
import de.lorbeer.rest.RestClient;
import de.mmt.lorbeerblatt.data.Dish;

public class DishActivity extends Activity {
	private static ArrayList<Dish> dishes;
	private DishHandler dishHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dishes);

		// get all data from intent (name and id)
		Integer m_id = getIntent().getExtras().getInt("m_id");
		// set header name
		String m_name = getIntent().getExtras().getString("m_name");
		TextView tv = (TextView) findViewById(R.id.m_name);
		tv.setText(m_name);

		// load data from REST, add ID for REST-service call
		if (ConnectionChecker.isNetworkReachable(getApplicationContext(), true)) {
			getData(m_id);
		} else {
			// no connection available
			Toast.makeText(getApplicationContext(), R.string.noConnection,
					Toast.LENGTH_LONG).show();
		}

	}

	private void getData(int m_id) {// get data from RESTservice
		RestClient rc = new RestClient(MensaActivity.basicURI + "mensas" + "/"
				+ m_id + "/dishes");
		try {
			rc.Execute(RequestMethod.GET);
		} catch (Exception e) {
			System.out.println("Fehler beim laden der Daten");
		}
		// timeout handling
		if (!rc.timeout) {
			String resp = rc.getResponse();

			try {
				// create parser
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();

				dishHandler = new DishHandler();
				xr.setContentHandler(dishHandler);

				InputSource is = new InputSource(new StringReader(resp));
				is.setEncoding("UTF-8");
				xr.parse(is);

			} catch (Exception e) {
				System.out.println("Fehler beim parsen");
				System.out.println(e.getMessage());

			}
			// load data from handler
			dishes = dishHandler.getDishes();
			Collections.sort(dishes, new Comparator<Dish>()
			{

				public int compare(Dish arg0, Dish arg1) {
					if (arg0.name == null) return 0;
					return arg0.name.compareTo(arg1.name);
				}});
			// fill the list
			fillList();

		} else {
			Toast.makeText(getApplicationContext(), R.string.error,
					Toast.LENGTH_LONG).show();
		}
	}

	private void fillList() {
		// set either list or no offers today text
		if (dishes.size() > 0) {
			// set list
			ListView dish_list = (ListView) findViewById(R.id.dishes);
			DishAdapter d_adapter = new DishAdapter(getApplicationContext(),
					R.layout.single_row_dish, dishes);
			dish_list.setAdapter(d_adapter);
			dish_list.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// get name and start single dish intent
					changeView(position);
				}
			});
		} else {
			// no offers today textview
			TextView tv = new TextView(getApplicationContext());
			tv.setGravity(1);
			tv.setPadding(0, 30, 0, 0);
			tv.setText(R.string.noDish);
			LinearLayout dishLayout = (LinearLayout) findViewById(R.id.DishLayout);
			dishLayout.addView(tv, 1);
		}
	}

	private void changeView(int position) {
		Dish x = dishes.get(position);
		// change view if dish has meal on it :)
		if (!x.name.contains("Kein Angebot")) {
			Intent i = new Intent(DishActivity.this, SingleDishActivity.class);
			i.putExtra("d_pos", position);
			startActivity(i);
		} else {
			Toast.makeText(getApplicationContext(),
					getResources().getText(R.string.noDish), Toast.LENGTH_SHORT)
					.show();
		}
	}

	private class DishAdapter extends ArrayAdapter<Dish> {

		private List<Dish> a_dishes;

		public DishAdapter(Context context, int textViewResourceId,
				List<Dish> dishes) {
			super(context, textViewResourceId, dishes);
			this.a_dishes = dishes;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.single_row_dish, null);
			}
			Dish dish = a_dishes.get(position);
			if (dish != null) {
				TextView tt = (TextView) v.findViewById(R.id.topdish);
				if (tt != null) {
					// dish name
					tt.setText(dish.name);
				}

			}
			return v;
		}
	}

	public static Dish getDish(int pos) {
		return dishes.get(pos);
	}

}
