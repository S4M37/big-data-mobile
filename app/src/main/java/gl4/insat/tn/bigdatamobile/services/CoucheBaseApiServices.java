package gl4.insat.tn.bigdatamobile.services;


import android.location.Location;
import android.util.Log;

import com.google.gson.JsonObject;

import java.util.Random;

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
                Log.d("response", "onResponse: " + response.message());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void trackUser(Location location, String address) {
        JsonObject jsonObject = Utils.getGson().fromJson("{\n" +
                "            \"id\": \"2wAx7Vv9BcIumkhAIf5PhQ\",\n" +
                "                \"doc_type\": \"point\",\n" +
                "                \"latitude\": " + location.getLatitude() + ",\n" +
                "                \"longitude\": " + location.getLongitude() + ",\n" +
                "                \"point_date\": \"2017-04-10 13:06:27 UTC\",\n" +
                "                \"speed\": " + location.getSpeed() + ",\n" +
                "                \"direction\": 2,\n" +
                "                \"street\": " + address + "\n" +
                "        }", JsonObject.class);

        Call<ResponseBody> call = Utils.getCouchBaseApiRetrofitInstance().trackUser(jsonObject);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void requestLiveTraffic(double[] start, double[] end) {

    }

}
