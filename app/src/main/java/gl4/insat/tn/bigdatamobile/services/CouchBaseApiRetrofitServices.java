package gl4.insat.tn.bigdatamobile.services;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface CouchBaseApiRetrofitServices {

    @GET("/")
    Call<ResponseBody> getInfo();
}
