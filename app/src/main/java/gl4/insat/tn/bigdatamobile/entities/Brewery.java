package gl4.insat.tn.bigdatamobile.entities;


import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class Brewery {
    @SerializedName("name")
    String Name;
    @SerializedName("city")
    String City;
    @SerializedName("state")
    String State;
    @SerializedName("code")
    String Code;
    @SerializedName("country")
    String Country;
    @SerializedName("phone")
    String Phone;
    @SerializedName("website")
    String WebSite;
    @SerializedName("type")
    String Type;
    @SerializedName("Updated")
    String Updated;
    @SerializedName("description")
    String Description;
    @SerializedName("address")
    String[] Address;
    Geo geo;

    @Override
    public String toString() {
        return "Brewery{" +
                "Name='" + Name + '\'' +
                ", City='" + City + '\'' +
                ", State='" + State + '\'' +
                ", Code='" + Code + '\'' +
                ", Country='" + Country + '\'' +
                ", Phone='" + Phone + '\'' +
                ", WebSite='" + WebSite + '\'' +
                ", Type='" + Type + '\'' +
                ", Updated='" + Updated + '\'' +
                ", Description='" + Description + '\'' +
                ", Address=" + Arrays.toString(Address) +
                ", geo=" + geo +
                '}';
    }
}
