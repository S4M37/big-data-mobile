package gl4.insat.tn.bigdatamobile.config;

public class Endpoints {

    /*  CouchBase Endpoint
     *  Run CouchBase as Docker container:
     *  docker run -d --name db -p 8091-8094:8091-8094 -p 11210:11210 couchbase
     *  Container runs under http://localhost:8091
     */

    public static final String COUCHEBASE_ENDPOINT = "http://192.168.1.3:8091/";
}
