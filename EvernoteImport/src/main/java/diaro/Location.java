package diaro;

import org.dom4j.Element;
import org.jetbrains.annotations.Contract;

public class Location {
    public String title;
    public String location_uid;
    public String latitiude;
    public String longitude;
    public String zoom;
    public String address;


    public Location(String location_uid, String latitiude, String longitude, String address , String title, String zoom) {
        this.location_uid = location_uid;
        this.latitiude = latitiude;
        this.longitude = longitude;
        this.title = title;
        this.address = address;
        this.zoom = zoom;
    }

    public static void generateLocationTable(Location location , Element row ) {
        row.addElement(Entry.KEY_UID).addText(location.location_uid);
        row.addElement(Entry.KEY_ENTRY_LOCATION_NAME).addText(location.title);
        row.addElement(Entry.KEY_ENTRY_LOCATION_ADDRESS).addText(location.address);
        row.addElement(Entry.KEY_ENTRY_LOCATION_LATITUDE).addText(location.latitiude);
        row.addElement(Entry.KEY_ENTRY_LOCATION_LONGITUDE).addText(location.longitude);
        row.addElement(Entry.KEY_ENTRY_LOCATION_ZOOM).addText(location.zoom);
    }

}
