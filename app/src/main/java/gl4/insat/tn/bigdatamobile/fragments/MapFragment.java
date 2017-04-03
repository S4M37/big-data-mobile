package gl4.insat.tn.bigdatamobile.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import gl4.insat.tn.bigdatamobile.R;
import gl4.insat.tn.bigdatamobile.config.Endpoints;
import gl4.insat.tn.bigdatamobile.entities.User;
import gl4.insat.tn.bigdatamobile.services.CouchBaseApiRetrofitServices;
import gl4.insat.tn.bigdatamobile.utils.Utils;


public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private MapView mapView;
    private ProgressBar progressBar;
    private View rootView;
    private GoogleMap googleMaps;
    private GoogleApiClient mGoogleApiClient;
    private Location myLastLocation;


    //couch internals
    protected static Manager manager;
    private Database database;
    private LiveQuery liveQuery;

    public static MapFragment newInstance() {

        Bundle args = new Bundle();

        MapFragment fragment = new MapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        try {
            startCBLite();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error Initializing CBLIte, see logs for details", Toast.LENGTH_LONG).show();
            Log.e("error", "Error initializing CBLite", e);
        }

    }

    protected void startCBLite() throws Exception {


        manager = new Manager(new AndroidContext(getContext()), Manager.DEFAULT_OPTIONS);

        //install a view definition needed by the application
        DatabaseOptions options = new DatabaseOptions();
        options.setCreate(true);
        database = manager.openDatabase("sync_gateway", options);
        /*
        com.couchbase.lite.View viewItemsByDate = database.getView(String.format("%s/%s", designDocName, byDateViewName));
        viewItemsByDate.setMap(new Mapper() {
            @Override
            public void map(Map<String, Object> document, Emitter emitter) {
                Object createdAt = document.get("created_at");
                if (createdAt != null) {
                    emitter.emit(createdAt.toString(), null);
                }
            }
        }, "1.0");
        */
        //initItemListAdapter();

        //startLiveQuery(viewItemsByDate);

        startSync();

        createGroceryItem("big Data mobile");

    }

    private void startSync() {

        URL syncUrl;
        try {
            syncUrl = new URL(Endpoints.COUCHEBASE_SYNC_GATEWAY);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        Replication pullReplication = database.createPullReplication(syncUrl);
        pullReplication.setContinuous(true);

        Replication pushReplication = database.createPushReplication(syncUrl);
        pushReplication.setContinuous(true);

        pullReplication.start();
        pushReplication.start();

        //pullReplication.addChangeListener(this);
        //pushReplication.addChangeListener(this);

    }

    private Document createGroceryItem(String text) throws Exception {

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        UUID uuid = UUID.randomUUID();
        Calendar calendar = GregorianCalendar.getInstance();
        long currentTime = calendar.getTimeInMillis();
        String currentTimeString = dateFormatter.format(calendar.getTime());

        String id = currentTime + "-" + uuid.toString();

        Document document = database.createDocument();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("_id", id);
        properties.put("text", text);
        properties.put("check", Boolean.FALSE);
        properties.put("created_at", currentTimeString);
        document.putProperties(properties);

        Log.d("couchebase", "Created new grocery item with id: " + document.getId());

        return document;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.map_fragment, container, false);
        initializeView();
        return rootView;
    }


    private void initializeMapView(View rootView, Bundle savedInstanceState) {
        mapView = (MapView) rootView.findViewById(R.id.map_view);
        //MapsInitializer.initialize(getContext());
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeMapView(rootView, savedInstanceState);
    }

    private void initializeView() {
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMaps = googleMap;
        //this.progressBar.setVisibility(View.GONE);
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(36.804402, 10.165068), 11));

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Toast.makeText(getContext(), "onMapClick", Toast.LENGTH_SHORT).show();
            }
        });
        googleMaps.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });

        LatLng latLng = new LatLng(36.804402, 10.165068);
        googleMaps.addMarker(new MarkerOptions().title("traffic").position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.parked_car)).draggable(true));
        startLocationUpdates();

    }

    //selectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.pin_coffee_empty));

    private void reinitMarker() {

    }

    private static final int UPDATE_INTERVAL = 10000;
    private static final int FASTEST_INTERVAL = 5000;

    protected void startLocationUpdates() {
        Log.d("startLocationUpdates()", "startLocationUpdates: ");
        // Create the location request
        LocationRequest mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        if (mGoogleApiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Log.d("location", "startLocationUpdates: ");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, (LocationListener) this);
        } else {
            mGoogleApiClient.connect();
        }
    }

    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        myLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (myLastLocation != null) {
            Toast.makeText(getContext(), myLastLocation.getLatitude() + " " + myLastLocation.getLongitude(), Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            googleMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLastLocation.getLatitude(),
                    myLastLocation.getLongitude()), 14));
            googleMaps.setMyLocationEnabled(true);

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

            /*
            Call<ResponseBody> call = couchBaseApiRetrofitServices.addDocById(dat, jsonObject);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
            */
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
