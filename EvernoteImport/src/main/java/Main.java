import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jetbrains.annotations.NotNull;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.cert.CRLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {

    private static Document enexDocument;

    private static String FOLDER = "diaro_folders";
    private static String ENTRY = "diaro_entries";
    private static String TAGS = "diaro_tags";
    private static String LOCATION = "diaro_locations";
    private static String DEFAULT_ZOOM = "11";

    private static String FOLDER_UID = Entry.generateRandomUid();
    private static String FOLDER_TITLE = "Evernote";
    private static String FOLDER_COLOR = "#F0B913";

    private static String LATITUDE = "note-attributes/latitude";
    private static String LONGITUDE = "note-attributes/longitude";
    private static String TAG = "tag";
    private static String TITLE = "title";
    private static String CONTENT = "content";
    private static String CREATED = "created";

    private static  HashMap<Integer,List<String>> tagsForEachEntry;
    private static  HashMap<String,String> uidForEachTag;
    private static  HashMap<String,String> uidForLocationMap;

    private static  List<String> tags_text;
    private static  List<Node> tagsList;
    private static  List<Node> notesList;

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

        //generating unique id for every tag and mapping it.
        uidForEachTag = new HashMap<String, String>();
        for(Node tags_title : tagsList){
            if(tagsList.size() > 0 ) {
                uidForEachTag.put(tags_title.getText(), Entry.generateRandomUid());
            }
        }

        //generating unique id for every location and mapping it.
        uidForLocationMap = new HashMap<String, String>();
        for(Node location : notesList){
            if(location.selectSingleNode(LATITUDE)!=null && location.selectSingleNode(LONGITUDE)!=null) {
                uidForLocationMap.put(Entry.addLocation(location.selectSingleNode(LATITUDE).getText(), location.selectSingleNode(LONGITUDE).getText()), Entry.generateRandomUid());
            }
        }

        System.out.println("----------------------------");

    }

    public static Document createXMLDocument(){
        getNodes(enexDocument);

        Document createdXmlDocument = DocumentHelper.createDocument();
        Element root = createdXmlDocument.addElement("data").addAttribute("version","2");

        tagsForEachEntry = new HashMap<Integer,List<String>>();


        for (Node node : notesList) {
            generateFolderTagForXml(root);
        }

        for (Node node : notesList) {
            generateTagTagsForXml(root,node);
        }


        for (Node node : notesList) {
            generateLocationTagForXml(root,node);
        }

        keyCounter = 0;

        for (Node node : notesList) {
            generateEntriesTagForXml(root,node);
        }

        //displaying on the console
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer = null;
        try {
            writer = new XMLWriter( System.out, format );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            writer.write( createdXmlDocument );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return createdXmlDocument;
    }

    //remove these methods later
    public static void generateFolderTagForXml(@NotNull Element root){


        Element row = generateRow(FOLDER,root);

        row.addElement(Entry.KEY_ENTRY_FOLDER_UID).addText(FOLDER_UID);
        row.addElement(Entry.KEY_ENTRY_FOLDER_TITLE).addText(FOLDER_TITLE);
        row.addElement(Entry.KEY_ENTRY_FOLDER_COLOR).addText(FOLDER_COLOR);


    }

    /**
     * this methods uses two hashMaps to save tags, tagsForEachEntry stores the list of tag uid's as map for each entry
     * as a keyCounter as key, where it increment every time it finds an Entry with tag.
     * UidForEachTag is used to fetch uid for a given tag which has already been mapped in the method above.
     */

    public static void generateTagTagsForXml(@NotNull Element root, Node node){

        if (node.selectNodes(TAG) != null) {

            //using KeyCounter as key to store all the tags of an Entry inside HashMap(key: counter, value: {tags_text})
            keyCounter++;
            tags_text =  new ArrayList<String>();
            List<Node> evernote_tags = node.selectNodes(TAG);
            Element row = generateRow(TAGS,root);

            //using uidForTag to get the uid for already mapped and stored list of tags.
            for(Node tag_node : evernote_tags){
                row.addElement(Entry.KEY_ENTRY_TAGS_UID).addText(uidForEachTag.get(tag_node.getText()));
                row.addElement(Entry.KEY_ENTRY_TAGS_TITLE).addText(tag_node.getText());
                tags_text.add(uidForEachTag.get(tag_node.getText()));

            }

            tagsForEachEntry.put(keyCounter,tags_text);

        }
    }

    public static void generateLocationTagForXml(@NotNull Element root, Node node){
        if (node != null) {

            if (node.selectSingleNode(LATITUDE) != null && node.selectSingleNode(LONGITUDE) != null) {

                    String latitude = node.selectSingleNode(LATITUDE).getText();
                    String longitude = node.selectSingleNode(LONGITUDE).getText();

                    Element row = generateRow(LOCATION,root);

                    row.addElement(Entry.KEY_ENTRY_LOCATION_UID).addText(uidForLocationMap.get(Entry.addLocation(latitude, longitude)));
                    row.addElement(Entry.KEY_ENTRY_LOCATION_NAME).addText(Entry.addLocation(latitude, longitude));
                    row.addElement(Entry.KEY_ENTRY_LOCATION_ZOOM).addText(DEFAULT_ZOOM);
            }
        }
    }

    public static void generateEntriesTagForXml(@NotNull Element root, Node node){


        if(node != null){

            Element row = generateRow(ENTRY,root);

            String uid = Entry.generateRandomUid();
            row.addElement(Entry.KEY_UID).addText(uid);

            if(node.selectSingleNode(TITLE)!= null) {
                String title = node.selectSingleNode(TITLE).getText();
                row.addElement(Entry.KEY_ENTRY_TITLE).addText(title);
            }
            if(node.selectSingleNode(CONTENT)!= null) {
                String text = node.selectSingleNode(CONTENT).getText();
                row.addElement(Entry.KEY_ENTRY_TEXT).addText(text);
            }

            if(node.selectSingleNode(LATITUDE)!=null && node.selectSingleNode(LONGITUDE)!=null){
                String location_uid = uidForLocationMap.get(Entry.addLocation(node.selectSingleNode(LATITUDE).getText(),
                        node.selectSingleNode(LONGITUDE).getText()));
                row.addElement(Entry.KEY_ENTRY_LOCATION_UID).addText(location_uid);
            }

            if(node.selectNodes(TAG) != null ) {
                keyCounter++;
                List tag = tagsForEachEntry.get(keyCounter);
                row.addElement(Entry.KEY_ENTRY_TAGS).addText(String.valueOf(tag));
            }

            if(node.selectSingleNode(CREATED)!= null) {
                String date = node.selectSingleNode(CREATED).getText().substring(0,7);
                row.addElement(Entry.KEY_ENTRY_DATE).addText(date);
            }

            row.addElement(Entry.KEY_ENTRY_FOLDER_UID).addText(FOLDER_UID);

        }
    }

    public static Element generateRow(String tableName , Element root){

        Element folderRoot = root.addElement("table").addAttribute("title",tableName);
        return folderRoot.addElement("r");
    }

}
