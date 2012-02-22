package de.lorbeer.app;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.thoughtworks.xstream.XStream;

import de.lorbeer.helper.ConnectionChecker;
import de.lorbeer.rest.RequestMethod;
import de.lorbeer.rest.RestClient;
import de.mmt.lorbeerblatt.data.Comment;

public class AddCommentActivity extends Activity {
	private int d_id;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_comment);

		// get id of current dish
		d_id = getIntent().getExtras().getInt("d_id");
	}

	public void sendComment(View v) throws Exception {
		// read data
		String author = ((EditText) findViewById(R.id.commentAuthor)).getText()
				.toString().trim();
		String text = ((EditText) findViewById(R.id.commentText)).getText()
				.toString().trim();

		if (author.equals("") || text.equals("")) {
			Toast.makeText(getApplicationContext(), R.string.wrongInputData,
					Toast.LENGTH_SHORT).show();
			return;
		}

		// create comment obj
		Comment comment = new Comment();
		comment.authorName = author;
		comment.text = text;
		comment.dishId = Integer.toString(d_id);

		// all data OK, send to REST
		String xml = "";

		try {
			// serialize data
			XStream xs = new XStream();
			xs.alias("comment", Comment.class);
			xml = xs.toXML(comment);
			System.out.println(xml);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getLocalizedMessage(),
					Toast.LENGTH_SHORT).show();
			return;
		}

		// send data to RESTservice
		RestClient rc = new RestClient(MensaActivity.basicURI + "dishes" + "/"
				+ d_id + "/comments");

		try {
			System.out.println(xml);
			rc.setEntity(xml);
		} catch (UnsupportedEncodingException e) {
			Toast.makeText(getApplicationContext(), e.getLocalizedMessage(),
					Toast.LENGTH_SHORT).show();
		}

		// no connection available
		if (!ConnectionChecker
				.isNetworkReachable(getApplicationContext(), true)) {
			Toast.makeText(getApplicationContext(), R.string.noConnection,
					Toast.LENGTH_SHORT).show();
			return;
		}

		rc.Execute(RequestMethod.POST);

		// finish for result
		if (rc.getResponseCode() == 201) {
			setResult(RESULT_OK);
			finish();
		} else {
			Toast.makeText(getApplicationContext(),
					"Übertragungsfehler " + rc.getResponseCode(),
					Toast.LENGTH_SHORT).show();
		}
	}

}