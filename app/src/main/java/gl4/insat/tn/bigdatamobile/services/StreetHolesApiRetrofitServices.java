package gl4.insat.tn.bigdatamobile.services;


import com.google.gson.JsonObject;

import gl4.insat.tn.bigdatamobile.config.Endpoints;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface StreetHolesApiRetrofitServices {
    @POST(Endpoints.STREET_HOLES_USER_REPORT_URI)
    Call<ResponseBody> reportStreetHole(@Body JsonObject jsonObject);

    @POST(Endpoints.TRACK_USER_BROADCASTER_URI)
    Call<ResponseBody> trackUserBroadcaster(@Body JsonObject jsonObject);

    Call<ResponseBody> trackUserTraffic(JsonObject jsonObject);

    @POST(Endpoints.NEARBY_POTHOLES_URI)
    Call<ResponseBody> getNearbyPotHoles(@Body JsonObject jsonObject);
}
