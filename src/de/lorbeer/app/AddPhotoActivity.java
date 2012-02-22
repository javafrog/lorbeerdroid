package de.lorbeer.app;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import de.lorbeer.helper.ConnectionChecker;

public class AddPhotoActivity extends Activity {
	private int d_id;

	private Bitmap thumbnail;
	private Bitmap temp;
	Uri uri;
	// The new size we want to scale to
	final int REQUIRED_SIZE = 300;

	private static final int CAMERA_PIC_REQUEST = 1337;
	private static final int ERROR = 9999;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.add_photo);
		// only for presentation
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// END
		d_id = getIntent().getExtras().getInt("d_id");

		// show unclickable send button
		Button send = (Button) findViewById(R.id.sendPhoto);
		send.setClickable(false);
		send.setTextColor(0xff888888);

	}

	public void takePhoto(View v) {
		Intent cameraIntent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
	}

	public void sendPhoto(View v) throws ClientProtocolException, IOException {
		if (ConnectionChecker.isNetworkReachable(getApplicationContext(), true)) {
			Boolean error = false;

			// HTTP Client
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(MensaActivity.basicURI + "dishes"
					+ "/" + d_id + "/" + "photos");

			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				temp.compress(CompressFormat.JPEG, 100, bos);
				byte[] data = bos.toByteArray();

				MultipartEntity entity = new MultipartEntity();
				entity.addPart("username", new StringBody(
						"anonymousAndroidUser"));
				entity.addPart("file", new ByteArrayBody(data, "myImage.jpg"));

				post.setEntity(entity);

				HttpResponse resp = client.execute(post);

				if (resp.getStatusLine().getStatusCode() == 201) {
					// all ok
					error = false;
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			if (!error) {
				// 201 OK finish
				setResult(RESULT_OK);
				finish();
			} else {
				// error
				setResult(ERROR);
				finish();
			}
		} else {
			// no connection available
			Toast.makeText(getApplicationContext(), R.string.noConnection,
					Toast.LENGTH_SHORT).show();
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_OK) {
			thumbnail = (Bitmap) data.getExtras().get("data");
			temp = (Bitmap) data.getExtras().get("data");
			ImageView image = (ImageView) findViewById(R.id.userPhoto);
			image.setImageBitmap(thumbnail);

			// set send button clickable
			Button send = (Button) findViewById(R.id.sendPhoto);
			send.setClickable(true);
			send.setTextColor(0xff000000);

		} else {
			// error
			Toast.makeText(getApplicationContext(), R.string.error,
					Toast.LENGTH_SHORT).show();
		}
	}

}
