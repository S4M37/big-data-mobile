package gl4.insat.tn.bigdatamobile.entities;

import com.google.gson.annotations.SerializedName;

public class Geo {
    @SerializedName("accuracy")
    String Accuracy;
    @SerializedName("lat")
    String Lat;
    @SerializedName("lon")
    String Lon;

    @Override
    public String toString() {
        return "Geo{" +
                "Accuracy='" + Accuracy + '\'' +
                ", Lat='" + Lat + '\'' +
                ", Lon='" + Lon + '\'' +
                '}';
    }
}
