package de.lorbeer.app;

import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.lorbeer.handler.PhotoHandler;
import de.lorbeer.helper.ConnectionChecker;
import de.lorbeer.helper.LoaderImageView;
import de.lorbeer.rest.RequestMethod;
import de.lorbeer.rest.RestClient;

public class PhotoActivity extends Activity {
	private int d_id;
	private PhotoHandler photoHandler;
	private static final int PHOTO_ADDED = 1337;
	private static final int ERROR = 9999;

	TextView noImage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo);
		d_id = getIntent().getExtras().getInt("d_id");

		TextView tv = (TextView) findViewById(R.id.PhotoDishName);
		tv.setText(getIntent().getExtras().getString("d_name"));

		loadURLs();

	}

	private void loadURLs() {
		// get all photos of current DISH
		// get data from REST
		RestClient rc = new RestClient(MensaActivity.basicURI + "dishes" + "/"
				+ d_id + "/" + "photos");

		if (ConnectionChecker.isNetworkReachable(getApplicationContext(), true)) {
			try {
				rc.Execute(RequestMethod.GET);
			} catch (Exception e) {
				System.out.println("Fehler beim laden der PhotoURLs");

			}
		} else {
			// no connection available
			Toast.makeText(getApplicationContext(), R.string.noConnection,
					Toast.LENGTH_SHORT).show();
		}

		if (!rc.timeout) {
			String resp = rc.getResponse();
			try {
				// create parser
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();

				photoHandler = new PhotoHandler();
				xr.setContentHandler(photoHandler);

				InputSource is = new InputSource(new StringReader(resp));
				xr.parse(is);
				// only fill list if parsing was successful
				fillList();

			} catch (Exception e) {
				System.out.println("Fehler beim parsen / photos");
			}
		} else {
			Toast.makeText(getApplicationContext(), R.string.error,
					Toast.LENGTH_SHORT).show();
		}
	}

	private void fillList() {
		List<String> photos = photoHandler.getPhotoIDs();
		for (int i = 0; i < photos.size(); i++) {
			String id = photos.get(i);
			final LoaderImageView image = new LoaderImageView(this,
					MensaActivity.basicURI + "photos" + "/" + id, true);
			// add padding for all elements
			image.setPadding(0, 0, 0, 10);
			// add image to full list
			LinearLayout imageLayout = (LinearLayout) findViewById(R.id.photoLayout);
			imageLayout.addView(image, i);
		}
		// hint if no images
		if (photos.size() <= 0) {
			noImage = new TextView(getApplicationContext());
			noImage.setText(R.string.noPhotosToShow);
			noImage.setPadding(0, 20, 0, 0);
			noImage.setGravity(0x00000011);

			LinearLayout imageLayout = (LinearLayout) findViewById(R.id.photoLayout);
			imageLayout.addView(noImage);

		}

	}

	public void takePhoto(View v) {
		Intent i = new Intent(PhotoActivity.this, AddPhotoActivity.class);

		i.putExtra("d_id", d_id);
		startActivityForResult(i, PHOTO_ADDED);
	}

	@SuppressWarnings("static-access")
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PHOTO_ADDED) {
			if (resultCode == RESULT_OK) {
				Toast.makeText(getApplicationContext(),
						getResources().getText(R.string.photoSuccessful),
						Toast.LENGTH_SHORT).show();
				// disable textView noImages
				if (noImage != null) {
					noImage.setVisibility(noImage.GONE);
				}
				LinearLayout imageLayout = (LinearLayout) findViewById(R.id.photoLayout);
				imageLayout.removeAllViews();
				loadURLs();
			}
			if (resultCode == ERROR) {
				Toast.makeText(getApplicationContext(),
						getResources().getText(R.string.error),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

}
