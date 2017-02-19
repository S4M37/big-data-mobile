package gl4.insat.tn.bigdatamobile.utils;

import java.util.concurrent.TimeUnit;

import gl4.insat.tn.bigdatamobile.config.Endpoints;
import gl4.insat.tn.bigdatamobile.services.CouchBaseApiRetrofitServices;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Utils {

    //TIMEOUT with Seconds
    private static final long TIMEOUT = 15;

    // CouchBase Api retrofit Singleton implementation

    private static CouchBaseApiRetrofitServices couchBaseApiInstance;

    public static CouchBaseApiRetrofitServices getVyndApiRetrofitInstance() {
        if (couchBaseApiInstance == null) {

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient().newBuilder().addInterceptor(interceptor)
                    .connectTimeout(Utils.TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(Utils.TIMEOUT, TimeUnit.SECONDS).build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Endpoints.COUCHEBASE_ENDPOINT)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            couchBaseApiInstance = retrofit.create(CouchBaseApiRetrofitServices.class);
        }
        return couchBaseApiInstance;
    }
}
