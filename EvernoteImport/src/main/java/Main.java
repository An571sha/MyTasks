import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

public class Main {

    private static Document enexDocument;

    private static String FOLDER = "diaro_folders";
    private static String ENTRY = "diaro_entries";
    private static String ATTACHMENTS = "diaro_attachments";
    private static String TAGS = "diaro_tags";
    private static String LOCATION = "diaro_locations";
    private static String DEFAULT_ZOOM = "11";

    private static String TIMEZONE_OFFSET = "00:00";

    private static String FOLDER_UID = Entry.generateRandomUid();
    private static String FOLDER_TITLE = "Evernote";
    private static String FOLDER_COLOR = "#F0B913";

    private static String LATITUDE = "note-attributes/latitude";
    private static String LONGITUDE = "note-attributes/longitude";
    private static String RESOURCE = "resource";
    private static String TAG = "tag";
    private static String TITLE = "title";
    private static String CONTENT = "content";
    private static String CREATED = "created";

    private static HashMap<Integer, List<String>> tagsForEachEntry;
    private static HashMap<String, String> uidForEachTag;
    private static HashMap<String, String> uidForLocations;

    private static List<String> tags_text;
    private static List<Node> tagsList;
    private static List<Node> notesList;

    private static int keyCounter = 0;


    public static void main(String[] args) throws DocumentException {
        File inputFile = new File("C:\\Users\\Animesh\\Downloads\\evernoteExport\\su tagais.enex");
        enexDocument = parse(inputFile);
        createXMLDocument();

    }

    public static Document parse(File file) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(file);
        return document;
    }

    //code will be configured later for multiple entries to duplicate the xml from Diaro.
    public static void getNodes(Document document) {

        notesList = document.selectNodes("/en-export/note");
        tagsList = document.selectNodes("/en-export/note/tag");

        //generating unique id for every tag and maps it.
        uidForEachTag = new HashMap<String, String>();
        for (Node tags_title : tagsList) {
            if (tagsList.size() > 0) {
                uidForEachTag.put(tags_title.getText(), Entry.generateRandomUid());
            }
        }

        //generating unique id for every location and maps it.
        uidForLocations = new HashMap<String, String>();
        for (Node location : notesList) {
            if (location.selectSingleNode(LATITUDE) != null && location.selectSingleNode(LONGITUDE) != null) {
                uidForLocations.put(Entry.addLocation(location.selectSingleNode(LATITUDE).getText(), location.selectSingleNode(LONGITUDE).getText()), Entry.generateRandomUid());
            }
        }

        System.out.println("----------------------------");

    }

    public static Document createXMLDocument() {
        getNodes(enexDocument);

        Document createdXmlDocument = DocumentHelper.createDocument();
        Element root = createdXmlDocument.addElement("data").addAttribute("version", "2");

        tagsForEachEntry = new HashMap<Integer, List<String>>();

        generateFolderTagForXml(root);

        generateTagTagsForXml(root);

        generateLocationTagForXml(root);

        //resetting the keyCounter to parse through the map for tags
        keyCounter = 0;

        generateEntriesTagForXml(root);

        generateAttachmentsForXml(root);


        //displaying on the console
        OutputFormat format = OutputFormat.createPrettyPrint();
        OutputStream outputStream;
        XMLWriter writer;
        try {
            outputStream = new FileOutputStream("C:\\Users\\Animesh\\Downloads\\evernoteExport\\created_xml\\created.xml");
//            writer = new XMLWriter(System.out, format);
            writer = new XMLWriter(outputStream, format);
            writer.write(createdXmlDocument);
        } catch (IOException e ) {
            e.printStackTrace();
        }
        return createdXmlDocument;
    }

    public static void generateFolderTagForXml(@NotNull Element root) {

        Element folderRoot = root.addElement("table").addAttribute("title", FOLDER);

        for (Node node : notesList) {

            Element row =  folderRoot.addElement("r");

            row.addElement(Entry.KEY_ENTRY_FOLDER_UID).addText(FOLDER_UID);
            row.addElement(Entry.KEY_ENTRY_FOLDER_TITLE).addText(FOLDER_TITLE);
            row.addElement(Entry.KEY_ENTRY_FOLDER_COLOR).addText(FOLDER_COLOR);
        }
    }

    /**
     * this methods uses two hashMaps to save tags, tagsForEachEntry stores the list of tag uid's as map for each entry
     * as a keyCounter as key, where it increment every time it finds an Entry with tag.
     * UidForEachTag is used to fetch uid for a given tag which has already been mapped in the method above.
     */

    public static void generateTagTagsForXml(@NotNull Element root) {
        Element folderRoot = root.addElement("table").addAttribute("title", TAGS);

        for (Node node : notesList) {

            if (node.selectSingleNode(TAG) != null) {

                //using KeyCounter as key to store all the tags of an Entry inside HashMap(key: counter, value: {tags_text})
                keyCounter++;
                tags_text = new ArrayList<String>();
                List<Node> evernote_tags = node.selectNodes(TAG);
                Element row =  folderRoot.addElement("r");;

                //using uidForTag to get the uid for already mapped and stored list of tags.
                for (Node tag_node : evernote_tags) {
                    row.addElement(Entry.KEY_ENTRY_TAGS_UID).addText(uidForEachTag.get(tag_node.getText()));
                    row.addElement(Entry.KEY_ENTRY_TAGS_TITLE).addText(tag_node.getText());
                    tags_text.add(uidForEachTag.get(tag_node.getText()));

                }

                tagsForEachEntry.put(keyCounter, tags_text);
            }
        }
    }

    public static void generateLocationTagForXml(@NotNull Element root) {
        Element folderRoot = root.addElement("table").addAttribute("title", LOCATION);

        for (Node node : notesList) {

            if (node != null) {

                if (node.selectSingleNode(LATITUDE) != null && node.selectSingleNode(LONGITUDE) != null) {

                    String latitude = node.selectSingleNode(LATITUDE).getText();
                    String longitude = node.selectSingleNode(LONGITUDE).getText();

                    Element row =  folderRoot.addElement("r");;

                    row.addElement(Entry.KEY_ENTRY_LOCATION_UID).addText(uidForLocations.get(Entry.addLocation(latitude, longitude)));
                    row.addElement(Entry.KEY_ENTRY_LOCATION_NAME).addText(Entry.addLocation(latitude, longitude));
                    row.addElement(Entry.KEY_ENTRY_LOCATION_ZOOM).addText(DEFAULT_ZOOM);
                }
            }
        }
    }

    public static void generateEntriesTagForXml(@NotNull Element root) {

        Element folderRoot = root.addElement("table").addAttribute("title", ENTRY);

        for (Node node : notesList) {


            if (node != null) {

                Element row =  folderRoot.addElement("r");;

                String uid = Entry.generateRandomUid();
                row.addElement(Entry.KEY_UID).addText(uid);

                if (node.selectSingleNode(TITLE) != null) {
                    String title = node.selectSingleNode(TITLE).getText();
                    row.addElement(Entry.KEY_ENTRY_TITLE).addText(title);
                }
                if (node.selectSingleNode(CONTENT) != null) {
                    String text = node.selectSingleNode(CONTENT).getText();
                    // using Jsoup to parse HTML inside Content
                    String parsedHtml = Jsoup.parse(text).text();
                    row.addElement(Entry.KEY_ENTRY_TEXT).addText(parsedHtml);
                }

                if (node.selectSingleNode(LATITUDE) != null && node.selectSingleNode(LONGITUDE) != null) {
                    String location_uid = uidForLocations.get(Entry.addLocation(node.selectSingleNode(LATITUDE).getText(), node.selectSingleNode(LONGITUDE).getText()));
                    row.addElement(Entry.KEY_ENTRY_LOCATION_UID).addText(location_uid);
                }

                if (node.selectSingleNode(TAG) != null) {
                    keyCounter++;
                    List<String> tag = tagsForEachEntry.get(keyCounter);
                    row.addElement(Entry.KEY_ENTRY_TAGS).addText(String.valueOf(tag));
                }

                if (node.selectSingleNode(CREATED) != null) {
                    //only date is being is being formatted here
                    String date = node.selectSingleNode(CREATED).getText().substring(0, 8);
                    String month = date.substring(4, 6);
                    String day = date.substring(6, 8);
                    String year = date.substring(2, 4);
                    String formattedDate = String.format("%s/%s/%s", month, day, year);
                    row.addElement(Entry.KEY_ENTRY_DATE).addText(String.valueOf(Entry.dateToTimeStamp(formattedDate)));
                }

                row.addElement(Entry.KEY_ENTRY_FOLDER_UID).addText(FOLDER_UID);
                //no offset found in evernote
                row.addElement(Entry.KEY_ENTRY_TZ_OFFSET).addText(TIMEZONE_OFFSET);

            }
        }
    }

    public static void generateAttachmentsForXml(@NotNull Element root){
        Element folderRoot = root.addElement("table").addAttribute("title", ATTACHMENTS);

        for (Node node : notesList) {

            if (node.selectSingleNode(RESOURCE) != null) {
                //           Element row = generateRow(ATTACHMENTS, root);
                List<Node> attachments = node.selectNodes(RESOURCE);
                String partSeparator = ",";
                byte[] decoded;

                for (Node attachment : attachments) {
                    String mime = attachment.selectSingleNode("mime").getText();
                    String baseEncoding = attachment.selectSingleNode("data").getText();
                    String fileName = attachment.selectSingleNode("resource-attributes/file-name").getText();

                    if (mime.equals("image/jpeg") || mime.equals("image/jpg") || mime.equals("image/gif") || mime.equals("image/png")) {

                        if (baseEncoding.contains(partSeparator)) {
                            String encodedImg = baseEncoding.split(partSeparator)[1];
                            decoded = Base64.getMimeDecoder().decode(encodedImg.getBytes(StandardCharsets.UTF_8));
                        } else {
                            decoded = Base64.getMimeDecoder().decode(baseEncoding);
                        }

                        try {
                            FileOutputStream outputStream = new FileOutputStream("C:\\Users\\Animesh\\Downloads\\evernoteExport\\created_xml\\media\\photos" + fileName);
                            outputStream.write(decoded);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                }
            }
        }

    }


}
