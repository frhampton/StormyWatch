package stormy.yellowbear.com.stormywatch;

/**
 * Created by Eve on 7/3/2016.
 */
public class Coordinates {

    private String formattedAddress;
    private double latitude;
    private double longitude;



    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }


}
