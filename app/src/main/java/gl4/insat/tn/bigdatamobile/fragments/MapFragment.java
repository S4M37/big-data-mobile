package gl4.insat.tn.bigdatamobile.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import cn.refactor.lib.colordialog.PromptDialog;
import gl4.insat.tn.bigdatamobile.R;
import gl4.insat.tn.bigdatamobile.config.Endpoints;
import gl4.insat.tn.bigdatamobile.entities.RSSItemAdress;
import gl4.insat.tn.bigdatamobile.services.ConnectivityService;
import gl4.insat.tn.bigdatamobile.services.CoucheBaseApiServices;
import gl4.insat.tn.bigdatamobile.services.RSSParserStreetService;
import gl4.insat.tn.bigdatamobile.services.StreetHolesApiServices;
import gl4.insat.tn.bigdatamobile.utils.Utils;
import gl4.insat.tn.bigdatamobile.widgets.ChartDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.SENSOR_SERVICE;


public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SensorEventListener, LocationListener {

    private GoogleMap mMap;
    private MapView mapView;
    private ProgressBar progressBar;
    private View rootView;
    private GoogleMap googleMaps;
    private GoogleApiClient mGoogleApiClient;
    private Location myLastLocation;

    private int trafficInfo = 0;
    //couch internals
    protected static Manager manager;
    private Database database;
    private LiveQuery liveQuery;
    private LocationManager location_manager;
    private android.location.LocationListener location_listener;
    private ChartDialog chart;
    private boolean broadCastEnabled;

    private float streetHolesGravity;

    private SensorManager sensorMan;
    private Sensor accelerometer;

    private float[] mGravity;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    private String url_street;
    private ArrayList<RSSItemAdress> listStreet;
    private boolean isPoteHoleReport;
    private boolean isTrafficMode = true;

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
        sensorMan = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;


        try {
            startCBLite();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error Initializing CBLIte, see logs for details", Toast.LENGTH_LONG).show();
            Log.e("error", "Error initializing CBLite", e);
        }

        location_manager = (LocationManager) getContext().getSystemService(getContext().LOCATION_SERVICE);
        if (!ConnectivityService.isOnline(getContext())) {
            new PromptDialog(getContext())
                    .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                    .setAnimationEnable(true)
                    .setTitleText("Ooops")
                    .setContentText("We cannot establish connection to the server, Please check your network connection")
                    .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                        @Override
                        public void onClick(PromptDialog dialog) {
                            dialog.dismiss();
                        }
                    }).show();
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

        //createLocationItem("big Data mobile");

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

    private Document createLocationItem(Location location, String address) throws Exception {

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        UUID uuid = UUID.randomUUID();
        Calendar calendar = GregorianCalendar.getInstance();
        long currentTime = calendar.getTimeInMillis();
        String currentTimeString = dateFormatter.format(calendar.getTime());

        String id = currentTime + "-" + uuid.toString();

        Document document = database.createDocument();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("_id", id);
        properties.put("doc_type", "point");
        properties.put("latitude", location.getLatitude());
        properties.put("longitude", location.getLongitude());
        properties.put("point_date", currentTimeString);
        properties.put("speed", location.getSpeed());
        properties.put("direction", 2);
        properties.put("street", address);
        document.putProperties(properties);

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
        chart = (ChartDialog) rootView.findViewById(R.id.chart);
        final FloatingActionButton networkBroadCastFab = (FloatingActionButton) rootView.findViewById(R.id.network_broadcast);
        networkBroadCastFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                broadCastEnabled = !broadCastEnabled;
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                long currentTime = calendar.getTimeInMillis();
                String currentTimeString = dateFormatter.format(calendar.getTime());

                StreetHolesApiServices.getInstance().trackUserBroadCaster((int) (Math.random() * 100), currentTimeString, broadCastEnabled);
                if (broadCastEnabled) {
                    networkBroadCastFab.setImageResource(R.drawable.ic_action_network_wifi);
                    Toast.makeText(getContext(), "Broadcast enabled", Toast.LENGTH_SHORT).show();
                    requestSpeed();
                } else {
                    networkBroadCastFab.setImageResource(R.drawable.ic_action_signal_wifi_off);
                    Toast.makeText(getContext(), "Broadcast disabled", Toast.LENGTH_SHORT).show();
                    stopSpeedMonitoring();
                }
            }
        });
        final FloatingActionButton trafficFab = (FloatingActionButton) rootView.findViewById(R.id.mode_fab);
        trafficFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isTrafficMode = !isTrafficMode;
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Calendar calendar = GregorianCalendar.getInstance();
                long currentTime = calendar.getTimeInMillis();
                String currentTimeString = dateFormatter.format(calendar.getTime());

                //StreetHolesApiServices.getInstance().trackUserTraffic((int) (Math.random() * 100), currentTimeString, broadCastEnabled);
                if (isTrafficMode) {
                    trafficFab.setImageResource(R.drawable.traffic_mode);
                    Toast.makeText(getContext(), "Traffic Mode enabled", Toast.LENGTH_SHORT).show();
                    //requestSpeed();
                } else {
                    trafficFab.setImageResource(R.drawable.pothols_mode);
                    Toast.makeText(getContext(), "PotHoles mode enabled", Toast.LENGTH_SHORT).show();
                    //stopSpeedMonitoring();
                }
            }
        });
    }

    LatLng source;
    LatLng destination;

    MarkerOptions sourceMarker;
    MarkerOptions destinationMarker;

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMaps = googleMap;
        //this.progressBar.setVisibility(View.GONE);
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(36.804402, 10.165068), 11));

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                chart.openView();
            }
        });
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {

                if (source != null && destination != null) {
                    googleMap.clear();
                    source = null;
                    destination = null;
                    return;
                }

                if (source == null) {
                    source = latLng;
                    googleMaps.addMarker(new MarkerOptions().title("Source").position(source)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_start)).draggable(true));
                    Toast.makeText(getContext(), "Selectionnez votre destination maintenant...", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (destination == null) {
                    destination = latLng;
                    googleMaps.addMarker(new MarkerOptions().title("Source").position(destination)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_destination)).draggable(true));
                    if (isTrafficMode) {
                        requestLiveTraffic();
                    } else {
                        requestPotHoles();
                    }
                    return;
                }

                /*
                Toast.makeText(getContext(), "requesting traffic info ...", Toast.LENGTH_SHORT).show();
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                switch (trafficInfo) {
                                    case 0:
                                        showTraffcicFancyAlert(PromptDialog.DIALOG_TYPE_WRONG, "Traffic at this point {" +
                                                latLng.latitude + "," + latLng.longitude + "}");
                                        break;
                                    case 1:
                                        chart.openView();
                                        /*showTraffcicFancyAlert(PromptDialog.DIALOG_TYPE_WARNING, "Possible traffic at this point {" +
                                                latLng.latitude + "," + latLng.longitude + "}");
                                        break;
                                    case 2:
                                        showTraffcicFancyAlert(PromptDialog.DIALOG_TYPE_SUCCESS, "No traffic at this point {" +
                                                latLng.latitude + "," + latLng.longitude + "}");
                                        break;
                                }
                                trafficInfo += 1;
                                trafficInfo %= 3;

                                //chart.openView();
                            }
                        });

                    }
                }, 1000);
                */
            }
        });
        googleMaps.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {
                //chart.openView();
                return true;
            }
        });

        //LatLng latLng = new LatLng(36.804402, 10.165068);
        //googleMaps.addMarker(new MarkerOptions().title("traffic").position(latLng)
        //      .icon(BitmapDescriptorFactory.fromResource(R.drawable.parked_car)).draggable(true));
        startLocationUpdates();

    }

    private void requestPotHoles() {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("radius", 6);
        jsonObject.addProperty("lat", source.latitude);
        jsonObject.addProperty("lon", source.longitude);

        Call<ResponseBody> call = Utils.getStreetHolesApiRetrofitServices().getNearbyPotHoles(jsonObject);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("code", "onResponse: " + response.code());
                Log.d("message", "onResponse: " + response.message());

                if (response.code() != 200) {
                    return;
                }
                try {
                    JSONObject jsonObject1 = new JSONObject(response.body().string());
                    JSONArray jsonArray = jsonObject1.getJSONArray("holes");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject hole = jsonArray.getJSONObject(0);
                        LatLng latLng = new LatLng(hole.getJSONObject("_source").getJSONObject("location").getDouble("lat"),
                                hole.getJSONObject("_source").getJSONObject("location").getDouble("lon"));
                        googleMaps.addMarker(new MarkerOptions().title("Source").position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_pothol)).draggable(true));

                    }
                    if (jsonArray.length() == 0) {
                        showTraffcicFancyAlert(PromptDialog.DIALOG_TYPE_SUCCESS, "No PotHole at this road {}", "PotHoles Info");

                    }
                } catch (JSONException | IOException | NullPointerException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void requestLiveTraffic() {
        double[] start = {source.latitude, source.longitude};
        double[] end = {destination.latitude, destination.longitude};
        //CoucheBaseApiServices.getInstance().requestLiveTraffic(start, end);
        //Call<ResponseBody> call = Utils.getCouchBaseApiRetrofitInstance().requestLiveTraffic(start, end, false);
        Call<ResponseBody> call = Utils.getCouchBaseApiRetrofitInstance().requestLiveTraffic();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("code", "onResponse: " + response.code());
                Log.d("message", "onResponse: " + response.message());
                if (response.code() != 200) {
                    return;
                }
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    int size = jsonObject.getJSONArray("rows").length();
                    if (size > 40) {
                        showTraffcicFancyAlert(PromptDialog.DIALOG_TYPE_WRONG, "Traffic at this road {}", "Traffic info");
                        return;
                    }
                    if (size < 20) {
                        showTraffcicFancyAlert(PromptDialog.DIALOG_TYPE_SUCCESS, "No Traffic at this road {}", "Traffic info");
                        return;
                    }
                    if (size > 20) {
                        showTraffcicFancyAlert(PromptDialog.DIALOG_TYPE_WARNING, "Possible traffic at this road {}", "Traffic info");
                        return;
                    }
                } catch (JSONException | IOException | NullPointerException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    void showTraffcicFancyAlert(int dialogType, String s, String title) {
        new PromptDialog(getContext())
                .setDialogType(dialogType)
                .setAnimationEnable(true)
                .setTitleText(title)
                .setContentText(s)
                .setPositiveListener("Ok", new PromptDialog.OnPositiveListener() {
                    @Override
                    public void onClick(PromptDialog dialog) {
                        dialog.dismiss();
                    }
                }).show();
    }
    //selectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.pin_coffee_empty));

    private void reinitMarker() {

    }

    private static final int UPDATE_INTERVAL = 10000;
    private static final int FASTEST_INTERVAL = 5000;

    protected void startLocationUpdates() {
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
            //Toast.makeText(getContext(), myLastLocation.getLatitude() + " " + myLastLocation.getLongitude(), Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            googleMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLastLocation.getLatitude(),
                    myLastLocation.getLongitude()), 14));
            googleMaps.setMyLocationEnabled(true);

            CoucheBaseApiServices.getInstance().sendCurrentLocation(myLastLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
        sensorMan.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        sensorMan.unregisterListener(this);
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
        mapView.onStart();
        super.onStart();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        mapView.onStop();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    void requestSpeed() {
        location_listener = new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("onLocationChanged : ", "Latitude : " + location.getLatitude() +
                        " | Longitude : " + location.getLongitude() + " | Speed : " + location.getSpeed());
                if (location.getSpeed() > 0) {
                    isPoteHoleReport = false;
                    myLastLocation = location;
                    getAdress(location);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (location_manager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
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
        if (location_manager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
            location_manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, location_listener);
        if (location_manager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
            location_manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    0, 0, location_listener);
    }

    void stopSpeedMonitoring() {
        location_manager.removeUpdates(location_listener);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values.clone();
            // Shake detection
            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            // Make this higher or lower according to how much
            // motion you want to detect
            if (mAccel > 20) {
                streetHolesGravity = mAccel;
                isPoteHoleReport = true;
                Log.d("streetreport", "onSensorChanged: " + mAccel);
                startLocationUpdates();
            }
        }
    }

    public void getAdress(Location location) {
        url_street = "https://maps.googleapis.com/maps/api/geocode/xml?latlng=" +
                location.getLatitude() + "," + location.getLongitude() +
                "&key=AIzaSyB01Lj0dPxIFOzgwUGL_tkLTpSScW58EM0";
        Log.d("url_street", "getAdress: " + url_street);
        listStreet = new ArrayList<>();
        new RetrieveRSSFeeds().execute();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            getAdress(location);
        }
    }

    private class RetrieveRSSFeeds extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            retrieveRSSFeed_street(url_street, listStreet);
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            String address = Utils.getAdress(listStreet);
            if (listStreet.size() == 0) {
                return;
            }
            if (!isPoteHoleReport) {
                try {
                    createLocationItem(myLastLocation, address);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //CoucheBaseApiServices.getInstance().trackUser(myLastLocation, address);
                return;
            }
            if (mAccel < 20) {
                return;
            }
            mAccel = 0;
            Log.d("isPoteHoleReport", "onPostExecute: " + isPoteHoleReport);
            //send street hole report
            JsonObject requestParams = new JsonObject();
            requestParams.addProperty("clientId", "121");
            JsonObject location = new JsonObject();
            location.addProperty("lat", myLastLocation.getLatitude());
            location.addProperty("lon", myLastLocation.getLongitude());
            requestParams.add("location", location);
            requestParams.addProperty("address", address);
            requestParams.addProperty("locality", listStreet.get(1).adr());
            Log.d("requestParams", "onPostExecute: " + requestParams.toString());
            Utils.getStreetHolesApiRetrofitServices().reportStreetHole(requestParams).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d("streetreport", "on Report Response [Code]: " + response.code());
                    Log.d("streetreport", "on Report Response [message]: " + response.message());
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                }
            });

            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }


        private void retrieveRSSFeed_street(String urlToRssFeed, ArrayList<RSSItemAdress> list) {
            try {
                URL url = new URL(urlToRssFeed);
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();
                XMLReader xmlreader = parser.getXMLReader();
                RSSParserStreetService theRssHandler = new RSSParserStreetService(list);
                xmlreader.setContentHandler(theRssHandler);
                InputSource is = new InputSource(url.openStream());
                xmlreader.parse(is);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
