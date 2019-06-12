import diaro.*;
import jdk.nashorn.internal.parser.JSONParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
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
    private static final String KEY_JOURNEY__WEATHER_place = "place";
    private static final String KEY_JOURNEY_PHOTOS = "photos";
    private static final String KEY_JOURNEY_TAGS = "tags";

    //defining lists
    private static List<Location> locationsList;
    private static List<Folder> foldersList;
    private static List<Attachment> attachmentList;
    private static List<Tags> tagsForEntryList;
    private static List<Entry> entriesList;

    //LinkedHashMap for Tags
    private static HashMap<String,String> tagsWithUid;


    //Journey folder
    private static String FOLDER_UID = Entry.generateRandomUid();
    private static String FOLDER_TITLE = "Journey";
    private static String FOLDER_COLOR = "#F0B913";


    public static void main(String[] args) {
        try {
            readZipFileAndCreateList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readZipFileAndCreateList() throws IOException {
        ZipFile zipFile = new ZipFile(inputPath);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            //check if the entry is json object and not a dir
            if (entry.getName().endsWith(".json") && !entry.isDirectory()) {
                InputStream stream = zipFile.getInputStream(entry);
                byte[]data = new byte[stream.available()];
                stream.read(data);
                String json = new String(data);
                listOfEntriesAsJson.add(json);
                stream.close();
            }else if(entry.isDirectory()){

            }

        }
        zipFile.close();
    }

    public static void collectVariables(){

        //defining the variables
        String title = "";
        String tags_uid = "";
        String tag = "";
        String parsedHtml = "";
        String formattedDate = "";
        String location_uid = "";
        String mime = "";
        String fileName = "";
        String attachment_uid = null;
        String entry_uid = "";
        String type = "photo";
        String baseEncoding;

        String tag_uid = "";

        //initialize the list
        foldersList = new ArrayList<>();
        entriesList = new ArrayList<>();
        locationsList = new ArrayList<>();
        attachmentList = new ArrayList<>();
        tagsForEntryList = new ArrayList<>();

        for(String entry : listOfEntriesAsJson){

            //create new entry JSONObject with entry as root
            JSONObject rootJsonObject = new JSONObject(entry);

            //-- collecting tags
            if(rootJsonObject.getJSONArray(KEY_JOURNEY_TAGS)!= null && rootJsonObject.getJSONArray(KEY_JOURNEY_TAGS).length() > 0){
                JSONArray tagsArray = rootJsonObject.getJSONArray(KEY_JOURNEY_TAGS);
                for(int i = 0; i < tagsArray.length(); i++) {
                    tags_uid = Entry.generateRandomUid();
                    tag = tagsArray.getString(i);
                    tagsForEntryList.add(new Tags(tags_uid,tag));
                    tagsWithUid.put(tag,tag_uid);
                }

            }
            //-- collecting folders
            foldersList.add(new Folder(FOLDER_TITLE,FOLDER_COLOR,FOLDER_UID));



        }
    }
}
