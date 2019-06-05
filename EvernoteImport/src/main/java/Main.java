import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jetbrains.annotations.NotNull;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static Document enexDocument;
    private static String FOLDER = "diaro_folders";
    private static String TAGS = "diaro_tags";
    private static String LOCATION = "diaro_locations";
    private static String DEFAULT_ZOOM = "11";

    private static String FOLDER_UID = Entry.generateRandomUid();
    private static String LOCATION_UID = Entry.generateRandomUid();


    private static String FOLDER_TITLE = "Evernote";
    private static String FOLDER_COLOR = "#F0B913";

    private static  List<Node> tagsList;
    private static  List<Node> locationsList;
    private static  List<Node> entriesContentList;

    private static Folder folder;
    private static Tags tags;
    private static Location location;



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

        tagsList = document.selectNodes("/en-export/note");
        locationsList = document.selectNodes("/en-export/note/note-attributes");
        entriesContentList = document.selectNodes("/en-export/note/content");
        System.out.println("----------------------------");

    }

    public static Document createXMLDocument(){
        getNodes(enexDocument);

        Document createdXmlDocument = DocumentHelper.createDocument();
        Element root = createdXmlDocument.addElement("data").addAttribute("version","2");

        for (Node node : tagsList) {
            createTagsTag(root,node);
        }

        for (Node node : locationsList) {
            createLocationTag(root,node);
            createFolderTag(root);
        }


        OutputFormat format = OutputFormat.createPrettyPrint();
        //displaying on the console
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
    //when data is configured later, it would go through each node in search for tags and save those tags in Tags[]

    public static Element createFolderTag(@NotNull Element root){
        Element folderRoot = root.addElement("table").addAttribute("title",FOLDER);
        Element row = folderRoot.addElement("r");

        row.addElement(Entry.KEY_ENTRY_FOLDER_UID).addText(FOLDER_UID);
        row.addElement(Entry.KEY_ENTRY_FOLDER_TITLE).addText(FOLDER_TITLE);
        row.addElement(Entry.KEY_ENTRY_FOLDER_COLOR).addText(FOLDER_COLOR);

        folder = new Folder(FOLDER_TITLE,FOLDER_COLOR,FOLDER_UID);

        return row;

    }

    public static Element createTagsTag(@NotNull Element root, Node node){
        if (node.selectNodes("tag") != null) {

            List<Node> evernote_tags = node.selectNodes("tag");
            Element tagsRoot = root.addElement("tags").addAttribute("title", TAGS);
            Element row = tagsRoot.addElement("r");

            for(Node tags : evernote_tags) {

                String TAG_UID = Entry.generateRandomUid();
                row.addElement(Entry.KEY_ENTRY_TAGS_UID).addText(TAG_UID);
                row.addElement(Entry.KEY_ENTRY_TAGS_TITLE).addText(tags.getText());
            }

          //  tags = new Tags(TAG_UID, tags.toString());

            return row;

        } else {

            return null;
        }
    }

    public static Element createLocationTag(@NotNull Element root, Node node){
        if (node !=null) {

            if (node.selectSingleNode("latitude") != null &&
                    node.selectSingleNode("longitude") != null) {

                    String latitude = node.selectSingleNode("latitude").getText();
                    String longitude = node.selectSingleNode("longitude").getText();

                    Element locationsRoot = root.addElement("table").addAttribute("title", LOCATION);
                    Element row = locationsRoot.addElement("r");
                    row.addElement(Entry.KEY_ENTRY_LOCATION_UID).addText(LOCATION_UID);
                    row.addElement(Entry.KEY_ENTRY_LOCATION_NAME).addText(Entry.addLocation(latitude, longitude));
                    row.addElement(Entry.KEY_ENTRY_LOCATION_ZOOM).addText(DEFAULT_ZOOM);

                    location = new Location(Entry.addLocation(latitude, longitude), LOCATION_UID, DEFAULT_ZOOM);
                    return row;
            }
        }
        return null;
    }

//    public static Element createEntryTag(@NotNull Element root, Node node){
//
//    }

}
