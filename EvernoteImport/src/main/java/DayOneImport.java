import diaro.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.dom4j.Document;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class DayOneImport {
    //DayOne Zip
//    private static final String DAY_ONE_ZIP = "C:\\Users\\Animesh\\Downloads\\dayOneImport\\2016-06-13-DayOne-JSON special symbols and pics formats.zip";
//   private static final String DAY_ONE_ZIP = "C:\\Users\\Animesh\\Downloads\\dayOneImport\\2016-06-22-DayOne-JSON gestas JP.zip";
   private static final String DAY_ONE_ZIP = "C:\\Users\\Animesh\\Downloads\\dayOneImport\\Export-dates.zip";
//   private static final String DAY_ONE_ZIP_Test= "C:\\Users\\Animesh\\Downloads\\dayOneImport\\Export-multiple-JSON.zip";
//   private static final String DAY_ONE_ZIP = "C:\\Users\\Animesh\\Downloads\\dayOneImport\\Export-Journal-photos.zip";
//   private static final String DAY_ONE_ZIP = "C:\\Users\\Animesh\\Downloads\\dayOneImport\\Export-All Journals.zip";
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

    //LinkedHashMaps for Tags and Locations
    private static HashMap<String, String> uidForEachTag;
    private static HashMap<String, String> uidForEachLocation;
    private static HashSet<String> locationsIdSet;
    private static HashMap<String, String> photosWithNewName;
    private static HashMap<String,String>  photosNamesWithAndWithoutExts;

    //
    public static String dateFormatDayOne = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    //Day One folder
    private static String FOLDER_UID = Entry.generateRandomUid();
    private static String FOLDER_TITLE = "Day One";
    private static String FOLDER_COLOR = "#2784B0";
    private static String DEFAULT_ZOOM = "11";


    public static void main(String[] args){
        try {

            dayOneEntriesJson = ImportUtils.mergeJsonArrays(getEntriesArrayAndImgFileName(DAY_ONE_ZIP)).toString();
            collectVariablesForList(dayOneEntriesJson);
            xmlDocument = XmlGenerator.generateXmlForDiaro(FOLDER_UID,FOLDER_TITLE,FOLDER_COLOR,uidForEachTag,locationsList,entriesList,attachmentList);
            writeXmlAndImagesToZip(xmlDocument,DAY_ONE_ZIP,OUTPUT_ZIP_FILE_PATH,photosWithNewName);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     *  This method collects all the entries from JSON file(s) and also the images name from zip file.
     * @param zipFile Dayone zip file
     * @return ArrayList of JSONArrays
     * @throws IOException Input output exception
     */
    private static ArrayList<JSONArray> getEntriesArrayAndImgFileName(String zipFile) throws IOException, JSONException {
        String json = "";
        ArrayList<JSONArray> entriesJsonArrays = new ArrayList<>();
        photosNamesWithAndWithoutExts = new HashMap<>();
        ZipFile dayOneZip = new ZipFile(zipFile);
        //enumerate though zip to find the json file
        Enumeration<? extends ZipEntry> dayOneZipEntries = dayOneZip.entries();
        while (dayOneZipEntries.hasMoreElements()) {
            ZipEntry zipEntry = dayOneZipEntries.nextElement();
            //if the file is JSON
            //collect all JSON entries in a list
            if(zipEntry.getName().endsWith(".json") && !zipEntry.isDirectory()){
                InputStream stream = dayOneZip.getInputStream(zipEntry);
                json = IOUtils.toString(stream, "UTF-8");
                JSONObject jsonObject = new JSONObject(json);
                JSONArray entries = jsonObject.getJSONArray("entries");
                entriesJsonArrays.add(entries);
                stream.close();
            }
            //if the file is a compatible attachment
            //get file name from file path
            //add the fileNames in a list
            if(ImportUtils.checkPhotosExtension(zipEntry)){
                String entryName = new File(zipEntry.getName()).getName();
                String entryNameWithoutExt = entryName.substring(0, entryName.indexOf("."));
                photosNamesWithAndWithoutExts.put(entryNameWithoutExt,entryName);
            }
        }
        dayOneZip.close();
        return entriesJsonArrays;
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
        locationsIdSet = new LinkedHashSet<>();

        JSONArray entries = new JSONArray(dayOneEntriesJson);



        //check if entries exists
        JSONObject objAtIndex;
        if(!entries.isEmpty()) {
            for (int i = 0; i < entries.length(); i++) {
                //create an Object at index
                objAtIndex = entries.getJSONObject(i);

                //-- collecting TAGS
                String tagUid = "";
                String tagTitle = "";
                //if tags Array is present
                if (objAtIndex.optJSONArray(KEY_DAYONE_TAGS) != null) {
                    JSONArray tags = objAtIndex.getJSONArray(KEY_DAYONE_TAGS);
                    for (int j = 0; j < tags.length(); j++) {

                        tagUid = Entry.generateRandomUid();

                        if (tags.getString(j) != null && !tags.getString(j).isEmpty()) {
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
                if (!ImportUtils.isNullOrEmpty(objAtIndex, KEY_DAYONE_LOCATION)) {

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

                    List<String> locationTitleValues = new ArrayList<>(Arrays.asList(placeName, localityName, administrativeArea, country));
                    //filtering out the empty values
                    locationTitleValues.removeAll(Arrays.asList("", null));
                    //if locationTitleValues is notEmpty
                    //append the values in a String
                    if (locationTitleValues.size() != 0) {
                        title = String.join("," + " ", locationTitleValues);
                    } else {
                        title = ImportUtils.concatLatLng(latitude, longitude);
                    }
                    //if title is not empty
                    //some lat,lng can be 0, ignore them
                    if (!title.isEmpty() && !latitude.equals("0.0") && !longitude.equals("0.0") && !latitude.equals("0") && !longitude.equals("0")) {

                        if (!uidForEachLocation.containsKey(title)) {
                            uidForEachLocation.put(title, Entry.generateRandomUid());
                        }
                        locationUid = uidForEachLocation.get(title);
                        Location location = new Location(locationUid, latitude, longitude, title, title, DEFAULT_ZOOM);

                        if (!locationsIdSet.contains(location.location_uid)) {
                            locationsList.add(location);
                            locationsIdSet.add(location.location_uid);
                        }
                        locationTitleValues.clear();

                    }
                }

                //--collect entries info
                String entryUid = Entry.generateRandomUid();
                String entryTitle = "";
                String[] entryTextAndTitle;
                String timeStamp = "";
                String timezoneOffset = "";
                DateTimeZone timeZone;
                String entryText = "";
                Entry entry;

                //collecting text and titles
                if (!ImportUtils.isNullOrEmpty(objAtIndex, KEY_DAYONE_TEXT)) {
                    entryText = objAtIndex.optString(KEY_DAYONE_TEXT);
                    entryTextAndTitle = ImportUtils.formatDayOneTextAndTitle(entryText);
                    entryTitle = entryTextAndTitle[0];
                    entryText = entryTextAndTitle[1];
                }
                //timezone offset and timeStamp  for DayOne
                if (!ImportUtils.isNullOrEmpty(objAtIndex, KEY_DAYONE_CREATION_DATE)) {

                    String dateString = objAtIndex.optString(KEY_DAYONE_CREATION_DATE);
                    DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormatDayOne).withZoneUTC();
                    DateTime dt = formatter.parseDateTime(dateString);
                    timeStamp = String.valueOf(dt.getMillis());

                    //check for timezone info in JSON
                    if (!ImportUtils.isNullOrEmpty(objAtIndex, KEY_DAYONE_TIMEZONE)) {

                        timeZone = DateTimeZone.forID(objAtIndex.getString(KEY_DAYONE_TIMEZONE));

                    } else {

                        timeZone = DateTimeZone.forID(TimeZone.getTimeZone(ZoneId.systemDefault()).getID());
                    }

                   timezoneOffset = ImportUtils.getUTCOffset(dateFormatDayOne,timeZone,dateString);

                }

                //looping through all the tags
                //appending the tags in a String
                if (tagsForEntryList.size() != 0) {
                    tagUid = "";
                    for (Tags tagsForEntry : tagsForEntryList) {
                        tagUid = tagUid + (",") + (String.join(",", tagsForEntry.tagsId));
                    }
                }
                 entry = new Entry(
                        entryUid,
                        timezoneOffset,
                        timeStamp,
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
                    JSONArray photosArray = objAtIndex.getJSONArray(KEY_DAYONE_PHOTOS);
                    JSONObject attachmentObjAtIndx;
                    for (int k = 0; k < photosArray.length(); k++) {

                        attachmentObjAtIndx =  photosArray.optJSONObject(k);

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
                                fileName = fileNameWithoutExt + "." + attachmentObjAtIndx.optString(KEY_DAYONE_PHOTOS_TYPE);
                                newFileName = ImportUtils.generateNewFileName(fileName, String.valueOf(i), String.valueOf(k));
                                attachment = new Attachment(Entry.generateRandomUid(),entryUid,type,newFileName);

                            //else loop through the photos name list from zip (old DayOne JSON)
                            } else {

                                if(photosNamesWithAndWithoutExts.containsKey(fileNameWithoutExt)){
                                    fileName = photosNamesWithAndWithoutExts.get(fileNameWithoutExt);
                                    newFileName = ImportUtils.generateNewFileName(fileName, String.valueOf(i), String.valueOf(k));
                                    attachment = new Attachment(Entry.generateRandomUid(),entryUid,type,newFileName);
                                }

                            }

                            //add the old name and the new name in a list
                            //add the attachment to attachments list
                            if (!photosWithNewName.containsKey(fileName)) {
                                photosWithNewName.put(fileName, newFileName);
                                if(attachment!=null) attachmentList.add(attachment);
                            }
                        }
                    }
                }
            }
        }
    }


    public static void writeXmlAndImagesToZip(Document createdDocument, String inputZipFilePath , String outputZipFilePath , HashMap oldAndNewNamesForAttachments) throws IOException,SecurityException {
        StopWatch stopwatch = new StopWatch();
        stopwatch.start();
        //creating new zipOutputStream
        ZipOutputStream append = new ZipOutputStream(new FileOutputStream(outputZipFilePath));
        //increase the buffer as required
        byte[] buffer = new byte[512];
        ZipFile journeyZipFile = new ZipFile(inputZipFilePath);
        Enumeration<? extends ZipEntry> zipEntries = journeyZipFile.entries();
        System.out.println("start appending");
        //loop through the entries
        while (zipEntries.hasMoreElements()) {
            ZipEntry newEntry = null;
            ZipEntry entry = zipEntries.nextElement();
            //creating entryName String object to get the filename without path
            String entryName = new File(entry.getName()).getName();

            //get the attachment name from map
            if (oldAndNewNamesForAttachments.containsKey(entryName)) {
                //create a new entry with the new attachment name
                newEntry = new ZipEntry("media/photo/" + oldAndNewNamesForAttachments.get(entryName));

                //checking for compatible attachments
                if (entry.getName().endsWith("jpg") || entry.getName().endsWith("png") || entry.getName().endsWith("gif") || entry.getName().endsWith("jpeg")) {
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
                    } catch (ZipException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        append.closeEntry();

        // now append xml
        ZipEntry diaroZipFile = new ZipEntry("DiaroBackup.xml");
        System.out.println("append: " + diaroZipFile.getName());
        append.putNextEntry(diaroZipFile);
        append.write(Entry.toBytes(createdDocument));
        String text = createdDocument.asXML();
        append.closeEntry();

        // close
        append.close();
        System.out.println("end appending");
        journeyZipFile.close();
        stopwatch.stop();
        System.out.println("Time taken on main Thread: " + stopwatch.getTime(TimeUnit.MILLISECONDS));

    }
}
