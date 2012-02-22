package de.lorbeer.app;

import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import de.lorbeer.handler.PhotoHandler;
import de.lorbeer.handler.RatingHandler;
import de.lorbeer.helper.ConnectionChecker;
import de.lorbeer.helper.LoaderImageView;
import de.lorbeer.helper.NoImageView;
import de.lorbeer.rest.RequestMethod;
import de.lorbeer.rest.RestClient;
import de.mmt.lorbeerblatt.data.Dish;
import de.mmt.lorbeerblatt.data.Ingredient;

public class SingleDishActivity extends Activity {
	private Dish dish;
	private int d_pos;
	private RatingHandler ratingHandler;

	private static final int RATING_ADDED = 0010;
	private static final int ERROR = 9999;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_dish);

		// load dish
		d_pos = getIntent().getExtras().getInt("d_pos");
		dish = DishActivity.getDish(d_pos);
		// set dish name
		TextView dish_name = (TextView) findViewById(R.id.dish_name);
		dish_name.setText(dish.name);
		// set prices, if null set text
		if (dish.priceStudent == 0) {
			TextView tv = (TextView) findViewById(R.id.price_student);
			tv.setText(R.string.noPrice);
			tv = (TextView) findViewById(R.id.price_employee);
			tv.setText(R.string.noPrice);
		} else {
			TextView tv = (TextView) findViewById(R.id.price_student);
			// format price
			float p_student = (float) dish.priceStudent / 100;
			float p_employee = (float) dish.priceEmployee / 100;
			DecimalFormat df = new DecimalFormat("#.00");
			// set prices
			String stud = String.format(
					getResources().getString(R.string.price_stud),
					df.format(p_student));
			tv.setText(stud);
			tv = (TextView) findViewById(R.id.price_employee);
			String empl = String.format(
					getResources().getString(R.string.price_empl),
					df.format(p_employee));
			tv.setText(empl);
			// set ingredients
			for (Ingredient i : dish.ingredients) {
				String text = "";
				switch (i) {
				case PORK:
					text = "enthält Schweinefleisch";
					break;
				case BEEF:
					text = "enthält Rindfleisch";
					break;
				case VEGETARIAN:
					text = "vegetarisch";
					break;
				case ALCOHOL:
					text = "enthält Alkohol";
					break;
				case GARLIC:
					text = "enthält Knoblauch";
					break;
				case VEGAN:
					text = "vegan";
					break;

				}
				// add to view
				TextView ingr_tv = new TextView(getApplicationContext());
				ingr_tv.setTextAppearance(getApplicationContext(),
						android.R.style.TextAppearance_Medium);
				ingr_tv.setText(text);
				LinearLayout ingr_layout = (LinearLayout) findViewById(R.id.ingredients_layout);
				ingr_layout.addView(ingr_tv);
			}
		}

		setRatings();
		
        LinearLayout imageLayout = (LinearLayout) findViewById(R.id.dishPhotoLayout);
        final View image = loadPhoto();
        image.setPadding(0, 20, 0, 10);
        // append image to view
        imageLayout.addView(image);		        
        
        // allow to directly take photo on click
        image.setClickable(true);
        image.setOnClickListener(new OnClickListener(){

           @Override
           public void onClick(View arg0)
           {    
           Intent i = new Intent(SingleDishActivity.this, AddPhotoActivity.class);
           i.putExtra("d_id", Integer.parseInt(dish.id));
           i.putExtra("d_name", dish.name);
                       startActivity(i);
           }   
        });
		
		incrementViewCount();
	}

private View loadPhoto()
  {
   // load photo from mensa-page
   String photoURL = dish.photoUrl;

   // image available
   if (!photoURL.isEmpty()) return new LoaderImageView(this,photoURL,true);
   
   // no image available, try to use user image
   LoaderImageView image = this.loadUserPhoto();

   if (image != null) return image;

   // still nothing...
   NoImageView noPic = new NoImageView(getApplicationContext());
   noPic.setImageDrawable(getResources().getDrawable(R.drawable.noimage));

   return noPic;
  }
	
	private LoaderImageView loadUserPhoto()
	{
		RestClient rc = new RestClient(MensaActivity.basicURI + "dishes" + "/"
				+ dish.id + "/" + "photos");			
		
		if (!ConnectionChecker.isNetworkReachable(getApplicationContext(), true)) return null;
		
		try {
			rc.Execute(RequestMethod.GET);
		} catch (Exception e) {
			System.out.println("Fehler beim laden der PhotoURLs");
			return null;
		}
		
		if (rc.timeout) return null;
		
		String resp = rc.getResponse();
		try {
			// create parser
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();

			PhotoHandler photoHandler = new PhotoHandler();
			xr.setContentHandler(photoHandler);

			InputSource is = new InputSource(new StringReader(resp));
			xr.parse(is);
			
			// only fill list if parsing was successful
			List<String> photos = photoHandler.getPhotoIDs();
			
			if (photos.size() == 0) return null;
		
			String id = photos.get(photos.size()-1);
			final LoaderImageView image = new LoaderImageView(this,
					MensaActivity.basicURI + "photos" + "/" + id, true);

			return image;

		} catch (Exception e) {
			System.out.println("Fehler beim parsen / photos");
		}

		return null;		
	}

	private void setRatings() {

		// load data from REST if internet is available
		if (ConnectionChecker.isNetworkReachable(getApplicationContext(), true)) {
			// get RATINGS and set them
			// get data from REST
			RestClient rc = new RestClient(MensaActivity.basicURI + "dishes"
					+ "/" + dish.id + "/" + "ratings");
			try {
				rc.Execute(RequestMethod.GET);
			} catch (Exception e) {
				System.out.println("Fehler beim laden der Daten");
			}

			String resp = rc.getResponse();

			try {
				// create parser
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();

				ratingHandler = new RatingHandler();
				xr.setContentHandler(ratingHandler);

				InputSource is = new InputSource(new StringReader(resp));
				is.setEncoding("UTF-8");
				xr.parse(is);

				// get small RatingBars and set values
				RatingBar amountRb = (RatingBar) findViewById(R.id.amountRating);
				RatingBar spicinessRb = (RatingBar) findViewById(R.id.spicinessRating);
				RatingBar appearenceRb = (RatingBar) findViewById(R.id.appearenceRating);

				if (ratingHandler.getCount() > 0) {
					amountRb.setRating(ratingHandler.getAmount());
					spicinessRb.setRating(ratingHandler.getSpiciness());
					appearenceRb.setRating(ratingHandler.getAppearence());
				}

			} catch (Exception e) {
				System.out.println("Fehler beim parsen / Ratings");
				Toast.makeText(getApplicationContext(), R.string.error,
						Toast.LENGTH_SHORT).show();

			}
		} else {
			// no connection available
			Toast.makeText(getApplicationContext(), R.string.noConnection,
					Toast.LENGTH_SHORT).show();
		}

	}

	private void incrementViewCount() {

		// add data to REST if internet is available
		if (ConnectionChecker.isNetworkReachable(getApplicationContext(), true)) {
			RestClient rc = new RestClient(MensaActivity.basicURI + "dishes"
					+ "/" + dish.id + "/" + "views");
			try {
				rc.Execute(RequestMethod.POST);
			} catch (Exception e) {
				System.out.println("Fehler beim POST von ViewCount");
			}
		} else {
			// no connection available
			Toast.makeText(getApplicationContext(), R.string.noConnection,
					Toast.LENGTH_LONG).show();
		}

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RATING_ADDED) {
			if (resultCode == RESULT_OK) {
				Toast.makeText(getApplicationContext(),
						getResources().getText(R.string.ratingSuccessful),
						Toast.LENGTH_SHORT).show();
				setRatings();
			}
			if (resultCode == ERROR) {
				Toast.makeText(getApplicationContext(),
						getResources().getText(R.string.error),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	/*
	 * BUTTON METHODS BELOW
	 */
	public void showComments(View v) {
		Intent i = new Intent(SingleDishActivity.this, CommentActivity.class);
		i.putExtra("d_id", Integer.parseInt(dish.id));
		i.putExtra("d_name", dish.name);
		startActivity(i);

	}

	public void showPhotos(View v) {
		Intent i = new Intent(SingleDishActivity.this, PhotoActivity.class);
		i.putExtra("d_id", Integer.parseInt(dish.id));
		i.putExtra("d_name", dish.name);
		startActivity(i);

	}

	public void addRating(View v) {
		Intent i = new Intent(SingleDishActivity.this, AddRatingActivity.class);
		i.putExtra("d_id", Integer.parseInt(dish.id));
		i.putExtra("d_name", dish.name);
		startActivityForResult(i, RATING_ADDED);
	}
}
