package de.lorbeer.helper;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.DataFormatException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import de.lorbeer.app.R;

/**
 * Free for anyone to use, just say thanks and share
 * 
 * @author Blundell
 * 
 */
public class LoaderImageView extends LinearLayout {

	private static final int COMPLETE = 0;
	private static final int FAILED = 1;

	private Context mContext;
	private Drawable mDrawable;

	private ProgressBar mSpinner;
	private AspectRatioImageView mImage;

	private Boolean special = false;

	/**
	 * This is used when creating the view in XML To have an image load in XML
	 * use the tag
	 * 'image="http://developer.android.com/images/dialog_buttons.png"'
	 * Replacing the url with your desired image Once you have instantiated the
	 * XML view you can call setImageDrawable(url) to change the image
	 * 
	 * @param context
	 * @param attrSet
	 */
	public LoaderImageView(final Context context, final AttributeSet attrSet) {
		super(context, attrSet);
		final String url = attrSet.getAttributeValue(null, "image");
		if (url != null) {
			instantiate(context, url);
		} else {
			instantiate(context, null);
		}
	}

	/**
	 * This is used when creating the view programatically Once you have
	 * instantiated the view you can call setImageDrawable(url) to change the
	 * image
	 * 
	 * @param context
	 *            the Activity context
	 * @param imageUrl
	 *            the Image URL you wish to load
	 */
	public LoaderImageView(final Context context, final String imageUrl) {
		super(context);
		instantiate(context, imageUrl);
	}

	/**
	 * This is used when creating the view programatically Once you have
	 * instantiated the view you can call setImageDrawable(url) to change the
	 * image
	 * 
	 * @param context
	 *            the Activity context
	 * @param imageUrl
	 *            the Image URL you wish to load
	 * @param isSpecial
	 *            sets the accept-header to image/jpeg if true
	 * 
	 */
	public LoaderImageView(final Context context, final String imageUrl,
			Boolean isSpecial) {
		super(context);
		this.special = isSpecial;
		instantiate(context, imageUrl);
	}

	/**
	 * First time loading of the LoaderImageView Sets up the LayoutParams of the
	 * view, you can change these to get the required effects you want
	 */
	private void instantiate(final Context context, final String imageUrl) {
		mContext = context;

		mImage = new AspectRatioImageView(mContext);
		mImage.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		mImage.setAdjustViewBounds(true);

		mSpinner = new ProgressBar(mContext);
		mSpinner.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		mSpinner.setIndeterminate(true);

		addView(mSpinner);
		addView(mImage);

		if (imageUrl != null) {
			setImageDrawable(imageUrl);
		}
	}

	/**
	 * Set's the view's drawable, this uses the internet to retrieve the image
	 * don't forget to add the correct permissions to your manifest
	 * 
	 * @param imageUrl
	 *            the url of the image you wish to load
	 */
	public void setImageDrawable(final String imageUrl) {
		mDrawable = null;
		mSpinner.setVisibility(View.VISIBLE);
		mImage.setVisibility(View.GONE);
		new Thread() {
			public void run() {
				try {
					mDrawable = getDrawableFromUrl(imageUrl);
					imageLoadedHandler.sendEmptyMessage(COMPLETE);
				} catch (MalformedURLException e) {
					imageLoadedHandler.sendEmptyMessage(FAILED);
				} catch (IOException e) {
					imageLoadedHandler.sendEmptyMessage(FAILED);
				} catch (URISyntaxException e) {
					// added
					imageLoadedHandler.sendEmptyMessage(FAILED);
				} catch (DataFormatException e) {
					// added
					imageLoadedHandler.sendEmptyMessage(FAILED);
				}
			};
		}.start();
	}

	/**
	 * Callback that is received once the image has been downloaded
	 */
	private final Handler imageLoadedHandler = new Handler(new Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case COMPLETE:
				mImage.setImageDrawable(mDrawable);
				mImage.setVisibility(View.VISIBLE);
				mSpinner.setVisibility(View.GONE);
				break;
			case FAILED:
				System.out.println("ImageLoaderView Loading IMAGE failed!!");
			default:
				// error image
				mDrawable = getResources().getDrawable(R.drawable.error_image);
				mImage.setImageDrawable(mDrawable);
				mImage.setVisibility(View.VISIBLE);
				mSpinner.setVisibility(View.GONE);
				break;
			}
			return true;
		}
	});

	/**
	 * Pass in an image url to get a drawable object
	 * 
	 * @return a drawable object
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws DataFormatException
	 */
	private Drawable getDrawableFromUrl(final String url) throws IOException,
			MalformedURLException, URISyntaxException, DataFormatException {

		if (this.special) {

			URL p_url = new URL(url);
			HttpGet httpRequest = null;

			httpRequest = new HttpGet(p_url.toURI());
			// test accept header
			httpRequest.addHeader("Accept", "image/jpeg");

			HttpClient httpclient = new DefaultHttpClient();

			HttpResponse response = (HttpResponse) httpclient
					.execute(httpRequest);

			HttpEntity entity = response.getEntity();
			BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
			InputStream input = bufHttpEntity.getContent();

			Bitmap bitmap = BitmapFactory.decodeStream(input);
			if (bitmap == null) {
				throw new DataFormatException();
			}
			input.close();
			return new BitmapDrawable(bitmap);

		} else {
			return Drawable.createFromStream(
					((java.io.InputStream) new java.net.URL(url).getContent()),
					"name");
		}
	}

	/**
	 * 
	 * @author Patrick Boos {@link}
	 *         http://stackoverflow.com/questions/4677269/how-to-stretch
	 *         -three-images-across-the-screen-preserving-aspect-ratio
	 * 
	 */
	private class AspectRatioImageView extends ImageView {

		public AspectRatioImageView(Context context) {
			super(context);
		}

		public AspectRatioImageView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public AspectRatioImageView(Context context, AttributeSet attrs,
				int defStyle) {
			super(context, attrs, defStyle);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

			if (mDrawable != null) {
				int width = MeasureSpec.getSize(widthMeasureSpec);

				int height = width * mDrawable.getIntrinsicHeight()
						/ mDrawable.getIntrinsicWidth();

				setMeasuredDimension(width, height);
			} else {
				// evtl übergroße bilder
				Display display = ((WindowManager) mContext
						.getSystemService(Context.WINDOW_SERVICE))
						.getDefaultDisplay();
				int width = display.getWidth();
				int height = 100;
				setMeasuredDimension(width, height);
			}
		}
	}

}