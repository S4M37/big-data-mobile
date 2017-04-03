package gl4.insat.tn.bigdatamobile.services;


import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

import gl4.insat.tn.bigdatamobile.entities.Brewery;
import gl4.insat.tn.bigdatamobile.entities.User;
import gl4.insat.tn.bigdatamobile.utils.Utils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CoucheBaseApiServices {
    private static final CoucheBaseApiServices ourInstance = new CoucheBaseApiServices();

    public static CoucheBaseApiServices getInstance() {
        return ourInstance;
    }

    private CoucheBaseApiServices() {
    }

    public void sendCurrentLocation(Location myLastLocation) {
        CouchBaseApiRetrofitServices couchBaseApiRetrofitServices = Utils.getCouchBaseApiRetrofitInstance();

        Random ran = new Random();

        char data = ' ';
        String dat = "";
        for (int i = 0; i <= 30; i++) {
            data = (char) (ran.nextInt(25) + 97);
            dat = data + dat;
        }
        User user = new User();
        user._userId = dat;
        user.lat = myLastLocation.getLatitude();
        user.lng = myLastLocation.getLongitude();
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("_userId", dat);
        jsonObject.addProperty("lat", myLastLocation.getLatitude());
        jsonObject.addProperty("lng", myLastLocation.getLongitude());
        Log.d("add doc", "onConnected: " + jsonObject.toString());

        Call<ResponseBody> call = couchBaseApiRetrofitServices.addDocById(dat, jsonObject);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

}
