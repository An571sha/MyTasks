import diaro.*;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public class JourneyImport {
//   private static final String ZIP_FILE_PATH = "C:\\Users\\Animesh\\Downloads\\journey_export\\journey-abhi-1511184570437.zip";
   private static final String ZIP_FILE_PATH = "C:\\Users\\Animesh\\Downloads\\journey_export\\journey-abhi-1499697023043.zip";
    private static final String outputPath = "C:\\Users\\Animesh\\Downloads\\journey_export\\journey_backup.zip";
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
    private static Folder folder;
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

    //xml table names
    private static String diaro_folders = "diaro_folders";
    private static String diaro_entries = "diaro_entries";
    private static String diaro_attachments = "diaro_attachments";
    private static String diaro_tags = "diaro_tags";
    private static String diaro_locations = "diaro_locations";

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

    public static void main(String[] args) {
        try {

            readZipFileAndCreateListOfJson();
            collectVariablesForList();
            xmlDocument = generateXmlForDiaro();
            writeXmlAndImagesToZip(xmlDocument);

        } catch (IOException | SecurityException e) {
            e.printStackTrace();
        }
    }

    private static void readZipFileAndCreateListOfJson() throws IOException,SecurityException {
        journeyZipFile = new ZipFile(ZIP_FILE_PATH);
        zipEntries = journeyZipFile.entries();
        while (zipEntries.hasMoreElements()) {
            ZipEntry entry = zipEntries.nextElement();
            //check if the entry is json object and not a dir
            if (entry.getName().endsWith(".json") && !entry.isDirectory()) {
                InputStream stream = journeyZipFile.getInputStream(entry);
                byte[] data = new byte[stream.available()];
                stream.read(data);
                String json = new String(data);
                listOfEntriesAsJson.add(json);
                stream.close();
            }

        }
        journeyZipFile.close();
    }

    private static void collectVariablesForList() {

        //initializing the list
        entriesList = new ArrayList<>();
        locationsList = new ArrayList<>();
        attachmentList = new ArrayList<>();
        tagsForEntryList = new ArrayList<>();

        uidForEachTag = new LinkedHashMap<>();
        uidForEachLocation = new LinkedHashMap<>();


        //creating a new Folder Object
        folder = new Folder(FOLDER_TITLE, FOLDER_COLOR, FOLDER_UID);

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
            if (!rootJsonObject.isNull(KEY_JOURNEY_LATITUDE) && !rootJsonObject.isNull(KEY_JOURNEY_LONGITUDE)) {

                String latitude = Double.toString(rootJsonObject.getDouble(KEY_JOURNEY_LATITUDE));
                String longitude = Double.toString(rootJsonObject.getDouble(KEY_JOURNEY_LATITUDE));

                //if address is available
                //check for address in weather
                //if no address found, display address as {Lat,Lng}

                if (!areNullAndEmpty(rootJsonObject,KEY_JOURNEY_ADDRESS)) {
                    title = rootJsonObject.getString(KEY_JOURNEY_ADDRESS);

                } else if (!areNullAndEmpty(journey_weather,KEY_JOURNEY__WEATHER_PLACE)) {
                    title = journey_weather.getString(KEY_JOURNEY__WEATHER_PLACE);

                } else {
                    title = Entry.concatLatLng(latitude, longitude);
                }

                //mapping every uid to the title
                if (!uidForEachLocation.containsKey(title)) {
                    uidForEachLocation.put(title, Entry.generateRandomUid()); }

                location_uid = uidForEachLocation.get(title);

                Location location = new Location(location_uid, latitude, longitude, title, DEFAULT_ZOOM);
                locationsList.add(location);
            }

            //-- collecting weather info
            //checking if weather exists
            String weather_temp = "";
            String weather_description = "";
            String weather_icon = "";
            if (journey_weather != null && !journey_weather.isEmpty()) {
                if (!areNullAndEmpty(journey_weather,KEY_JOURNEY__WEATHER_DEGREE) &&
                        !areNullAndEmpty(journey_weather,KEY_JOURNEY__WEATHER_DESCRIPTION) &&
                        !areNullAndEmpty(journey_weather,KEY_JOURNEY__WEATHER_ICON)) {

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

            if (!areNullAndEmpty(rootJsonObject,KEY_JOURNEY_TEXT)) {
                entry_text = journey_text;
            }

            if (!areNullAndEmpty(rootJsonObject,KEY_JOURNEY_PREVIEW_TEXT)) {
                entry_title = journey_preview_text;
            }

            if (!areNullAndEmpty(rootJsonObject,KEY_JOURNEY_DATE_MODIFIED)) {
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
                    String newFileName = Entry.setNewFileName(fileName);

                    //check for compatibility

                    if (newFileName.endsWith(".png")|| newFileName.endsWith(".gif")|| newFileName.endsWith(".jpg")|| newFileName.endsWith(".jpeg")){
//                    change the corresponding file name in zip
                        try {
                            searchAndRenameFilesInZip(fileName, newFileName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    Attachment attachment = new Attachment(attachment_uid,entry_uid,type,newFileName);
                    attachmentList.add(attachment);
                }

            }
        }
    }


    private static void searchAndRenameFilesInZip(String oldFileName, String newFileName) throws IOException,SecurityException {
        Path newNameFlePath;
        Path oldNameFilePath;
        String oldFileNameWithSeperator = File.separator + oldFileName;
        String newFileNameWithSeperator = File.separator + newFileName;
        //using java nio library's FileSystem for renaming files
        Path zipFilePath = Paths.get(ZIP_FILE_PATH);

        //open an input stream to rename Files
        InputStream theFile = new FileInputStream(ZIP_FILE_PATH);
        ZipInputStream stream = new ZipInputStream(theFile);

        FileSystem zipFS = FileSystems.newFileSystem(zipFilePath,null);

        ZipEntry zipEntry;

        //if the attachment name exists in zip
        while ((zipEntry = stream.getNextEntry()) != null) {

            //if the zipEntry exists in a dir
            if (!zipEntry.isDirectory()) {

                //create a new file object for that zipEntry
                File zipEntryFile = new File(zipEntry.getName());
                String zipEntryParent = zipEntryFile.getParent();

                //if the parent dir does not exist
                if (zipEntryParent == null) {

                    oldNameFilePath = zipFS.getPath(oldFileNameWithSeperator);
                    newNameFlePath = zipFS.getPath(newFileNameWithSeperator);

                    if (zipEntryFile.getName().equals(oldFileName)) {

                        Files.move(oldNameFilePath, newNameFlePath);
                        System.out.println(oldFileName + "File successfully renamed to" + newFileName);
                    }

                } else {

                    oldNameFilePath = zipFS.getPath(zipEntryParent + oldFileNameWithSeperator);
                    newNameFlePath = zipFS.getPath(zipEntryParent + newFileNameWithSeperator);

                    if (zipEntryFile.getPath().equals(zipEntryParent+ oldFileNameWithSeperator)) {

                        Files.move(oldNameFilePath, newNameFlePath);
                        System.out.println(oldFileName + "File successfully renamed to" + newFileName);

                    }
                }
            }
        }
        //close the streams
        stream.close();
        zipFS.close();


    }

    private static Document generateXmlForDiaro(){
        Document createdXmlDocument = DocumentHelper.createDocument();
        //create the root element of the document
        Element root = createdXmlDocument.addElement("data").addAttribute("version", "2");

        //adding folders table
        // generate folder table. All the folders have a constant
        // uid, title and colour. As evernote does not provide any data regarding folders.
        Element folderRoot = root.addElement("table").addAttribute("title", diaro_folders);
        Element folderRow =  folderRoot.addElement("r");
        folderRow.addElement(Entry.KEY_UID).addText(FOLDER_UID);
        folderRow.addElement(Entry.KEY_ENTRY_FOLDER_TITLE).addText(FOLDER_TITLE);
        folderRow.addElement(Entry.KEY_ENTRY_FOLDER_COLOR).addText(FOLDER_COLOR);

        //adding tags table
        Element tagRoot =  root.addElement("table").addAttribute("title", diaro_tags);
        for(Map.Entry<String,String> tag : uidForEachTag.entrySet() ){
            assert tagRoot != null;
            Element tagRow = tagRoot.addElement("r");
            Tags.generateTagTable(tag,tagRow);
        }
        //adding location table
        Element locationRoot =  root.addElement("table").addAttribute("title", diaro_locations);
        for(Location location : locationsList){

            assert locationRoot != null;
            Element locationRow = locationRoot.addElement("r");
            Location.generateLocationTable(location,locationRow);
        }

        //adding zipEntries table
        Element entryRoot = root.addElement("table").addAttribute("title", diaro_entries);
        for(Entry entry: entriesList){
            Element entriesRow = entryRoot.addElement("r");
            Entry.generateEntryTable(entry,entriesRow,FOLDER_UID);
        }
        //adding attachments table
        Element attachmentsRoot = root.addElement("table").addAttribute("title", diaro_attachments);
        for(Attachment attachment: attachmentList){

            assert attachmentsRoot != null;
            Element attachmentRow =  attachmentsRoot.addElement("r");
            Attachment.generateAttachmentTable(attachment,attachmentRow);

        }
        return createdXmlDocument;

    }

    private static void writeXmlAndImagesToZip(Document createdDocument) throws IOException,SecurityException {
        ZipOutputStream append = new ZipOutputStream(new FileOutputStream(outputPath));
        //increase the buffer as required
        byte[] buffer = new byte[512];

        journeyZipFile = new ZipFile(ZIP_FILE_PATH);
        zipEntries = journeyZipFile.entries();
        System.out.println("start appending");
        while (zipEntries.hasMoreElements()) {

            ZipEntry entry = zipEntries.nextElement();
            String entryName = new File(entry.getName()).getName();
            ZipEntry newEntry = new ZipEntry("media/photos/" + entryName);
            if(entry.getName().endsWith("jpg") || entry.getName().endsWith("png") || entry.getName().endsWith("gif") || entry.getName().endsWith("jpeg")   ) {
                System.out.println("append: " + entryName);
                try {
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
        System.out.println("end appending");
        append.closeEntry();

        // now append xml
        ZipEntry diaroZipFile = new ZipEntry("DiaroBackup.xml");
        System.out.println("append: " + diaroZipFile.getName());
        append.putNextEntry(diaroZipFile);
        append.write(Entry.toBytes(createdDocument));
        append.closeEntry();

        // close
       append.close();
       journeyZipFile.close();


    }

    private static String iconCodeToName(String icon, Map<String, String> weatherIconToNameMap){
        if(!icon.isEmpty() && weatherIconToNameMap.containsKey(icon)){
            return weatherIconToNameMap.get(icon);
        }
        return "";
    }

    private static boolean areNullAndEmpty(JSONObject rootJsonObject, String key){
        if(rootJsonObject.get(key) instanceof String) {
            return rootJsonObject.isNull(key) || rootJsonObject.getString(key).isEmpty();

        } else if(rootJsonObject.get(key) instanceof JSONObject){
            return rootJsonObject.isNull(key);
        }

        return false;
    }

}
