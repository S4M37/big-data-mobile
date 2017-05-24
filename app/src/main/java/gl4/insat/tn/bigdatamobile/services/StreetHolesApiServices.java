package gl4.insat.tn.bigdatamobile.services;

import com.google.gson.JsonObject;

import gl4.insat.tn.bigdatamobile.utils.Utils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StreetHolesApiServices {
    private static final StreetHolesApiServices ourInstance = new StreetHolesApiServices();

    public static StreetHolesApiServices getInstance() {
        return ourInstance;
    }

    private StreetHolesApiServices() {
    }

    public void trackUserBroadCaster(int userId, String date, Boolean isBroadcastEnabled) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user", userId);
        jsonObject.addProperty("isBroadcastEnabled", isBroadcastEnabled);
        jsonObject.addProperty("timestamp", date);

        Call<ResponseBody> call = Utils.getStreetHolesApiRetrofitServices().trackUserBroadcaster(jsonObject);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void trackUserTraffic(int userId, String date, boolean isTrafficEnabled) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user", userId);
        jsonObject.addProperty("timestamp", date);
        jsonObject.addProperty("isTrafficEnabled", isTrafficEnabled);

        Call<ResponseBody> call = Utils.getStreetHolesApiRetrofitServices().trackUserTraffic(jsonObject);
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
