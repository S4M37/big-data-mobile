package gl4.insat.tn.bigdatamobile.services;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;

public class ConnectivityService {
    /*----Method to Check GPS is enable or disable ----- */
    public static Boolean displayGpsStatus(LocationManager location_manager) {
        //Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.GPS_PROVIDER);
        return location_manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /*----Method to Check Network is enable or disable ----- */
    public static boolean isOnline(Context context) {
        if (context != null) {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo() != null &&
                    cm.getActiveNetworkInfo().isConnectedOrConnecting();
        }
        return false;
    }

}
