import com.google.gson.Gson;
import diaro.Attachment;
import diaro.Entry;
import diaro.Location;
import diaro.Tags;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DayOneImport {
    //DayOne Zip
    private static final String DAY_ONE_ZIP = "C:\\Users\\Animesh\\Downloads\\dayOneImport\\2016-06-13-DayOne-JSON special symbols and pics formats.zip";

    //DayOneJson
    private static String dayOneEntriesJson;

    //dayOne Fields
    private static final String KEY_DAYONE_TEXT = "text";
    private static final String KEY_DAYONE_LOCATION = "location";
    private static final String KEY_DAYONE_LOCATION_REGION = "region";
    private static final String KEY_DAYONE_LOCATION_REGION_CENTER = "center";
    private static final String KEY_DAYONE_LOCATION_REGION_CENTER_LONGITUDE = "longitude";
    private static final String KEY_DAYONE_LOCATION_REGION_CENTER_LATITUDE = "latitude";
    private static final String KEY_DAYONE_LOCATION_REGION_RADIUS = "radius";
    private static final String KEY_DAYONE_LOCATION_LOCALITY_NAME = "localityName";
    private static final String KEY_DAYONE_LOCATION_COUNTRY = "country";
    private static final String KEY_DAYONE_LOCATION_LONGITUDE = "longitude";
    private static final String KEY_DAYONE_LOCATION_LATITUDE = "latitude";
    private static final String KEY_DAYONE_LOCATION_PLACE_NAME = "placeName";
    private static final String KEY_DAYONE_LOCATION_ADMINISTRATIVE_AREA = "administrativeArea";
    private static final String KEY_DAYONE_WEATHER = "weather";
    private static final String KEY_DAYONE_WEATHER_CONDITIONS_DESCRIPTION = "conditionsDescription";
    private static final String KEY_DAYONE_WEATHER_WEATHER_CODE = "weatherCode";
    private static final String KEY_DAYONE_WEATHER_TEMPERATURE_CELSIUS = "temperatureCelsius";
    private static final String KEY_DAYONE_CREATION_DATE= "creationDate";
    private static final String KEY_DAYONE_ENTRIES= "entries";
    private static final String KEY_DAYONE_ENTRIES_TAGS= "tags";

    //defining lists
    private static List<Location> locationsList;
    private static List<Attachment> attachmentList;
    private static List<Tags> tagsForEntryList;
    private static List<Entry> entriesList;

    //LinkedHashMaps for Tags and Locations
    private static HashMap<String, String> uidForEachTag;
    private static HashMap<String, String> uidForEachLocation;

    public static void main(String[] args){
        try {
            dayOneEntriesJson = getEntries(DAY_ONE_ZIP);
            System.out.println("Is a valid JSON ? :  " + isJSONValid(dayOneEntriesJson));

        } catch (IOException e) {
            e.printStackTrace();
        }

        collectVariablesForList(dayOneEntriesJson);

    }

    public static String getEntries(String zipFile) throws IOException {
        String json = "";
        ZipFile dayOneZip = new ZipFile(zipFile);
        //enumerate though zip to find the json file
        Enumeration<? extends ZipEntry> dayOneZipEntries = dayOneZip.entries();
        while (dayOneZipEntries.hasMoreElements()) {
            ZipEntry zipEntry = dayOneZipEntries.nextElement();
            //if the file is JSON
            if(zipEntry.getName().endsWith(".json") && !zipEntry.isDirectory()){
                InputStream stream = dayOneZip.getInputStream(zipEntry);
                byte[] data = new byte[stream.available()];
                stream.read(data);
                json = new String(data);
            }
        }
        dayOneZip.close();
        return json;
    }

    private static void collectVariablesForList(String dayOneEntriesJson){

        //initializing the list
        entriesList = new ArrayList<>();
        locationsList = new ArrayList<>();
        attachmentList = new ArrayList<>();
        tagsForEntryList = new ArrayList<>();

        uidForEachTag = new LinkedHashMap<>();
        uidForEachLocation = new LinkedHashMap<>();

        //escape String

        JSONObject rootJsonObject = new JSONObject();
        try {
            rootJsonObject = new JSONObject(dayOneEntriesJson);
        }catch (JSONException e){
            e.printStackTrace();
        }
        JSONArray entries = rootJsonObject.getJSONArray(KEY_DAYONE_ENTRIES);
        //check if entries exists
        JSONObject objAtIndex;
        if(!rootJsonObject.isNull(KEY_DAYONE_ENTRIES)){
            for(int i = 0; i < entries.length(); i++) {
                //create an Object at index
                 objAtIndex =  entries.optJSONObject(i);

                //-- collecting TAGS
                String tag_uid = "";
                String tag_title = "";

                //if tags Array is present
                if(objAtIndex.optJSONArray(KEY_DAYONE_ENTRIES_TAGS)!=null){
                    JSONArray tags = objAtIndex.optJSONArray(KEY_DAYONE_ENTRIES_TAGS);
                    for(int j = 0; j < tags.length(); j++) {

                        tag_uid = Entry.generateRandomUid();

                        if(tags.getString(j)!= null && !tags.getString(j).isEmpty()) {
                            tag_title = tags.getString(j);
                            if (!uidForEachTag.containsKey(tag_title)) {
                                uidForEachTag.put(tag_title, tag_uid);
                            }
                            Tags tag = new Tags(uidForEachTag.get(tag_title), tag_title);
                            tagsForEntryList.add(tag);
                        }
                    }
                }

                //-- collecting location
                String location_uid = "";
                String title = "";
                String latitude = "";
                String longitude = "";
                if(objAtIndex.optJSONObject(KEY_DAYONE_LOCATION) != null) {
                    if (!Entry.areNullAndEmpty(objAtIndex.getJSONObject(KEY_DAYONE_LOCATION), KEY_DAYONE_LOCATION_LATITUDE) &&
                            !Entry.areNullAndEmpty(objAtIndex.getJSONObject(KEY_DAYONE_LOCATION), KEY_DAYONE_LOCATION_LONGITUDE)) {
                        latitude = objAtIndex.optJSONObject(KEY_DAYONE_LOCATION).optString(KEY_DAYONE_LOCATION_LATITUDE);
                        longitude = objAtIndex.optJSONObject(KEY_DAYONE_LOCATION).optString(KEY_DAYONE_LOCATION_LONGITUDE);
                    }


                    //check for name,country,administrativeArea and placeName
                    String administrativeArea = objAtIndex.optJSONObject(KEY_DAYONE_LOCATION).optString(KEY_DAYONE_LOCATION_ADMINISTRATIVE_AREA);
                    String placeName = objAtIndex.optJSONObject(KEY_DAYONE_LOCATION).optString(KEY_DAYONE_LOCATION_PLACE_NAME);
                    String localityName = objAtIndex.optJSONObject(KEY_DAYONE_LOCATION).optString(KEY_DAYONE_LOCATION_LOCALITY_NAME);
                    String country = objAtIndex.optJSONObject(KEY_DAYONE_LOCATION).optString(KEY_DAYONE_LOCATION_COUNTRY);
                    List<String> locationTitleValues = new ArrayList<>(Arrays.asList(administrativeArea,placeName,localityName,country));
                    System.out.println("b"+locationTitleValues);
                    locationTitleValues.removeAll(Arrays.asList("",null));
                    System.out.println("a"+locationTitleValues);
                }


            }

        }

    }
    //validate JSON
    private static boolean isJSONValid(String jsonInString) {
        Gson gson = new Gson();
        try {
            gson.fromJson(jsonInString, Object.class);
            return true;
        } catch(com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }



}
