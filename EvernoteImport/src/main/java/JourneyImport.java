import diaro.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.*;
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
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
        }
        collectVaraiblesForList();
    }

    private static void readZipFileAndCreateList() throws IOException,SecurityException {
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
//--------------------
            }

        }
        zipFile.close();
    }

    public static void collectVaraiblesForList() {

        //initializing the list
        foldersList = new ArrayList<>();
        entriesList = new ArrayList<>();
        locationsList = new ArrayList<>();
        attachmentList = new ArrayList<>();
        tagsForEntryList = new ArrayList<>();

        uidForEachTag = new LinkedHashMap<>();
        uidForEachLocation = new LinkedHashMap<>();

        for (String journeyEntry : listOfEntriesAsJson) {
            //defining the variables
            String tag_uid = "";
            String tag_text;

            String location_uid;
            String latitude = "";
            String longitude = "";
            String address;

            String weather_temp = "";
            String weather_description = "";
            String weather_icon = "";

            String attachment_uid;
            String entry_uid;
            String type = "photo";
            String fileName;

            String entry_text = "";
            String entry_title = "";
            String entry_date = "";

            //create new entry JSONObject with entry as root
            JSONObject rootJsonObject = new JSONObject(journeyEntry);
            JSONObject journey_weather = rootJsonObject.getJSONObject(KEY_JOURNEY__WEATHER);

            //-- collecting TAGS
            if (rootJsonObject.getJSONArray(KEY_JOURNEY_TAGS) != null && rootJsonObject.getJSONArray(KEY_JOURNEY_TAGS).length() > 0) {
                JSONArray tagsArray = rootJsonObject.getJSONArray(KEY_JOURNEY_TAGS);
                for (int i = 0; i < tagsArray.length(); i++) {
                    tag_text = tagsArray.getString(i);

                    //--check for already existing tag
                    if (!uidForEachTag.containsKey(tag_text)) {
                        uidForEachTag.put(tag_text, Entry.generateRandomUid());
                    }
                   Tags tag = new Tags(uidForEachTag.get(tag_text), tag_text);
                    tagsForEntryList.add(tag);
                }

            }
            //-- collecting folders
            foldersList.add(new Folder(FOLDER_TITLE, FOLDER_COLOR, FOLDER_UID));

            //-- collecting location
            if (!rootJsonObject.isNull(KEY_JOURNEY_LATITUDE) && !rootJsonObject.isNull(KEY_JOURNEY_LONGITUDE)) {

                latitude = Double.toString(rootJsonObject.getDouble(KEY_JOURNEY_LATITUDE));
                longitude = Double.toString(rootJsonObject.getDouble(KEY_JOURNEY_LATITUDE));
            }

            //if address is available
            //check for address in weather
            //if no address found, display address as {Lat,Lng}
            if (!rootJsonObject.isNull(KEY_JOURNEY_ADDRESS) && !rootJsonObject.getString(KEY_JOURNEY_ADDRESS).isEmpty()) {
                address = rootJsonObject.getString(KEY_JOURNEY_ADDRESS);

            } else if (!journey_weather.isNull(KEY_JOURNEY__WEATHER_PLACE) && !journey_weather.getString(KEY_JOURNEY__WEATHER_PLACE).isEmpty()) {
                address = journey_weather.getString(KEY_JOURNEY__WEATHER_PLACE);

            } else {
                address = Entry.addLocation(latitude, longitude);
            }

            //mapping every uid to the address
            if (!uidForEachLocation.containsKey(address)) { uidForEachLocation.put(address, Entry.generateRandomUid()); }
            location_uid = uidForEachLocation.get(address);

            Location location = new Location(location_uid, latitude, longitude, address, DEFAULT_ZOOM);
            locationsList.add(location);

            //-- collecting weather info
            //checking if weather exists
            if (journey_weather != null && journey_weather.length() != 0) {
                if (!journey_weather.isNull(KEY_JOURNEY__WEATHER_DEGREE) &&
                        !journey_weather.getString(KEY_JOURNEY__WEATHER_DESCRIPTION).isEmpty() &&
                        !journey_weather.getString(KEY_JOURNEY__WEATHER_ICON).isEmpty()) {

                    weather_temp = Double.toString(journey_weather.getDouble(KEY_JOURNEY__WEATHER_DEGREE));
                    weather_description = journey_weather.getString(KEY_JOURNEY__WEATHER_DESCRIPTION).toLowerCase();
                    weather_icon = iconCodeToName(journey_weather.getString(KEY_JOURNEY__WEATHER_ICON));
                }
            }

            //--collecting text and titles
            entry_uid = Entry.generateRandomUid();

            String journey_text = rootJsonObject.getString(KEY_JOURNEY_TEXT);
            String journey_preview_text = rootJsonObject.getString(KEY_JOURNEY_PREVIEW_TEXT);
            BigInteger journey_date= rootJsonObject.getBigInteger(KEY_JOURNEY_DATE_JOURNAL);

            if (!rootJsonObject.isNull(KEY_JOURNEY_TEXT) && !journey_text.isEmpty()) {
                entry_text = journey_text;
            }

            if (!rootJsonObject.isNull(KEY_JOURNEY_PREVIEW_TEXT) && !journey_preview_text.isEmpty()) {
                entry_title = journey_preview_text;
            }

            if (!rootJsonObject.isNull(KEY_JOURNEY_DATE_JOURNAL) && !String.valueOf(journey_date).isEmpty()) {
                entry_date = String.valueOf(journey_date);
            }
            //looping through all the tags
            //appending the tags in a String
            if (tagsForEntryList.size() != 0) {
                for (Tags tagsForEntry : tagsForEntryList) {
                    tag_uid = tag_uid + (",") + (String.join(",", tagsForEntry.tagsId));
                }
                tag_uid = tag_uid + (",");
                System.out.println(tag_uid);
            }
            Entry entry = new Entry(entry_uid, entry_date, entry_title, entry_text, FOLDER_UID, location_uid, tag_uid);
            entry.weather_temperature = weather_temp;
            entry.weather_description = weather_description;
            entry.weather_icon = weather_icon;
            entriesList.add(entry);


            //clear the list and variable before another loop starts
            tagsForEntryList.clear();
            tag_uid = "";

            //--collecting and renaming attachments
            if (!rootJsonObject.isNull(KEY_JOURNEY_PHOTOS)&& rootJsonObject.getJSONArray(KEY_JOURNEY_PHOTOS).length() > 0) {
                JSONArray photosArray = rootJsonObject.getJSONArray(KEY_JOURNEY_PHOTOS);
                for (int i = 0; i < photosArray.length(); i++) {
                    attachment_uid = Entry.generateRandomUid();
                    fileName = photosArray.getString(i);

                    //generate new File Name
                    String newFileName = Entry.setNewFileName(fileName);

                    //change the corresponding file name in zip
//                    try {
//                       // searchAndRenameFileInZip(fileName,newFileName);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    Attachment attachment = new Attachment(attachment_uid,entry_uid,type,newFileName);
                    attachmentList.add(attachment);
                }

            }
        }
    }


    public static void searchAndRenameFileInZip(String oldName, String newName) throws IOException,SecurityException {
        //using java nio library's FileSystem for renaming files
        Path zipFile = Paths.get(inputPath);
        final URI uri = URI.create("jar:file:" + zipFile.toUri().getPath());

        // Defining ZIP File System Properties in HashMap
        Map<String, String> zip_properties = new HashMap<>();
        zip_properties.put("create", "true");

        FileSystem zipFS = FileSystems.newFileSystem(uri, zip_properties, null);
        Path path = zipFS.getPath("/" + oldName);

        //check for compatibility
        if (pathMatcher(".png",path)|| pathMatcher(".gif",path)|| pathMatcher(".jpg",path)|| pathMatcher(".jpeg",path)){
            Path newPath = zipFS.getPath("/"+ newName);
            Files.move(path,newPath);
            System.out.println("File successfully renamed");
        }
        zipFS.close();
    }

    public static boolean pathMatcher(String extension , Path path){
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*" + extension);
        return matcher.matches(path);

    }


    public static String iconCodeToName(String icon){
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
