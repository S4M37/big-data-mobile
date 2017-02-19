package gl4.insat.tn.bigdatamobile.config;

public class Endpoints {

    /*  CouchBase Endpoint
     *  Run CouchBase as Docker container:
     *  docker run -d --name db -p 8091-8094:8091-8094 -p 11210:11210 couchbase
     *  Container runs under http://localhost:8091
     */
    public static final String SERVER_IP = "192.168.1.3";
    public static final String COUCHEBASE_ENDPOINT = "http://" + SERVER_IP + ":8091/";
    public static final String COUCHEBASE_API_ENDPOINT = COUCHEBASE_ENDPOINT + "pools/default/buckets/";
    public static final String COUCHEBASE_BEER_SAMPLE_URI = "beer-sample/docs/";
}
