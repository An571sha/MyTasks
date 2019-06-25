import diaro.*;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jsoup.Jsoup;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class EvernoteImport {
    private static Document enexDocument;
    private static Document xmlDocument;

    //input and output paths
    private static String ENEX_DOCUMENT_PATH = "C:\\Users\\Animesh\\Downloads\\evernoteExport\\sample.enex";
    private static String OUPTPUT_ZIP_PATH = "C:\\Users\\Animesh\\Downloads\\evernoteExport\\created_xml\\diaro_evernote_import.zip";
    private static String OUPTPUT_XML_PATH = "C:\\Users\\Animesh\\Downloads\\evernoteExport\\created_xml\\diaro_import.xml";

    //evernote enex nodes
    private static String NOTES = "/en-export/note";
    private static String LATITUDE = "note-attributes/latitude";
    private static String LONGITUDE = "note-attributes/longitude";
    private static String RESOURCE_ATTRIBUTE_FILENAME = "resource-attributes/file-name";
    private static String TITLE = "title";
    private static String CONTENT = "content";
    private static String CREATED = "created";
    private static String RESOURCE = "resource";
    private static String MIME = "mime";
    private static String DATA = "data";
    private static String TAGS = "/en-export/note/tag";

    private static String DEFAULT_ZOOM = "11";

    //lists
    private static Set<String> fileNameSet;
    private static List<Node> nodeList;
    private static List<Entry> entriesList;
    private static List<Tags> tagsList;
    private static List<Tags> tagsForEntryList;
    private static List<Location> locationsList;
    private static List<Attachment> attachmentList;

    private static HashMap<String, String> uidForEachTag;
    private static HashMap<String, String> uidForEachLocation;

    //evernote folder
    private static String FOLDER_UID = Entry.generateRandomUid();
    private static String FOLDER_TITLE = "Evernote";
    private static String FOLDER_COLOR = "#F0B913";

    private static String TAG = "tag";

    public static void main(String[] args) throws DocumentException {
        File inputFile = new File(ENEX_DOCUMENT_PATH);
        enexDocument =  parseXml(inputFile);
        collectVariables(enexDocument);
        xmlDocument = XmlGenerator.generateXmlForDiaro(FOLDER_UID,FOLDER_TITLE,FOLDER_COLOR,uidForEachTag,locationsList,entriesList,attachmentList);
        createZipOrXmlFile(xmlDocument);
    }

    private static Document parseXml(File file) throws DocumentException {
        SAXReader reader = new SAXReader();
        return reader.read(file);
    }

    /**this method collects all the nodes and variables, and adds them in the corresponding
     * <p>
     * Object list.
     *
     * @param document enex document
     */
    private static void collectVariables(Document document){
        //collecting all the nodes inside a list
        nodeList = document.selectNodes(NOTES);

        //initialising the lists
        entriesList = new ArrayList<>();
        locationsList = new ArrayList<>();
        attachmentList = new ArrayList<>();
        tagsForEntryList = new ArrayList<>();

        //hashSet for handling the duplicate fileNames
        fileNameSet = new LinkedHashSet<>();

        //linkedHashMap for Storing the uid for each tag
        uidForEachTag = new LinkedHashMap<>();
        uidForEachLocation = new LinkedHashMap<>();

        Tags tags;
        Location location;
        Entry entry;
        Attachment attachment;

        byte[] decoded = null;

        //looping through the list
        for(Node node : nodeList){

            String title = "";
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

            //get all tags including duplicates
            if( node.selectSingleNode(TAG) !=null) {
                List<Node> evernote_tags = node.selectNodes(TAG);
                for (Node evenote_tag : evernote_tags) {
                    //generate unique id for the tag and map it.
                    //if the name id pair does not exist
                    if(!uidForEachTag.containsKey(evenote_tag.getText())) {
                        uidForEachTag.put(evenote_tag.getText(), Entry.generateRandomUid());
                    }
                    //create new diaro.Tags Object
                    tags = new Tags(uidForEachTag.get(evenote_tag.getText()),evenote_tag.getText());
                    tagsForEntryList.add(tags);
                }
            }

            //get all Locations
            // using diaro.Entry.concatLatLng to add title as {<Latitude></Latitude>,<Longitude></Longitude>}
            if( node.selectSingleNode(LATITUDE) !=null && node.selectSingleNode(LONGITUDE) !=null) {
                String latitude = String.format("%.5f",Double.parseDouble(node.selectSingleNode(LATITUDE).getText()));
                String longitude = String.format("%.5f",Double.parseDouble(node.selectSingleNode(LONGITUDE).getText()));
                String location_title = (ImportStringUtils.concatLatLng(latitude, longitude));

                //mapping every uid to the address
                if(!uidForEachLocation.containsKey(location_title)) {
                    uidForEachLocation.put(location_title, Entry.generateRandomUid());
                }
                location_uid = uidForEachLocation.get(location_title);
                location = new Location(location_uid,latitude,longitude,location_title, location_title, DEFAULT_ZOOM);
                locationsList.add(location);
            }

            //get all Entries
            if(node.selectSingleNode(TITLE) != null ){
                 title = node.selectSingleNode(TITLE).getText();
            }
            if(node.selectSingleNode(CONTENT) != null ){
                String text = node.selectSingleNode(CONTENT).getText();
                // using Jsoup to parseXml HTML inside Content
                parsedHtml = Jsoup.parse(text).text();
            }
            if(node.selectSingleNode(CREATED) != null){
                String date = node.selectSingleNode(CREATED).getText().substring(0, 8);
                String month = date.substring(4, 6);
                String day = date.substring(6, 8);
                String year = date.substring(2, 4);
                formattedDate = String.format("%s/%s/%s", month, day, year);
            }

            //get all the tags
            //append all the string stringBuilder
            if(tagsForEntryList.size() != 0){
                for(Tags tag : tagsForEntryList) {
                  tag_uid = tag_uid + (",") + (String.join(",", tag.tagsId));
                }
                tag_uid = tag_uid + (",");
                System.out.println(tag_uid);

            }
            entry = new Entry(formattedDate,parsedHtml,title,FOLDER_UID,location_uid,tag_uid);
            //clear the list
            //clear the tag_uid variable for next loop
            tagsForEntryList.clear();
            tag_uid = "";
           //add entry in the list
            entriesList.add(entry);

            //get all the attachments
            if (node.selectSingleNode(RESOURCE) != null) {
                List<Node> attachmentsForEachEntry = node.selectNodes(RESOURCE);
                //loop through all the attachments for a single note/entry
                for (Node attachmentForeach : attachmentsForEachEntry) {
                    try {
                        mime = attachmentForeach.selectSingleNode(MIME).getText();

                    }catch( NullPointerException e){
                        e.printStackTrace();
                    }
                    // check for a valid attachment
                    if(checkForImageMime(mime)) {
                        try {
                            fileName = attachmentForeach.selectSingleNode(RESOURCE_ATTRIBUTE_FILENAME).getText();
                            attachment_uid = Entry.generateRandomUid();
                            entry_uid = entry.uid;
                            baseEncoding = attachmentForeach.selectSingleNode(DATA).getText();
                            //----- please do a check for valid Base64--
                            decoded = Base64.getMimeDecoder().decode(baseEncoding);
                            // assign primary_photo uid to entry
                            entry.primary_photo_uid = attachment_uid;
                        }catch(IllegalArgumentException | NullPointerException e){
                            e.printStackTrace();
                        }
                        // check if the fileName already exists
                        // if true generate a new fileName to prevent duplicates
                        if(fileNameSet.add(fileName)){
                            fileNameSet.add(fileName);
                            attachment = new Attachment(attachment_uid, entry_uid, type,fileName,decoded);
                        }else{
                            String newFileName = ImportStringUtils.generateNewFileName(fileName);
                            fileNameSet.add(newFileName);
                            attachment = new Attachment(attachment_uid, entry_uid, type,newFileName,decoded);
                        }
                        attachmentList.add(attachment);
                    }
                }
            }
        }

    }

    private static boolean checkForImageMime(String mime){
        return mime.equals("image/jpeg") || mime.equals("image/jpg") || mime.equals("image/gif") || mime.equals("image/png");
    }


    /**
     * this method creates a zip file for the given xml document and saves the images in media/photos
     * <p>
     * @param createdDocument generated xml document for diaro
     */
    private static void createZipOrXmlFile(Document createdDocument) {
        File zipFile = new File(OUPTPUT_ZIP_PATH);
        ZipOutputStream zipOutputStream = null;
        String fileName;
        byte[] decoded;

        try {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if(attachmentList.size() > 0){
            for (Attachment attachment : attachmentList) {
                try {
                    //create a new zip entry
                    //add the zip entry and  write the decoded data
                    assert zipOutputStream != null;
                    fileName = attachment.filename;
                    decoded = attachment.data;
                    ZipEntry imageOutputStream = new ZipEntry("media/photo/" + fileName);
                    zipOutputStream.putNextEntry(imageOutputStream);
                    zipOutputStream.write(decoded);

                } catch (IllegalArgumentException | IOException e) {
                    e.printStackTrace();
                }
            }
            //add the xml after the attachments have been added
            ZipEntry xmlOutputStream = new ZipEntry("DiaroBackup.xml");
            try {
                zipOutputStream.putNextEntry(xmlOutputStream);
                zipOutputStream.write(Entry.toBytes(createdDocument));
                //closing the stream
                //! not closing the stream can result in malformed zip files
                zipOutputStream.finish();
                zipOutputStream.flush();
                zipOutputStream.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();

            }
        }else{
            //if no attachments found
            // add the xml file only
            OutputFormat format = OutputFormat.createPrettyPrint();
            try {
                OutputStream outputStream = new FileOutputStream(OUPTPUT_XML_PATH);
                XMLWriter writer = new XMLWriter(outputStream, format);
                writer.write(createdDocument);
            } catch (IOException e ) {
                e.printStackTrace();
            }
        }
    }


}
