package gl4.insat.tn.bigdatamobile.services;


import gl4.insat.tn.bigdatamobile.config.Endpoints;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CouchBaseApiRetrofitServices {

    @GET(Endpoints.COUCHEBASE_BEER_SAMPLE_URI + "{doc_id}")
    Call<ResponseBody> getDocById(@Path("doc_id") String docId);
}
