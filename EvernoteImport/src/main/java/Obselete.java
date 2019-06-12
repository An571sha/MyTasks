import diaro.Entry;
import org.apache.commons.io.FilenameUtils;
import org.dom4j.*;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;

import org.dom4j.io.XMLWriter;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;

import java.io.*;

import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 *
 */
public class Obselete {

    private static Document enexDocument;
    private static Document createdDocument;

    private static String ENEX_DOCUMENT_PATH = "C:\\Users\\Animesh\\Downloads\\evernoteExport\\su tagais.enex";
    private static String OUPTPUT_ZIP_PATH = "C:\\Users\\Animesh\\Downloads\\evernoteExport\\created_xml\\test.zip";
    private static String OUPTPUT_XML_PATH = "C:\\Users\\Animesh\\Downloads\\evernoteExport\\created_xml\\test.xml";

    private static String diaro_folders = "diaro_folders";
    private static String diaro_entries = "diaro_entries";
    private static String diaro_attachments = "diaro_attachments";
    private static String diaro_tags = "diaro_tags";
    private static String diaro_locations = "diaro_locations";
    private static String DEFAULT_ZOOM = "11";
    private static String TIMEZONE_OFFSET = "+00:00";

    private static String FOLDER_UID = Entry.generateRandomUid();
    private static String FOLDER_TITLE = "Evernote";
    private static String FOLDER_COLOR = "#F0B913";

    private static String NOTES = "/en-export/note";
    private static String TAGS = "/en-export/note/tag";
    private static String LATITUDE = "note-attributes/latitude";
    private static String LONGITUDE = "note-attributes/longitude";
    private static String RESOURCE_ATTRIBUTE_FILENAME = "/en-export/note/resource/resource-attributes/file-name";
    private static String DATA = "data";
    private static String MIME = "mime";
    private static String RESOURCE = "resource";
    private static String TAG = "tag";
    private static String TITLE = "title";
    private static String CONTENT = "content";
    private static String CREATED = "created";

    private static HashMap<Integer, List<String>> tagsForEachEntry;
    private static HashMap<String, String> uidForEachTag;
    private static HashMap<String, String> uidForLocations;
    private static HashMap<String,String> entry_uid_primary_photo_uid;

    private static List<String> entries_uid;
    private static List<String> tags_text;
    private static List<String> fileNameList;
    private static List<Node> tagsList;
    private static List<Node> notesList;


    private static int keyCounter = 0;


    /**
     * @param args
     * @throws DocumentException
     */
    public static void main(String[] args) throws DocumentException {
        File inputFile = new File(ENEX_DOCUMENT_PATH);
        enexDocument = parse(inputFile);
        createdDocument = createXMLDocument();
        createZipOrXmlFile(enexDocument,createdDocument);

    }

    /**
     * @param file
     * @return parsed xml Document
     * @throws DocumentException
     */
    private static Document parse(File file) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(file);
        return document;
    }


    /** collects all the nodes inside a list or a map
     *
     * @param document enex document
     */
    private static void getNodes(Document document) {

        notesList = document.selectNodes(NOTES);
        tagsList = document.selectNodes(TAGS);

        //generating unique id for every tag and mapping it.
        uidForEachTag = new HashMap<>();
        for (Node tags_title : tagsList) {
            if (tagsList.size() > 0) {
                uidForEachTag.put(tags_title.getText(), Entry.generateRandomUid());
            }
        }

        //generating unique id for every location and mapping it.
        uidForLocations = new HashMap<>();
        for (Node location : notesList) {
            if (location.selectSingleNode(LATITUDE) != null && location.selectSingleNode(LONGITUDE) != null) {
                uidForLocations.put(Entry.addLocation(location.selectSingleNode(LATITUDE).getText(), location.selectSingleNode(LONGITUDE).getText()), Entry.generateRandomUid());
            }
        }
    }

    /**
     * create xml document
     * @return xml Document for diaro
     */
    private static Document createXMLDocument() {

        getNodes(enexDocument);

        //create the root element of the document
        Document createdXmlDocument = DocumentHelper.createDocument();
        Element root = createdXmlDocument.addElement("data").addAttribute("version", "2");

        //adding folders table
        generateFolderTable(root);

        //adding tags table
        generateTagTable(root);

        //adding locations table
        generateLocationTable(root);

        //adding entries table
        generateEntriesTable(root);

        //adding attachments table
        generateAttachmentsTable(root);

        return createdXmlDocument;
    }

    /**
     * this method generates folder tags for a given list of nodes. All the folders have a constant
     * uid, title and colour. As evernote does not provide any data regarding folders.
     *
     * @param root Root element of the document
     *
     */
    private static void generateFolderTable(@NotNull Element root) {

        Element folderRoot = root.addElement("table").addAttribute("title", diaro_folders);

        for (Node node : notesList) {

            Element row =  folderRoot.addElement("r");

            row.addElement(Entry.KEY_UID).addText(FOLDER_UID);
            row.addElement(Entry.KEY_ENTRY_FOLDER_TITLE).addText(FOLDER_TITLE);
            row.addElement(Entry.KEY_ENTRY_FOLDER_COLOR).addText(FOLDER_COLOR);
        }
    }

    /**
     *
     * this method generates diaro.Tags table for diaro
     *
     * this methods uses two hashMaps to save tags, tagsForEachEntry stores the list of tag uid's as map for each entry
     * and uses a keyCounter as key, where it increment every time it finds an diaro.Entry with tag.
     * UidForEachTag is used to fetch uid for a given tag which has already been mapped in the method above.
     *
     * ## missing from Evernote -
     * ## included in Evernote - <tag></tag>
     * ## used - <tag></tag> as <tag_title></tag_title>
     * @param root Root element of the document
     */

    private static void generateTagTable(@NotNull Element root) {
        tagsForEachEntry = new HashMap<>();

        Element folderRoot = generateTableTag(TAG, diaro_tags,root);

        for (Node node : notesList) {

            if (node.selectSingleNode(TAG) != null) {

                //using KeyCounter as key to store all the tags of an diaro.Entry inside HashMap(key: counter, value: {tags_text})
                tags_text = new ArrayList<>();
                List<Node> evernote_tags = node.selectNodes(TAG);
                assert folderRoot != null;
                Element row = folderRoot.addElement("r");
                //using uidForTag to get the uid for already mapped and stored list of tags.
                for (Node tag_node : evernote_tags) {
                    row.addElement(Entry.KEY_UID).addText(uidForEachTag.get(tag_node.getText()));
                    row.addElement(Entry.KEY_ENTRY_TAGS_TITLE).addText(tag_node.getText());
                    tags_text.add(uidForEachTag.get(tag_node.getText()));

                }

                tagsForEachEntry.put(keyCounter, tags_text);
                keyCounter++;
            }
        }
    }

    /**
     * This method takes the Latitude and Longitude from the Evernote <note-attributes> and generates the diaro.Location table.
     * ## Required tags missing from Evernote - <address></address>,<title></title>,<zoom></zoom>
     * ## Included in Evernote <Latitude></Latitude>,<Longitude></Longitude>,<altitude></altitude>
     * ## used  <Latitude></Latitude>,<Longitude></Longitude>
     * @param root Root element of the document
     */

    private static void generateLocationTable(@NotNull Element root) {
        Element folderRoot = generateTableTag(LATITUDE, diaro_locations,root);

        for (Node node : notesList) {

                if (node.selectSingleNode(LATITUDE) != null && node.selectSingleNode(LONGITUDE) != null) {

                    String latitude = node.selectSingleNode(LATITUDE).getText();
                    String longitude = node.selectSingleNode(LONGITUDE).getText();

                    assert folderRoot != null;
                    Element row = folderRoot.addElement("r");
                    row.addElement(Entry.KEY_UID).addText(uidForLocations.get(Entry.addLocation(latitude, longitude)));
                    row.addElement(Entry.KEY_ENTRY_LOCATION_NAME).addText(Entry.addLocation(latitude, longitude));
                    row.addElement(Entry.KEY_ENTRY_LOCATION_ZOOM).addText(DEFAULT_ZOOM);
                }

        }
    }

    /**
     * This method generates the Entries table for diaro xml
     * ## required tags missing from evernote <tz_offset></tz_offset>
     * ## included in Evernote <title></title>,<content></content>
     * ## used <title></title>,<content></content>
     * @param root Root element of the document
     */
    private static void generateEntriesTable(@NotNull Element root) {
        //resetting the keyCounter to loop through the map for tags
        keyCounter = 0;
        Element folderRoot = root.addElement("table").addAttribute("title", diaro_entries);
        entries_uid = new ArrayList<>();
        entry_uid_primary_photo_uid = new HashMap<>();
        String primary_photo_uid;

        for (Node node : notesList) {

            if (node != null) {

                Element entriesRow =  folderRoot.addElement("r");

                String uid = Entry.generateRandomUid();
                entriesRow.addElement(Entry.KEY_UID).addText(uid);

                if (node.selectSingleNode(CREATED) != null) {
                    //only date is being is being formatted here
                    String date = node.selectSingleNode(CREATED).getText().substring(0, 8);
                    String month = date.substring(4, 6);
                    String day = date.substring(6, 8);
                    String year = date.substring(2, 4);
                    String formattedDate = String.format("%s/%s/%s", month, day, year);
                    entriesRow.addElement(Entry.KEY_ENTRY_DATE).addText(String.valueOf(Entry.dateToTimeStamp(formattedDate)));
                }

                if (node.selectSingleNode(TITLE) != null) {
                    String title = node.selectSingleNode(TITLE).getText();
                    entriesRow.addElement(Entry.KEY_ENTRY_TITLE).addText(title);
                }


                if (node.selectSingleNode(CONTENT) != null) {
                    String text = node.selectSingleNode(CONTENT).getText();
                    // using Jsoup to parse HTML inside Content
                    String parsedHtml = Jsoup.parse(text).text();
                    entriesRow.addElement(Entry.KEY_ENTRY_TEXT).addText(parsedHtml);
                }

                if (node.selectSingleNode(LATITUDE) != null && node.selectSingleNode(LONGITUDE) != null) {
                    //adding the location uid using uidForLocations Map
                    String location_uid = uidForLocations.get(Entry.addLocation(node.selectSingleNode(LATITUDE).getText(), node.selectSingleNode(LONGITUDE).getText()));
                    entriesRow.addElement(Entry.KEY_ENTRY_LOCATION_UID).addText(location_uid);
                }

                if (node.selectSingleNode(TAG) != null) {
                    //using key counter to select the tag uid from tagForEachEntry List
                    List<String> tags = tagsForEachEntry.get(keyCounter);
                    //appending the tags for each entry in a string with ,
                    entriesRow.addElement(Entry.KEY_ENTRY_TAGS).addText("," + String.join(",",tags)+ ",");
                    keyCounter++;
                }

                if(node.selectSingleNode(RESOURCE) != null){
                    //adding the uid's inside a list to display in attachments table
                    entries_uid.add(uid);
                    String mime = node.selectSingleNode("resource/mime").getText();
                    if (assertMime(mime)) {
                        //generating a primary_photo_uid for every entry_uid if resource present
                        primary_photo_uid = Entry.generateRandomUid();
                        entry_uid_primary_photo_uid.put(uid, primary_photo_uid);
                        entriesRow.addElement("primary_photo_uid").addText(primary_photo_uid);
                    }
                }

                entriesRow.addElement(Entry.KEY_ENTRY_FOLDER_UID).addText(FOLDER_UID);
                //no <tz_offset> found in evernote so it is set as +00:00
                entriesRow.addElement(Entry.KEY_ENTRY_TZ_OFFSET).addText(TIMEZONE_OFFSET);

            }
        }
    }

    /**
     * method generates the attachments table for diaro xml
     * ## required tags missing from evernote -- <position></position><type></type>
     * ## included in Evernote  <data></data><mime></mime><width></width><height></height><file-name></file-name>
     * ## used <data></data><mime></mime> <file-name></file-name>
     * (<data></data>) is used to generate the image file in createZipOrXmlFile() and is not displayed in the table
     * @param root Root element of the document
     */
    private static void generateAttachmentsTable(@NotNull Element root){
        //resetting the keyCounter to loop through the uid's for entries
        keyCounter = 0;

        Element folderRoot = generateTableTag(RESOURCE, diaro_attachments,root);
        String mime = "";
        String fileName = "";
        String attachment_uid;
        int fileNameCounter = 0;

        //find and rename duplicate filename in resources
        fileNameList = new ArrayList<>();
        findAndRenameDuplicateEntries(fileNameList);

        for (Node node : notesList) {
            int uidGeneratorCounter = 0;


            if (node.selectSingleNode(RESOURCE) != null) {
                //get the attachments and entry_uid for each note
                List<Node> attachments = node.selectNodes(RESOURCE);
                String entry_uid = entries_uid.get(keyCounter);

                for (Node attachment : attachments) {

                    try {
                        mime = attachment.selectSingleNode(MIME).getText();
                        fileName = fileNameList.get(fileNameCounter);
                        fileNameCounter++;

                    }catch( NullPointerException e){
                        e.printStackTrace();
                    }

                    if (assertMime(mime)) {
                        assert folderRoot != null;
                        //if attachment is compatible add the name in list

                        Element row =  folderRoot.addElement("r");
                        //if multiple attachments found
                        //add the generated photo_uid (line308) to the first(primary) attachment
                        //in attachments.
                        if(uidGeneratorCounter == 0){
                            row.addElement(Entry.KEY_UID).addText(entry_uid_primary_photo_uid.get(entry_uid));

                            //Generate random uid for rest
                        }else{

                            attachment_uid = Entry.generateRandomUid();
                            row.addElement(Entry.KEY_UID).addText(attachment_uid);

                        }
                        try {
                            row.addElement(Entry.KEY_ENTRY_UID).addText(entry_uid);
                        }catch (IndexOutOfBoundsException e){
                            e.printStackTrace();
                        }
                        row.addElement("type").addText("photo");
                        row.addElement(Entry.KEY_ENTRY_FILENAME).addText(fileName);
                        //could not find <position> in enex
                        //incrementing the uid generator to get the next primary_photo_uid, if present
                        uidGeneratorCounter++;
                    }

                }
                //using the keyCounter to get entry_uid
                keyCounter++;
            }
        }
    }

    /**
     * this methods generates table tag for each xml elements in xml. <table></table> is only generated when the
     * equivalent node exists in the nodesList

     * @param s1 Name of the node inside enex document
     * @param s2 Name of the table title inside xml document
     * @param root Root element of the document
     * @return <table title="s2"></table>
     */
    private static Element generateTableTag(String s1, String s2, Element root){
        for (Node node : notesList) {

            if(node.selectSingleNode(s1) != null){

                return root.addElement("table").addAttribute("title", s2);
            }
        }
        return null;
    }

    /**
     * this method creates a zip file for the given xml document and saves the images in media/photos
     * @param enexDocument input Evernote xml Document
     * @param createdDocument generated xml document for diaro
     */
    private static void createZipOrXmlFile(Document enexDocument, Document createdDocument) {
        File zipFile = new File(OUPTPUT_ZIP_PATH);
        ZipOutputStream zipOutputStream = null;
        String mime = "";
        String fileName = "";
        String baseEncoding = "";
        int counter = 0;

        try {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //get all the <resources> in a list
        List<Node> data_nodes = enexDocument.selectNodes("/en-export/note/resource");

        byte[] decoded;
        // if the list is not empty proceed to make zip file
        if (data_nodes.size() > 0) {
            for (Node attachment : data_nodes) {
                //fetch the required string
                try {
                    baseEncoding = attachment.selectSingleNode(DATA).getText();
                    fileName = fileNameList.get(counter);
                    mime = attachment.selectSingleNode(MIME).getText();
                    counter++;
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
                //check supported format
                if (assertMime(mime)) {
                    try {
                        //decode base64 encoding
                        //create a new zip entry
                        //add the zip entry and  write the decoded data
                        assert zipOutputStream != null;
                        decoded = Base64.getMimeDecoder().decode(baseEncoding);
                        ZipEntry imageOutputStream = new ZipEntry("media/photos/" + fileName);
                        zipOutputStream.putNextEntry(imageOutputStream);
                        zipOutputStream.write(decoded);
                    } catch (IllegalArgumentException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            //add the xml after the attachments have been added
            ZipEntry xmlOutputStream = new ZipEntry("test.xml");
            try {
                assert zipOutputStream != null;
                zipOutputStream.putNextEntry(xmlOutputStream);
                zipOutputStream.write(toBytes(createdDocument));
                zipOutputStream.finish();
                zipOutputStream.flush();
                zipOutputStream.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();

            }

        } else {
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

    /**
     * @param document xml document
     * @return byte[] for zipOutputStream
     */
    private static byte[] toBytes(Document document) {
        String text = document.asXML();
        return text.getBytes();
    }

    /**
     * @param mime resource type
     * @return true if resource type is compatible with diaro
     */
    private static boolean assertMime(String mime){
        return mime.equals("image/jpeg") || mime.equals("image/jpg") || mime.equals("image/gif") || mime.equals("image/png");
    }

    private static void findAndRenameDuplicateEntries(List<String> namesList) {
        String replacingString;
        List<Node> filenames = enexDocument.selectNodes(RESOURCE_ATTRIBUTE_FILENAME);
        for(Node filename: filenames){
            namesList.add(filename.getText());
        }

        for (int i = 0; i < namesList.size(); i++) {
            for (int j = i + 1; j < namesList.size(); j++) {
                if (namesList.get(i).equals(namesList.get(j))) {
                    String fileNameString = namesList.get(j);
                    String extension = FilenameUtils.getExtension(fileNameString);
                    String newString = Entry.generateRandomUid() + "." + extension;
                    replacingString = namesList.get(j).replace( namesList.get(j),newString);
                    namesList.set(i,replacingString);
                }
            }
        }
    }

}
