package gl4.insat.tn.bigdatamobile.config;

public class Endpoints {

    /*  CouchBase Endpoint
     *  Run CouchBase as Docker container:
     *  docker run -d --name db -p 8091-8094:8091-8094 -p 11210:11210 couchbase
     *  Container runs under http://localhost:8091
     */
    public static final String SERVER_IP = "192.168.43.88";
    public static final String COUCHEBASE_ENDPOINT = "http://" + SERVER_IP + ":8092/";
    public static final String COUCHEBASE_API_ENDPOINT = COUCHEBASE_ENDPOINT + "pools/default/buckets/";
    public static final String COUCHEBASE_BEER_SAMPLE_URI = "beer-sample/docs/";
    public static final String COUCHEBASE_MOBILE_URI = "celer_test/docs/";
    public static final String COUCHEBASE_MAPREDUCE_VIEW_URI = "/celer_test/_design/dev_spatialtest/_spatial/spatial_test?connection_timeout=60000&end_range=[177.8358850636653,177.8358850636653,500]&limit=600&skip=0&stale=false&start_range=[-177.8358850636653,+-177.8358850636653,+0]";

    public static final String COUCHEBASE_SYNC_GATEWAY = "http://" + SERVER_IP + ":4984/sync_gateway";

    public static final String STREET_HOLES_ENDPOINT = "http://ec2-54-77-104-232.eu-west-1.compute.amazonaws.com:3000/api/";
    public static final String STREET_HOLES_USER_REPORT_URI = "user-street-hole-report/";
    public static final String TRACK_USER_BROADCASTER_URI = "track/broadcast/";
    public static final String NEARBY_POTHOLES_URI = "elastic/holes/nearby";
}
