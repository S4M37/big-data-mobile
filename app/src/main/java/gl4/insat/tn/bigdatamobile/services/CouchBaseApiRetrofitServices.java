package gl4.insat.tn.bigdatamobile.services;


import com.google.gson.JsonObject;

import gl4.insat.tn.bigdatamobile.config.Endpoints;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CouchBaseApiRetrofitServices {

    @GET(Endpoints.COUCHEBASE_BEER_SAMPLE_URI + "{doc_id}")
    Call<ResponseBody> getDocById(@Path("doc_id") String docId);

    @POST(Endpoints.COUCHEBASE_MOBILE_URI + "{doc_id}")
    Call<ResponseBody> addDocById(@Path("doc_id") String docId, @Body JsonObject jsonObject);

    @POST("/track-user")
    Call<ResponseBody> trackUser(@Body JsonObject jsonObject);

    @GET(Endpoints.COUCHEBASE_MAPREDUCE_VIEW_URI)
   // Call<ResponseBody> requestLiveTraffic(@Query("start_range") double[] start, @Query("end_range") double[] end, @Query("stale") boolean stale);
    Call<ResponseBody> requestLiveTraffic();
}
