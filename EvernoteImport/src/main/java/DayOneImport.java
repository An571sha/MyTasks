import diaro.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.time.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DayOneImport {
    //DayOne Zip
    private static final String DAY_ONE_ZIP = "C:\\Users\\Animesh\\Downloads\\dayOneImport\\2016-06-13-DayOne-JSON special symbols and pics formats.zip";
//    private static final String DAY_ONE_ZIP = "C:\\Users\\Animesh\\Downloads\\dayOneImport\\Export-Journal-photos.zip";
    private static final String OUTPUT_ZIP_FILE_PATH = "C:\\Users\\Animesh\\Downloads\\dayOneImport\\diaro_dayOne_import.zip";
    //DayOneJson
    private static String dayOneEntriesJson;

    private static Document xmlDocument;

    //dayOne Fields
    private static final String KEY_DAYONE_TEXT = "text";
    private static final String KEY_DAYONE_LOCATION = "location";
    private static final String KEY_DAYONE_LOCATION_LOCALITY_NAME = "localityName";
    private static final String KEY_DAYONE_LOCATION_COUNTRY = "country";
    private static final String KEY_DAYONE_LOCATION_LONGITUDE = "longitude";
    private static final String KEY_DAYONE_LOCATION_LATITUDE = "latitude";
    private static final String KEY_DAYONE_LOCATION_PLACE_NAME = "placeName";
    private static final String KEY_DAYONE_LOCATION_ADMINISTRATIVE_AREA = "administrativeArea";
    private static final String KEY_DAYONE_CREATION_DATE= "creationDate";
    private static final String KEY_DAYONE_ENTRIES= "entries";
    private static final String KEY_DAYONE_TAGS = "tags";
    private static final String KEY_DAYONE_TIMEZONE = "timeZone";
    private static final String KEY_DAYONE_PHOTOS = "photos";
    private static final String KEY_DAYONE_PHOTOS_IDENTIFIER = "identifier";
    private static final String KEY_DAYONE_PHOTOS_MD5 = "md5";
    private static final String KEY_DAYONE_PHOTOS_WIDTH = "width";
    private static final String KEY_DAYONE_PHOTOS_TYPE = "type";


    //defining lists
    private static List<Location> locationsList;
    private static List<Attachment> attachmentList;
    private static List<Tags> tagsForEntryList;
    private static List<Entry> entriesList;
    private static List<String> zipPhotosNames;

    //LinkedHashMaps for Tags and Locations
    private static HashMap<String, String> uidForEachTag;
    private static HashMap<String, String> uidForEachLocation;
    private static HashMap<String, String> photosWithNewName;


    //Day One folder
    private static String FOLDER_UID = Entry.generateRandomUid();
    private static String FOLDER_TITLE = "DayOne";
    private static String FOLDER_COLOR = "#F0B913";
    private static String DEFAULT_ZOOM = "11";


    public static void main(String[] args){
        try {

            dayOneEntriesJson = getJson(DAY_ONE_ZIP);
            collectVariablesForList(dayOneEntriesJson);
            xmlDocument = XmlGenerator.generateXmlForDiaro(FOLDER_UID,FOLDER_TITLE,FOLDER_COLOR,uidForEachTag,locationsList,entriesList,attachmentList);
            ImportUtils.writeXmlAndImagesToZip(xmlDocument,DAY_ONE_ZIP,OUTPUT_ZIP_FILE_PATH,photosWithNewName);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String getJson(String zipFile) throws IOException {
        String json = "";
        String photoFilePath = "";
        zipPhotosNames = new ArrayList<>();
        ZipFile dayOneZip = new ZipFile(zipFile);
        //enumerate though zip to find the json file
        Enumeration<? extends ZipEntry> dayOneZipEntries = dayOneZip.entries();
        while (dayOneZipEntries.hasMoreElements()) {
            ZipEntry zipEntry = dayOneZipEntries.nextElement();
            //if the file is JSON
            if(zipEntry.getName().endsWith(".json") && !zipEntry.isDirectory()){
                InputStream stream = dayOneZip.getInputStream(zipEntry);
                json = IOUtils.toString(stream, "UTF-8");
                stream.close();
            }
            //if the file is a compatible attachment
            //get file name from file path
            //add the fileNames in a list
            if(ImportUtils.checkPhotosExtension(zipEntry)){
                photoFilePath = zipEntry.getName();
                photoFilePath = photoFilePath.substring(photoFilePath.lastIndexOf("/")+1,StringUtils.length(photoFilePath));
                zipPhotosNames.add(photoFilePath);
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
        photosWithNewName = new LinkedHashMap<>();

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
                String tagUid = "";
                String tagTitle = "";
                //if tags Array is present
                if(objAtIndex.optJSONArray(KEY_DAYONE_TAGS)!=null){
                    JSONArray tags = objAtIndex.optJSONArray(KEY_DAYONE_TAGS);
                    for(int j = 0; j < tags.length(); j++) {

                        tagUid = Entry.generateRandomUid();

                        if(tags.getString(j)!= null && !tags.getString(j).isEmpty()) {
                            tagTitle = tags.getString(j);
                            if (!uidForEachTag.containsKey(tagTitle)) {
                                uidForEachTag.put(tagTitle, tagUid);
                            }
                            Tags tag = new Tags(uidForEachTag.get(tagTitle), tagTitle);
                            tagsForEntryList.add(tag);
                        }
                    }
                }
                //-- collecting location
                String locationUid = "";
                String title = "";
                String latitude = "";
                String longitude = "";
                if(!ImportUtils.isNullOrEmpty(objAtIndex,KEY_DAYONE_LOCATION)) {

                    JSONObject dayOneLocation = objAtIndex.optJSONObject(KEY_DAYONE_LOCATION);

                    if (!ImportUtils.isNullOrEmpty(dayOneLocation, KEY_DAYONE_LOCATION_LATITUDE) &&
                            !ImportUtils.isNullOrEmpty(dayOneLocation, KEY_DAYONE_LOCATION_LONGITUDE)) {
                        latitude = dayOneLocation.optString(KEY_DAYONE_LOCATION_LATITUDE);
                        longitude = dayOneLocation.optString(KEY_DAYONE_LOCATION_LONGITUDE);
                    }
                    //if place Name is present
                    //get name,country,administrativeArea and placeName
                    String administrativeArea = dayOneLocation.optString(KEY_DAYONE_LOCATION_ADMINISTRATIVE_AREA);
                    String placeName = dayOneLocation.optString(KEY_DAYONE_LOCATION_PLACE_NAME);
                    String localityName = dayOneLocation.optString(KEY_DAYONE_LOCATION_LOCALITY_NAME);
                    String country = dayOneLocation.optString(KEY_DAYONE_LOCATION_COUNTRY);

                    List<String> locationTitleValues = new ArrayList<>(Arrays.asList(administrativeArea, placeName, localityName, country));
                    //filtering out the empty values
                    locationTitleValues.removeAll(Arrays.asList("", null));
                    //if locationTitleValues is notEmpty
                    //looping through all the locationTitleValues
                    //append the values in a String
                    if (locationTitleValues.size() != 0) {
                        for (String locationNameVals : locationTitleValues) {
                            title = title + (",") + (String.join(",", locationNameVals));
                        }
                        locationTitleValues.clear();
                    } else {
                        title = ImportUtils.concatLatLng(latitude, longitude);
                    }
                    //if title is not empty
                    //some lat,lng can be 0, ignore them
                    if (!title.isEmpty() && !latitude.equals("0") && !longitude.equals("0")) {

                        if (!uidForEachLocation.containsKey(title)) {
                            uidForEachLocation.put(title, Entry.generateRandomUid());
                        }
                        locationUid = uidForEachLocation.get(title);
                        Location location = new Location(locationUid, latitude, longitude, title, title, DEFAULT_ZOOM);
                        locationsList.add(location);
                    }
                }

                //--collect entries info
                String entryUid = Entry.generateRandomUid();
                String entryTitle = "";
                String[]entryTextAndTitle;
                String entryDate = "";
                String timezoneOffset = "";
                String entryText = "";
                Entry entry;
                //collecting text and titles
                if(!ImportUtils.isNullOrEmpty(objAtIndex,KEY_DAYONE_TEXT)) {
                    entryText = objAtIndex.optString(KEY_DAYONE_TEXT);
                    entryTextAndTitle = ImportUtils.formatDayOneTextAndTitle(entryText);
                    entryTitle = entryTextAndTitle[0];
                    entryText = entryTextAndTitle[1];
                }
                //date
                if(!ImportUtils.isNullOrEmpty(objAtIndex,KEY_DAYONE_CREATION_DATE)){
                    entryDate = objAtIndex.optString(KEY_DAYONE_CREATION_DATE);
                }
                //timezone
                if(!ImportUtils.isNullOrEmpty(objAtIndex,KEY_DAYONE_TIMEZONE)){
                    timezoneOffset = objAtIndex.optString(KEY_DAYONE_TIMEZONE);

                    //get local date, zone and offset
                    LocalDateTime dt = LocalDateTime.now();
                    ZoneId zone = ZoneId.of(timezoneOffset);
                    ZonedDateTime zdt = dt.atZone(zone);
                    ZoneOffset offset = zdt.getOffset();
                    timezoneOffset = String.format("%s",offset);
                }

                //looping through all the tags
                //appending the tags in a String
                if (tagsForEntryList.size() != 0) {
                    for (Tags tagsForEntry : tagsForEntryList) {
                        tagUid = tagUid + (",") + (String.join(",", tagsForEntry.tagsId));
                    }
                    tagUid =(",")+ tagUid + (",");
                    System.out.println(tagUid);
                }
                 entry = new Entry(
                        entryUid,
                        timezoneOffset,
                        entryDate,
                        entryTitle,
                        entryText,
                        FOLDER_UID,
                        locationUid,
                        tagUid
                );
                entriesList.add(entry);
                //clear the list
                tagsForEntryList.clear();

                //--collect attachments(photos) info
                String fileName = "";
                String newFileName = "";
                String type = "photo";
                Attachment attachment = null;
                if (!objAtIndex.isNull(KEY_DAYONE_PHOTOS)) {
                    JSONArray photosArray = objAtIndex.optJSONArray(KEY_DAYONE_PHOTOS);
                    for (int k = 0; k < photosArray.length(); k++) {

                        JSONObject attachmentObjAtIndx =  photosArray.optJSONObject(k);

                        //if photo has a identifier String
                        // entry text will contain this String
                        //replace the identifier name from entry text or title
                        if(!ImportUtils.isNullOrEmpty(attachmentObjAtIndx,KEY_DAYONE_PHOTOS_IDENTIFIER)){
                            String identifier = attachmentObjAtIndx.optString(KEY_DAYONE_PHOTOS_IDENTIFIER);
                            String stringToReplace = "![](dayone-moment://" +  identifier + ")";
                            entry.text = StringUtils.replace(entry.text,stringToReplace,"");
                            entry.title = StringUtils.replace(entry.title,stringToReplace,"");
                        }
                        //generate a new file name
                        String fileNameWithoutExt = "";
                        if(!ImportUtils.isNullOrEmpty(attachmentObjAtIndx,KEY_DAYONE_PHOTOS_MD5)) {
                            fileNameWithoutExt = attachmentObjAtIndx.optString(KEY_DAYONE_PHOTOS_MD5);

                            //if the attachment type key is present in JSON (new DayOne JSON)
                            if (!ImportUtils.isNullOrEmpty(attachmentObjAtIndx, KEY_DAYONE_PHOTOS_TYPE)) {
                                fileName = fileNameWithoutExt + attachmentObjAtIndx.optString(KEY_DAYONE_PHOTOS_TYPE);
                                newFileName = ImportUtils.generateNewFileName(fileName, String.valueOf(i), String.valueOf(k));
                                attachment = new Attachment(Entry.generateRandomUid(),entryUid,type,newFileName);

                            //else loop through the photos name list from zip (old DayOne JSON)
                            } else {
                                for(String zipPhotoName : zipPhotosNames){
                                    if(fileNameWithoutExt.equals(zipPhotoName.substring(0,zipPhotoName.indexOf(".")))){
                                        fileName = zipPhotoName;
                                        newFileName = ImportUtils.generateNewFileName(fileName, String.valueOf(i), String.valueOf(k));
                                        attachment = new Attachment(Entry.generateRandomUid(),entryUid,type,newFileName);
                                    }
                                }
                            }

                            attachmentList.add(attachment);

                            //add the old name and the new name in a list
                            if (!photosWithNewName.containsKey(fileName)) {
                                photosWithNewName.put(fileName, newFileName);
                            }
                        }
                    }
                }
            }
        }
    }


}
