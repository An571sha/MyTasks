package diaro;

import org.dom4j.Element;
import org.jetbrains.annotations.Contract;

public class Location {
    public String title;
    public String location_uid;
    public String latitiude;
    public String longitude;
    public String zoom;

    //if address data is not available
    public Location(String title, String location_uid, String zoom) {
        this.title = title;
        this.location_uid = location_uid;
        this.zoom = zoom;
    }

    public Location(String location_uid, String latitiude, String longitude, String title, String zoom) {
        this.location_uid = location_uid;
        this.latitiude = latitiude;
        this.longitude = longitude;
        this.title = title;
        this.zoom = zoom;
    }

    public static void generateLocationTable(Location location , Element row ) {
        row.addElement(Entry.KEY_UID).addText(location.location_uid);
        row.addElement(Entry.KEY_ENTRY_LOCATION_NAME).addText(location.title);
        row.addElement(Entry.KEY_ENTRY_LOCATION_ZOOM).addText(location.zoom);
    }

}
