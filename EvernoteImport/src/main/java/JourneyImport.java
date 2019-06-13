import diaro.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JourneyImport {
    private static final String inputPath = "C:\\Users\\Animesh\\Downloads\\journey_export\\journey-abhi-1511184570437.zip";
    private static ArrayList<String> listOfEntriesAsJson = new ArrayList<>();

    //journey fields
    private static final String KEY_JOURNEY_TEXT = "text";
    private static final String KEY_JOURNEY_DATE_MODIFIED = "date_modified";
    private static final String KEY_JOURNEY_DATE_JOURNAL = "date_journal";
    private static final String KEY_JOURNEY_ID = "id";
    private static final String KEY_JOURNEY_PREVIEW_TEXT = "preview_text";
    private static final String KEY_JOURNEY_ADDRESS = "address";
    private static final String KEY_JOURNEY_LATITUDE = "lat";
    private static final String KEY_JOURNEY_LONGITUDE = "lon";
    private static final String KEY_JOURNEY__MOOD = "mood";
    private static final String KEY_JOURNEY__WEATHER = "weather";
    private static final String KEY_JOURNEY__WEATHER_ID = "id";
    private static final String KEY_JOURNEY__WEATHER_DEGREE = "degree_c";
    private static final String KEY_JOURNEY__WEATHER_DESCRIPTION = "description";
    private static final String KEY_JOURNEY__WEATHER_ICON = "icon";
    private static final String KEY_JOURNEY__WEATHER_PLACE = "place";
    private static final String KEY_JOURNEY_PHOTOS = "photos";
    private static final String KEY_JOURNEY_TAGS = "tags";

    //defining lists
    private static List<Location> locationsList;
    private static List<Folder> foldersList;
    private static List<Attachment> attachmentList;
    private static List<Tags> tagsForEntryList;
    private static List<Entry> entriesList;

    //LinkedHashMaps for Tags and Locations
    private static HashMap<String, String> uidForEachTag;
    private static HashMap<String, String> uidForEachLocation;

    //Journey folder
    private static String FOLDER_UID = Entry.generateRandomUid();
    private static String FOLDER_TITLE = "Journey";
    private static String FOLDER_COLOR = "#F0B913";
    private static String DEFAULT_ZOOM = "11";


    public static void main(String[] args) {
        try {
            readZipFileAndCreateList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        collectVariables();
    }

    private static void readZipFileAndCreateList() throws IOException {
        ZipFile zipFile = new ZipFile(inputPath);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            //check if the entry is json object and not a dir
            if (entry.getName().endsWith(".json") && !entry.isDirectory()) {
                InputStream stream = zipFile.getInputStream(entry);
                byte[] data = new byte[stream.available()];
                stream.read(data);
                String json = new String(data);
                listOfEntriesAsJson.add(json);
                stream.close();
            } else if (entry.isDirectory()) {

            }

        }
        zipFile.close();
    }

    public static void collectVariables() {

        //defining the variables
        String title = "";

        String tag_uid = "";
        String tag_text = "";

        String location_uid = "";
        String latitude = "";
        String longitude = "";
        String address = "";

        String weather_temp = "";
        String weather_description = "";
        String weather_icon = "";

        String attachment_uid = null;
        String entry_uid = "";
        String type = "photo";
        String baseEncoding;
        String mime = "";
        String fileName = "";

        String entry_text= "";
        String entry_title= "";
        String entry_date= "";

        Tags tag;
        Location location;
        Entry entry;
        Attachment attachment;

        //initializing the list
        foldersList = new ArrayList<>();
        entriesList = new ArrayList<>();
        locationsList = new ArrayList<>();
        attachmentList = new ArrayList<>();
        tagsForEntryList = new ArrayList<>();

        uidForEachTag = new LinkedHashMap<>();
        uidForEachLocation = new LinkedHashMap<>();

        for (String journeyEntry : listOfEntriesAsJson) {

            //create new entry JSONObject with entry as root
            JSONObject rootJsonObject = new JSONObject(journeyEntry);

            //-- collecting tags
            if (rootJsonObject.getJSONArray(KEY_JOURNEY_TAGS) != null && rootJsonObject.getJSONArray(KEY_JOURNEY_TAGS).length() > 0) {
                JSONArray tagsArray = rootJsonObject.getJSONArray(KEY_JOURNEY_TAGS);
                for (int i = 0; i < tagsArray.length(); i++) {
                    tag_text = tagsArray.getString(i);

                    //--check for already existing tag
                    if (!uidForEachTag.containsKey(tag_text)) {
                        uidForEachTag.put(tag_text, Entry.generateRandomUid());
                    }
                    tag = new Tags(uidForEachTag.get(tag_text), tag_text);
                    tagsForEntryList.add(tag);
                }

            }
            //-- collecting folders
            foldersList.add(new Folder(FOLDER_TITLE, FOLDER_COLOR, FOLDER_UID));

            //-- collecting location data
            /***
             * TODO create a locations Map and correct the location map in Evernote import
             */


            if (Double.toString(rootJsonObject.getDouble(KEY_JOURNEY_LATITUDE)) != null && Double.toString(rootJsonObject.getDouble(KEY_JOURNEY_LONGITUDE)) != null) {

                latitude = Double.toString(rootJsonObject.getDouble(KEY_JOURNEY_LATITUDE));
                longitude = Double.toString(rootJsonObject.getDouble(KEY_JOURNEY_LATITUDE));
            }
            //if address is available
            if (rootJsonObject.getString(KEY_JOURNEY_ADDRESS) != null && !rootJsonObject.getString(KEY_JOURNEY_ADDRESS).isEmpty()) {

                address = rootJsonObject.getString(KEY_JOURNEY_ADDRESS);

                //check for address in weather
            } else if (rootJsonObject.getJSONObject(KEY_JOURNEY__WEATHER).getString(KEY_JOURNEY__WEATHER_PLACE) != null
                    && !rootJsonObject.getJSONObject(KEY_JOURNEY__WEATHER).getString(KEY_JOURNEY__WEATHER_PLACE).isEmpty()) {

                address = rootJsonObject.getJSONObject(KEY_JOURNEY__WEATHER).getString(KEY_JOURNEY__WEATHER_PLACE);

                //if no address found, display address as {Lat,Lng}
            } else {

                address = Entry.addLocation(latitude, longitude);
            }

            //mapping every uid to the address
            if(!uidForEachLocation.containsKey(address)){
                uidForEachLocation.put(address, Entry.generateRandomUid());
            }

            location_uid = uidForEachLocation.get(address);

            location = new Location(location_uid, latitude, longitude, address, DEFAULT_ZOOM);
            locationsList.add(location);

            //-- collecting weather info
            //checking if weather exists
            if (rootJsonObject.getJSONObject(KEY_JOURNEY__WEATHER) != null && rootJsonObject.getJSONObject(KEY_JOURNEY__WEATHER).length() != 0) {

                if (Double.toString(rootJsonObject.getJSONObject(KEY_JOURNEY__WEATHER).getDouble(KEY_JOURNEY__WEATHER_DEGREE)) != null) {
                    weather_temp = Double.toString(rootJsonObject.getJSONObject(KEY_JOURNEY__WEATHER).getDouble(KEY_JOURNEY__WEATHER_DEGREE));
                }
                if(rootJsonObject.getJSONObject(KEY_JOURNEY__WEATHER).getString(KEY_JOURNEY__WEATHER_DESCRIPTION)!= null &&
                        !rootJsonObject.getJSONObject(KEY_JOURNEY__WEATHER).getString(KEY_JOURNEY__WEATHER_DESCRIPTION).isEmpty()){
                    weather_description = rootJsonObject.getJSONObject(KEY_JOURNEY__WEATHER).getString(KEY_JOURNEY__WEATHER_DESCRIPTION).toLowerCase();
                }
                if(rootJsonObject.getJSONObject(KEY_JOURNEY__WEATHER).getString(KEY_JOURNEY__WEATHER_ICON)!= null &&
                        !rootJsonObject.getJSONObject(KEY_JOURNEY__WEATHER).getString(KEY_JOURNEY__WEATHER_ICON).isEmpty()){
                    //get weather icon info
                    weather_icon = basicIconToName(rootJsonObject.getJSONObject(KEY_JOURNEY__WEATHER).getString(KEY_JOURNEY__WEATHER_ICON));
                }
            }
            //--collecting text and titles
            entry_uid = Entry.generateRandomUid();

            if(rootJsonObject.getString(KEY_JOURNEY_TEXT)!=null && !rootJsonObject.getString(KEY_JOURNEY_TEXT).isEmpty()){
                entry_text = rootJsonObject.getString(KEY_JOURNEY_TEXT);
            }

            if(rootJsonObject.getString(KEY_JOURNEY_PREVIEW_TEXT)!=null && !rootJsonObject.getString(KEY_JOURNEY_PREVIEW_TEXT).isEmpty()){
                entry_title = rootJsonObject.getString(KEY_JOURNEY_TEXT);
            }

            if(rootJsonObject.getString(KEY_JOURNEY_DATE_JOURNAL)!=null && !rootJsonObject.getString(KEY_JOURNEY_DATE_JOURNAL).isEmpty()){
                entry_date = rootJsonObject.getString(KEY_JOURNEY_DATE_JOURNAL);
            }
            //looping through all the tags
            //appending the tags in a String
            if(tagsForEntryList.size() != 0) {
                for (Tags tagsForEntry : tagsForEntryList) {
                    tag_uid = tag_uid + (",") + (String.join(",", tagsForEntry.tagsId));
                }
                tag_uid = tag_uid + (",");
                System.out.println(tag_uid);
                entry = new Entry(entry_uid, entry_date, entry_title, entry_text, FOLDER_UID, location_uid, tag_uid);
                entry.weather_temperature = weather_temp;
                entry.weather_description = weather_description;
                entry.weather_icon = weather_icon;
                entriesList.add(entry);

            }
            //clear the list and variable before another loop starts
            tagsForEntryList.clear();
            tag_uid = "";






        }
    }


    public static String basicIconToName(String icon){
        HashMap<String,String> iconAndNameMap = new LinkedHashMap<>();

        iconAndNameMap.put("01d" ,"day-sunny");
        iconAndNameMap.put("01n" ,"night-clear");
        iconAndNameMap.put("02d" ,"day-cloudy-gusts");
        iconAndNameMap.put("02n" ,"night-alt-cloudy-gusts");
        iconAndNameMap.put( "03d" ,"day-cloudy-gusts");
        iconAndNameMap.put( "03n" ,"night-alt-cloudy-gusts");
        iconAndNameMap.put("04d" ,"day-sunny-overcast");
        iconAndNameMap.put("04n" ,"night-alt-cloudy");
        iconAndNameMap.put( "09d" ,"day-showers");
        iconAndNameMap.put("09n" ,"night-alt-showers");
        iconAndNameMap.put( "10d" ,"day-sprinkle");
        iconAndNameMap.put("10n" ,"night-alt-sprinkle");
        iconAndNameMap.put("11d" ,"day-lightning");
        iconAndNameMap.put("11n" ,"night-alt-lightning");
        iconAndNameMap.put("13d" ,"day-snow");
        iconAndNameMap.put( "13n" ,"night-alt-snow");
        iconAndNameMap.put("50d" ,"day-fog");
        iconAndNameMap.put("50n" ,"night-fog");

        if(!icon.isEmpty() && iconAndNameMap.containsKey(icon)){
            return iconAndNameMap.get(icon);
        }

        return "";
    }
}
