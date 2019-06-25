import diaro.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.dom4j.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.*;

public class JourneyImport {
 //  private static final String INPUT_ZIP_FILE_PATH = "C:\\Users\\Animesh\\Downloads\\journey_export\\journey-abhi-1511184570437.zip";
   private static final String INPUT_ZIP_FILE_PATH = "C:\\Users\\Animesh\\Downloads\\journey_export\\journey-abhi-1499697023043.zip";
//    private static final String INPUT_ZIP_FILE_PATH = "C:\\Users\\Animesh\\Downloads\\journey_export\\journey-15607860943425500387449960148818.zip";
    private static final String OUTPUT_ZIP_FILE_PATH = "C:\\Users\\Animesh\\Downloads\\journey_export\\diaro_journey_import.zip";
    private static ArrayList<String> listOfEntriesAsJson = new ArrayList<>();
    private static Document xmlDocument;

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
    private static List<Attachment> attachmentList;
    private static List<Tags> tagsForEntryList;
    private static List<Entry> entriesList;

    //LinkedHashMaps for Tags and Locations
    private static HashMap<String, String> uidForEachTag;
    private static HashMap<String, String> uidForEachLocation;
    private static HashMap<String, String> attachmentsWithNewName;


    //Journey folder
    private static String FOLDER_UID = Entry.generateRandomUid();
    private static String FOLDER_TITLE = "Journey";
    private static String FOLDER_COLOR = "#F0B913";
    private static String DEFAULT_ZOOM = "11";

    //iterator for zipEntries
    private static  Enumeration<? extends ZipEntry> zipEntries;
    private static  ZipFile journeyZipFile;


    //weather icon info
    private static HashMap<String,String> iconAndNameMap = new HashMap<String,String>() {
        {
            put("01d", "day-sunny");
            put("01n", "night-clear");
            put("02d", "day-cloudy-gusts");
            put("02n", "night-alt-cloudy-gusts");
            put("03d", "day-cloudy-gusts");
            put("03n", "night-alt-cloudy-gusts");
            put("04d", "day-sunny-overcast");
            put("04n", "night-alt-cloudy");
            put("09d", "day-showers");
            put("09n", "night-alt-showers");
            put("10d", "day-sprinkle");
            put("10n", "night-alt-sprinkle");
            put("11d", "day-lightning");
            put("11n", "night-alt-lightning");
            put("13d", "day-snow");
            put("13n", "night-alt-snow");
            put("50d", "day-fog");
            put("50n", "night-fog");
        }
    };

    /**
     *  run the code
     * @param args no arguments
     */
    public static void main(String[] args) {
        try {

            readZipFileAndCreateListOfJson();
            collectVariablesForList();
            xmlDocument = XmlGenerator.generateXmlForDiaro(FOLDER_UID,FOLDER_TITLE,FOLDER_COLOR,uidForEachTag,locationsList,entriesList,attachmentList);
            writeXmlAndImagesToZip(xmlDocument);
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method reads the journey zip and creates a list with String json.
     * @throws IOException
     * @throws SecurityException
     */
    private static void readZipFileAndCreateListOfJson() throws IOException,SecurityException {
        String json = "";
        journeyZipFile = new ZipFile(INPUT_ZIP_FILE_PATH);
        zipEntries = journeyZipFile.entries();
        while (zipEntries.hasMoreElements()) {
            ZipEntry entry = zipEntries.nextElement();
            //check if the entry is json object and not a dir
            if (entry.getName().endsWith(".json") && !entry.isDirectory()) {
                InputStream stream = journeyZipFile.getInputStream(entry);
                json = IOUtils.toString(stream, "UTF-8");
                listOfEntriesAsJson.add(json);
                stream.close();
            }

        }
        journeyZipFile.close();
    }

    /**
     * <p>This method collects all the variable required for the
     * import from listOfEntriesAsJson and adds them in the corresponding
     * list of Diaro Java Objects
     * </p>
     */
    private static void collectVariablesForList() {

        //initializing the list
        entriesList = new ArrayList<>();
        locationsList = new ArrayList<>();
        attachmentList = new ArrayList<>();
        tagsForEntryList = new ArrayList<>();

        uidForEachTag = new LinkedHashMap<>();
        uidForEachLocation = new LinkedHashMap<>();
        attachmentsWithNewName = new LinkedHashMap<>();

        int j = 0;

        for (String journeyEntry : listOfEntriesAsJson) {
            //create new entry JSONObject with entry as root
            JSONObject rootJsonObject;
            try {
               rootJsonObject = new JSONObject(journeyEntry);

            }catch (JSONException e){
                continue;
            }
            JSONObject journey_weather = rootJsonObject.getJSONObject(KEY_JOURNEY__WEATHER);

            //-- collecting TAGS
            String tag_uid = "";
            String tag_title = "";
            if (!rootJsonObject.isNull(KEY_JOURNEY_TAGS)) {
                JSONArray tagsArray = rootJsonObject.getJSONArray(KEY_JOURNEY_TAGS);
                for (int i = 0; i < tagsArray.length(); i++) {
                    tag_title = tagsArray.getString(i);

                    //--check for already existing tag
                    if (!uidForEachTag.containsKey(tag_title)) {
                        uidForEachTag.put(tag_title, Entry.generateRandomUid());
                    }
                    Tags tag = new Tags(uidForEachTag.get(tag_title), tag_title);
                    tagsForEntryList.add(tag);
                }

            }
            //-- collecting location
            String location_uid = "";
            String title = "";
            String latitude = "";
            String longitude = "";
            if (!rootJsonObject.isNull(KEY_JOURNEY_LATITUDE) && !rootJsonObject.isNull(KEY_JOURNEY_LONGITUDE)) {

                double lat = rootJsonObject.getDouble(KEY_JOURNEY_LATITUDE);
                double lng = rootJsonObject.getDouble(KEY_JOURNEY_LONGITUDE);

                //a bug in journey just saves lat and lng as Double.MAX_VALUE(1.7976931348623157E308)
                // do not skip this check
                if(lat != 1.7976931348623157E308 && lng != 1.7976931348623157E308) {

                    latitude = String.format("%.5f", lat);
                    longitude = String.format("%.5f", lng);

                    //if address is available
                    //check for address in weather
                    //if no address found, display address as {Lat,Lng}
                    if (!ImportStringUtils.isNullOrEmpty(rootJsonObject, KEY_JOURNEY_ADDRESS)) {
                        title = rootJsonObject.getString(KEY_JOURNEY_ADDRESS);

                    } else if (!ImportStringUtils.isNullOrEmpty(journey_weather, KEY_JOURNEY__WEATHER_PLACE)) {
                        title = journey_weather.getString(KEY_JOURNEY__WEATHER_PLACE);

                    } else if (!latitude.isEmpty() && !longitude.isEmpty()) {
                        title = ImportStringUtils.concatLatLng(latitude, longitude);
                    }

                    //mapping every uid to the title
                    if (!uidForEachLocation.containsKey(title)) {
                        uidForEachLocation.put(title, Entry.generateRandomUid());
                    }

                    location_uid = uidForEachLocation.get(title);

                    Location location = new Location(location_uid, latitude, longitude, title, title, DEFAULT_ZOOM);
                    locationsList.add(location);
                }
            }

            //-- collecting weather info
            //checking if weather exists
            String weather_temp = "";
            String weather_description = "";
            String weather_icon = "";
            if (journey_weather != null && !journey_weather.isEmpty()) {
                if (!ImportStringUtils.isNullOrEmpty(journey_weather,KEY_JOURNEY__WEATHER_DEGREE) &&
                        !ImportStringUtils.isNullOrEmpty(journey_weather,KEY_JOURNEY__WEATHER_DESCRIPTION) &&
                        !ImportStringUtils.isNullOrEmpty(journey_weather,KEY_JOURNEY__WEATHER_ICON)) {

                    weather_temp = Double.toString(journey_weather.getDouble(KEY_JOURNEY__WEATHER_DEGREE));
                    weather_description = journey_weather.getString(KEY_JOURNEY__WEATHER_DESCRIPTION).toLowerCase();
                    weather_icon = iconCodeToName(journey_weather.getString(KEY_JOURNEY__WEATHER_ICON), iconAndNameMap);
                }
            }

            //--collecting text and titles
            String entry_uid = Entry.generateRandomUid();
            String entry_text = "";
            String entry_title = "";
            String entry_date = "";

            String journey_text = rootJsonObject.getString(KEY_JOURNEY_TEXT);
            String journey_preview_text = rootJsonObject.getString(KEY_JOURNEY_PREVIEW_TEXT);
            BigInteger journey_date= rootJsonObject.getBigInteger(KEY_JOURNEY_DATE_JOURNAL);

            if (!ImportStringUtils.isNullOrEmpty(rootJsonObject,KEY_JOURNEY_TEXT)) {
                entry_text = journey_text;
            }

            if (!ImportStringUtils.isNullOrEmpty(rootJsonObject,KEY_JOURNEY_PREVIEW_TEXT)) {
                entry_title = journey_preview_text;
            }

            if (!ImportStringUtils.isNullOrEmpty(rootJsonObject,KEY_JOURNEY_DATE_MODIFIED)) {
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
           Entry entry = new Entry(
                    entry_uid,
                    entry_date,
                    entry_title,
                    entry_text,
                    FOLDER_UID,
                    location_uid,
                    tag_uid
            );
            entry.weather_temperature = weather_temp;
            entry.weather_description = weather_description;
            entry.weather_icon = weather_icon;
            entriesList.add(entry);

            //clear the list and variable before another loop starts
            tagsForEntryList.clear();

            //--collecting and renaming attachments
            String attachment_uid;
            String fileName;
            String type = "photo";
            if (!rootJsonObject.isNull(KEY_JOURNEY_PHOTOS)&& rootJsonObject.getJSONArray(KEY_JOURNEY_PHOTOS).length() > 0) {
                JSONArray photosArray = rootJsonObject.getJSONArray(KEY_JOURNEY_PHOTOS);
                for (int i = 0; i < photosArray.length(); i++) {
                    attachment_uid = Entry.generateRandomUid();
                    fileName = photosArray.getString(i);
                    //generate new File Name
                    //adding the i and j to prevent duplicate entries generated during
                    //the same time span of ms
                    String newFileName = ImportStringUtils.generateNewFileName(fileName, String.valueOf(i), String.valueOf(j));

                    //check for compatibility
                    if (newFileName.endsWith(".png")|| newFileName.endsWith(".gif")|| newFileName.endsWith(".jpg")|| newFileName.endsWith(".jpeg")){
                       Attachment attachment = new Attachment(attachment_uid,entry_uid,type,newFileName);
                        attachmentList.add(attachment);
                        //add the old name and the new name in a list
                        if (!attachmentsWithNewName.containsKey(fileName)) {
                            attachmentsWithNewName.put(fileName,newFileName);
                        }
                    }
                }
//              incrementing j for additional String in fileName
                j++;
            }
        }
    }

    /** creates a new zip,saves the images in the required Diaro folders and writes the xml
     *
     * @param createdDocument created xml document for Diaro
     * @throws IOException is thrown when there is a problem reading or writing on zip.
     * @throws SecurityException is thrown if the permissions does not allow reading or writing of zip.
     */
    private static void writeXmlAndImagesToZip(Document createdDocument) throws IOException,SecurityException {
        StopWatch stopwatch = new StopWatch();
        stopwatch.start();
        //creating new zipOutputStream
        ZipOutputStream append = new ZipOutputStream(new FileOutputStream(OUTPUT_ZIP_FILE_PATH));
        //increase the buffer as required
        byte[] buffer = new byte[512];
        journeyZipFile = new ZipFile(INPUT_ZIP_FILE_PATH);
        zipEntries = journeyZipFile.entries();
        System.out.println("start appending");
        //loop through the entries
        while (zipEntries.hasMoreElements()) {
            ZipEntry newEntry;
            ZipEntry entry = zipEntries.nextElement();
            //creating entryName String object to get the filename without path
            String entryName = new File(entry.getName()).getName();

            //get the attachment name from list
            if(attachmentsWithNewName.containsKey(entryName)) {
                //create a new entry with the new attachment name
                newEntry = new ZipEntry("media/photo/" + attachmentsWithNewName.get(entryName));
            }else{

                newEntry = new ZipEntry("media/photo/" + entryName);
            }
            //checking for compatible attachments
            if(entry.getName().endsWith("jpg") || entry.getName().endsWith("png") || entry.getName().endsWith("gif") || entry.getName().endsWith("jpeg")) {
                System.out.println("append: " + newEntry);
                try {
                    //copying the attachments from old zip to new zip
                    append.putNextEntry(newEntry);
                    InputStream in = journeyZipFile.getInputStream(entry);
                    while (0 < in.available()) {
                        int read = in.read(buffer);
                        if (read > 0) {
                            append.write(buffer, 0, read);
                        }
                    }
                }catch (ZipException e){
                    e.printStackTrace();
                }
            }
        }
        append.closeEntry();

        // now append xml
        ZipEntry diaroZipFile = new ZipEntry("DiaroBackup.xml");
        System.out.println("append: " + diaroZipFile.getName());
        append.putNextEntry(diaroZipFile);
        append.write(Entry.toBytes(createdDocument));
        append.closeEntry();

        // close
       append.close();
       System.out.println("end appending");
       journeyZipFile.close();
       stopwatch.stop();
       System.out.println("Time taken on main Thread: " + stopwatch.getTime(TimeUnit.MILLISECONDS));

    }

    /** converts icon code to name
     * @param icon weather icon code
     * @param weatherIconToNameMap map with weather code and name
     * @return weather icon name
     */
    private static String iconCodeToName(String icon, Map<String, String> weatherIconToNameMap){
        if(!icon.isEmpty() && weatherIconToNameMap.containsKey(icon)){
            return weatherIconToNameMap.get(icon);
        }
        return "";
    }



}
