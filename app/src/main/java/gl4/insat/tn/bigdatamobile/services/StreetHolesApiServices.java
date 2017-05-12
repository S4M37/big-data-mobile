package gl4.insat.tn.bigdatamobile.services;

public class StreetHolesApiServices {
    private static final StreetHolesApiServices ourInstance = new StreetHolesApiServices();

    public static StreetHolesApiServices getInstance() {
        return ourInstance;
    }

    private StreetHolesApiServices() {
    }
}
