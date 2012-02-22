package de.lorbeer.app;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.thoughtworks.xstream.XStream;

import de.lorbeer.helper.ConnectionChecker;
import de.lorbeer.rest.RequestMethod;
import de.lorbeer.rest.RestClient;
import de.mmt.lorbeerblatt.data.Rating;

public class AddRatingActivity extends Activity {
	private int d_id;
	private static final int ERROR = 9999;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_rating);
		d_id = getIntent().getExtras().getInt("d_id");
		TextView tv = (TextView) findViewById(R.id.dishName);
		tv.setText(getIntent().getExtras().getString("d_name"));
	}

	public void addRating(View v) {
		Boolean noRating = false;
		// get data from RatingBars
		RatingBar amountRb = (RatingBar) findViewById(R.id.myAmountRatingBar);
		float amount = amountRb.getRating();
		RatingBar spicinessRb = (RatingBar) findViewById(R.id.mySpicinessRatingBar);
		float spiciness = spicinessRb.getRating();
		RatingBar appearenceRb = (RatingBar) findViewById(R.id.myAppearenceRatingBar);
		float appearence = appearenceRb.getRating();
		if (amount == 0 && spiciness == 0 && appearence == 0) {
			noRating = true;
		}
		if (!noRating) {
			// get author name
			EditText ratingAuthorName = (EditText) findViewById(R.id.ratingAuthorName);
			String author = ratingAuthorName.getText().toString();

			// send data to webservice
			String id = "";
			Rating rating = new Rating(id, author, Math.round(amount),
					Math.round(spiciness), Math.round(appearence));
			rating.authorName = author;
			rating.dishId = Integer.toString(d_id);
			rating.id = "0";

			String xml = "";

			try {
				// serialize data
				XStream xs = new XStream();
				xs.alias("rating", Rating.class);
				xml = xs.toXML(rating);
				System.out.println(xml);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// HTTP POST
			RestClient rc = new RestClient(MensaActivity.basicURI + "dishes"
					+ "/" + d_id + "/" + "ratings");
			try {
				rc.setEntity(xml);
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}

			if (ConnectionChecker.isNetworkReachable(getApplicationContext(),
					true)) {
				try {
					rc.Execute(RequestMethod.POST);
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Fehler beim Senden der Daten");
				}

				// finish for result
				if (rc.getResponseCode() == 201) {
					setResult(RESULT_OK);
				} else {
					setResult(ERROR);
				}
				finish();
			} else {
				// no connection available
				Toast.makeText(getApplicationContext(), R.string.noConnection,
						Toast.LENGTH_SHORT).show();

			}
		}

		// tell user that no rating was made by him
		if (noRating) {
			// make Toast
			Toast.makeText(getApplicationContext(), R.string.noRatingInput,
					Toast.LENGTH_SHORT).show();
		}
	}
}
