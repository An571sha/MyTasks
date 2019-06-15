package diaro;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

//import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Entry {

    public final static String KEY_UID = "uid";
    public final static String KEY_ENTRY_UID = "entry_uid";
    public final static String KEY_ENTRY_DATE = "date";
    public final static String KEY_ENTRY_TZ_OFFSET = "tz_offset";
    public final static String KEY_ENTRY_TITLE = "title";
    public final static String KEY_ENTRY_TEXT = "text";
    public final static String KEY_ENTRY_FOLDER_UID = "folder_uid";
    public final static String KEY_ENTRY_FOLDER_TITLE = "title";
    public final static String KEY_ENTRY_FOLDER_COLOR = "color";
    public final static String KEY_ENTRY_LOCATION_UID = "location_uid";
    public final static String KEY_ENTRY_LOCATION_NAME = "title";
    public final static String KEY_ENTRY_LOCATION_ZOOM = "zoom";
    public final static String KEY_ENTRY_TAGS_UID = "tag_uid";
    public final static String KEY_ENTRY_TAGS = "tags";
    public final static String KEY_ENTRY_TAGS_TITLE = "title";
    public final static String KEY_ENTRY_PRIMARY_PHOTO_UID = "primary_photo_uid";
    public final static String KEY_ENTRY_FILENAME = "filename";
    public final static String KEY_ENTRY_WEATHER_TEMP = "weather_temperature";
    public final static String KEY_ENTRY_WEATHER_DESC = "weather_description";
    public final static String KEY_ENTRY_WEATHER_ICON = "weather_icon";

    public static String dateFormatNormal = "dd/MM/yyyy";
    public static String dateFormatUS = "MM/dd/yy";

    public static String dateFormat = dateFormatUS;


    public String uid;
    public String date;
    public String tz_offset;
    public String title;
    public String text;
    public String folder_uid;
    public String location_uid;
    public String weather_temperature;
    public String weather_description;
    public String weather_icon;
    public String tags;
    public String primary_photo_uid;

    public Entry(String date, String text, String title, String folder_uid, String location_uid, String tags) {

        this.uid = generateRandomUid();
        this.date = String.valueOf(dateToTimeStamp(date));
        this.tz_offset = "+00:00";
        this.title = title;
        this.text = text;
        this.folder_uid = folder_uid;
        this.location_uid = location_uid;
        this.tags = tags;
        this.primary_photo_uid = "";


    }

    public Entry(String uid ,String date, String title, String text, String folder_uid, String location_uid, String tags) {
        this.uid = uid;
        this.date = date;
        this.tz_offset = "+00:00";
        this.title = title;
        this.text = text;
        this.folder_uid = folder_uid;
        this.location_uid = location_uid;
        this.weather_temperature = "";
        this.weather_description = "";
        this.weather_icon = "";
        this.tags = tags;
        this.primary_photo_uid = "";
    }

    public Entry() {
        // TODO Auto-generated constructor stub
    }

    public String getXmlString() {

        StringBuffer buf = new StringBuffer();

        buf.append(addColumn(KEY_UID, uid));
        buf.append(addColumn(KEY_ENTRY_DATE, date));
        buf.append(addColumn(KEY_ENTRY_TZ_OFFSET, tz_offset));
        buf.append(addColumn(KEY_ENTRY_TITLE, title));
        buf.append(addColumn(KEY_ENTRY_TEXT, text));
        buf.append(addColumn(KEY_ENTRY_FOLDER_UID, folder_uid));
        buf.append(addColumn(KEY_ENTRY_LOCATION_UID, location_uid));
        buf.append(addColumn(KEY_ENTRY_TAGS_UID, tags));
        buf.append(addColumn(KEY_ENTRY_PRIMARY_PHOTO_UID, primary_photo_uid));

        return buf.toString();

    }

    public String addColumn(String name, Object val) {

        if (name.equals(Entry.KEY_ENTRY_TITLE) || name.equals(Entry.KEY_ENTRY_TEXT)) {
            val = StringEscapeUtils.escapeXml11(val.toString());

        }
        val = String.valueOf("\n   <" + name + ">" + val + "</" + name + ">");

        return val.toString();
    }

    public static String generateRandomUid() {
        return md5(String.valueOf(new DateTime().getMillis()) + new BigInteger(130, new Random()).toString(32));
    }

    /**
     * @Returns md5 hash of given string
     */
    public static String md5(String string) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(string.getBytes());
            byte[] a = digest.digest();
            int len = a.length;
            StringBuilder sb = new StringBuilder(len << 1);
            for (int i = 0; i < len; i++) {
                sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
                sb.append(Character.forDigit(a[i] & 0x0f, 16));
            }
            return sb.toString();
        } catch (Exception e) {
        }
        return null;
    }

    public static void timeStampToDate(Long val) {

        long curVal = val;
        DateTime formattedJodaDate = new DateTime(curVal);
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern(dateFormat);
        System.out.println(dtfOut.print(formattedJodaDate));

    }

    public static long dateToTimeStamp(String date) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yy");
        DateTime dt = formatter.parseDateTime(date);
        DateTime dtNew = dt.plusHours(18);

        return dtNew.getMillis();
    }

    public static String concatLatLng(String lat, String lng){
        try {
            int latDotIndex = lat.indexOf(".");
            lat = lat.substring(0, latDotIndex) + lat.substring(latDotIndex, latDotIndex + 7);
            int lngDotIndex = lng.indexOf(".");
            lng = lng.substring(0, lngDotIndex) + lng.substring(lngDotIndex, lngDotIndex + 7);
            return String.format("(%s,%s)", lat, lng);

        }catch(IndexOutOfBoundsException e){
            e.printStackTrace();
        }
        return String.format("(%s,%s)", lat, lng);
    }


    /** creates a new FileName if duplicates are found
     * @param fileNameString name of the file in attachment
     * @return the newString
     */
    public static String setNewFileName(String fileNameString) {
        String extension = FilenameUtils.getExtension(fileNameString);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        return timeStamp + "." + extension;
    }

    public static void generateEntryTable(Entry entry , Element entriesRow , String folder_uid){

        entriesRow.addElement(KEY_UID).addText(entry.uid);
        entriesRow.addElement(KEY_ENTRY_DATE).addText(entry.date);
        entriesRow.addElement(KEY_ENTRY_TITLE).addText(entry.title);
        entriesRow.addElement(KEY_ENTRY_TEXT).addText(entry.text);
        entriesRow.addElement(KEY_ENTRY_LOCATION_UID).addText(entry.location_uid);
        entriesRow.addElement(KEY_ENTRY_TAGS).addText(entry.tags);
        entriesRow.addElement("primary_photo_uid").addText(entry.primary_photo_uid);
        entriesRow.addElement(KEY_ENTRY_FOLDER_UID).addText(folder_uid);
        entriesRow.addElement(KEY_ENTRY_TZ_OFFSET).addText(entry.tz_offset);

        if(entry.weather_icon !=null && !entry.weather_icon.isEmpty()  &&
                entry.weather_temperature !=null && !entry.weather_temperature.isEmpty() &&
                entry.weather_description !=null && !entry.weather_description.isEmpty()){

            entriesRow.addElement(KEY_ENTRY_WEATHER_TEMP).addText(entry.weather_temperature);
            entriesRow.addElement(KEY_ENTRY_WEATHER_DESC).addText(entry.weather_description);
            entriesRow.addElement(KEY_ENTRY_WEATHER_ICON).addText(entry.weather_icon);
        }
    }

    /**
     * @param document xml document
     * @return byte[] for zipOutputStream
     */
    public static byte[] toBytes(Document document) {
        String text = document.asXML();
        return text.getBytes();
    }







}
