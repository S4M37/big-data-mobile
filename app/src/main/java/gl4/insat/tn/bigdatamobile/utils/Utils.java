package gl4.insat.tn.bigdatamobile.utils;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.google.gson.Gson;

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

    public static CouchBaseApiRetrofitServices getCouchBaseApiRetrofitInstance() {
        if (couchBaseApiInstance == null) {

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient().newBuilder().addInterceptor(interceptor)
                    .connectTimeout(Utils.TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(Utils.TIMEOUT, TimeUnit.SECONDS).build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Endpoints.COUCHEBASE_API_ENDPOINT)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            couchBaseApiInstance = retrofit.create(CouchBaseApiRetrofitServices.class);
        }
        return couchBaseApiInstance;
    }

    private static Gson gson;

    public static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    private static Bucket bucket;

    public static Bucket getBucketCluster(String bucketName) {
        if (bucket == null) {
            // Create a cluster reference
            CouchbaseCluster cluster = CouchbaseCluster.create(Endpoints.SERVER_IP);
            // Connect to the bucket and open it
            bucket = cluster.openBucket(bucketName);
        }
        return bucket;
    }

    public static int add(int a,int b){
        return a+b;
    }
}
