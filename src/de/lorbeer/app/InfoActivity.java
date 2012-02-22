package de.lorbeer.app;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class InfoActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info);

		TextView version = (TextView) findViewById(R.id.versionText);
		String v = "";
		try {
			v = "v "
					+ getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.e("tag", e.getMessage());
		}
		version.setText(v);
	}
}
