package de.lorbeer.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.util.Log;

/**
 * 
 * @author MacSystems {@link http
 *         ://www.androidpit.de/de/android/forum/thread/401021
 *         /isNetworkReachable#p487250}
 * 
 */
public class ConnectionChecker {
	private static final String LOG_TAG = ConnectionChecker.class
			.getSimpleName();

	/**
	 * Returns <code>true</code> when Network is reachable and connected.
	 * 
	 * @param _context
	 * @param useRoaming
	 * @return
	 */
	public static boolean isNetworkReachable(final Context _context,
			final boolean useRoaming) {
		boolean isNetworkReachable = false;
		final ConnectivityManager systemService = (ConnectivityManager) _context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		/**
		 * Avoid NullPointerException when offline
		 */
		if (systemService.getActiveNetworkInfo() == null) {
			Log.i(LOG_TAG, "Network not reachable!");
			return false;
		}

		final NetworkInfo[] infos = systemService.getAllNetworkInfo();
		boolean userWantIOWhileRoaming = useRoaming;
		for (final NetworkInfo info : infos) {
			if (info != null) {
				if (info.isConnectedOrConnecting()) {
					final State networkState = info.getState();
					final boolean isRoamingNow = info.isRoaming();
					if (State.CONNECTED == networkState
							|| State.CONNECTING == networkState) {
						// if (Logging.isEnabled)
						{
							Log.i(LOG_TAG, "Using: ");
							Log.i(LOG_TAG,
									"NetworkInfo.extraInfo : "
											+ info.getExtraInfo());
							Log.i(LOG_TAG,
									"NetworkInfo.reason : " + info.getReason());
							Log.i(LOG_TAG,
									"NetworkInfo.subtypeName : "
											+ info.getSubtypeName());
							Log.i(LOG_TAG,
									"NetworkInfo.state : " + info.getState());
							Log.i(LOG_TAG, "NetworkInfo.detailedState : "
									+ info.getDetailedState());
							Log.i(LOG_TAG,
									"NetworkInfo.isAvailable : "
											+ info.isAvailable());
							Log.i(LOG_TAG,
									"NetworkInfo.isConnected : "
											+ info.isConnected());
							Log.i(LOG_TAG,
									"NetworkInfo.isConnectedOrConnecting : "
											+ info.isConnectedOrConnecting());
							Log.i(LOG_TAG,
									"NetworkInfo.isFailover : "
											+ info.isFailover());
							Log.i(LOG_TAG,
									"NetworkInfo.isRoaming : "
											+ info.isRoaming());
						}
						if (userWantIOWhileRoaming) {
							isNetworkReachable = true;
							break;
						} else {
							isNetworkReachable = isRoamingNow ? false : true;
							break;
						}
					}
				}
			}
		}
		return isNetworkReachable;

	}
}