package diaro;

import org.jetbrains.annotations.Contract;

public class Location {
    public String title;
    public String location_uid;
    public String latitiude;
    public String longitude;
    public String address;
    public String zoom;

    //if address data is not available
    public Location(String title, String location_uid, String zoom) {
        this.title = title;
        this.location_uid = location_uid;
        this.zoom = zoom;
    }

    public Location(String location_uid, String latitiude, String longitude, String address, String zoom) {
        this.location_uid = location_uid;
        this.latitiude = latitiude;
        this.longitude = longitude;
        this.address = address;
        this.zoom = zoom;
    }

}
