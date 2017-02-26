package gl4.insat.tn.bigdatamobile.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import gl4.insat.tn.bigdatamobile.R;
import gl4.insat.tn.bigdatamobile.activities.MainActivity;
import gl4.insat.tn.bigdatamobile.entities.Brewery;
import gl4.insat.tn.bigdatamobile.services.CouchBaseApiRetrofitServices;
import gl4.insat.tn.bigdatamobile.utils.Utils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeFragment extends Fragment {

    public static HomeFragment newInstance() {

        Bundle args = new Bundle();

        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    Button retryButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home_fragment, container, false);
        initializeView(rootView);
        return rootView;

    }
    

    public void initializeView(View rootView) {
        retryButton = (Button) rootView.findViewById(R.id.retry_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDocById("21st_amendment_brewery_cafe");
            }
        });
        Button mapButton = (Button) rootView.findViewById(R.id.map_button);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).showFragment(MapFragment.newInstance());
            }
        });
    }


    public void getDocById(String docId) {
        CouchBaseApiRetrofitServices couchBaseApiRetrofitServices = Utils.getVyndApiRetrofitInstance();
        Call<ResponseBody> call = couchBaseApiRetrofitServices.getDocById(docId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    jsonObject = jsonObject.getJSONObject("json");
                    Brewery brewery = Utils.getGson().fromJson(String.valueOf(jsonObject), Brewery.class);
                    Log.d("CouchBase : ", "onResponse: " + brewery.toString());
                    Toast.makeText(getContext(), brewery.toString(), Toast.LENGTH_LONG).show();
                } catch (IOException | JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });


        // Couchbase Java SDK
        // TODO: 19/02/2017 Bug: java.lang.NoClassDefFoundError: Failed resolution of: Ljava/lang/management/ManagementFactory;
        /*
        Bucket bucket = Utils.getBucketCluster("beer-sample");
        N1qlQueryResult result = bucket.query(N1qlQuery.simple("Select * from `beer-sample` where city = 'San Francisco' LIMIT 2"));

        for (N1qlQueryRow row : result) {
            Log.d("CouchBase SDK :", String.valueOf(row.value()));
        }
        */
    }
}
