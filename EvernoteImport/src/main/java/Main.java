import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jetbrains.annotations.NotNull;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class Main {

    private static Document document;
    private static String FOLDER = "diaro_folders";
    private static String TAGS = "tags";
    private static String FOLDER_UID = Entry.generateRandomUid();
    private static String FOLDER_TITLE = "EVERNOTE";
    private static String FOLDER_COLOR = "#F0B913";


    public static void main(String[] args) throws DocumentException {
        File inputFile = new File("C:\\Users\\Animesh\\Downloads\\evernoteExport\\sample.enex");
        document = parse(inputFile);
        getNodes(document);
    }

    public static Document parse(File file) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(file);
        return document;
    }

    public static void getNodes(Document document) {

        List<Node> nodes = document.selectNodes("/en-export/note");
        System.out.println("----------------------------");

        for (Node node : nodes) {
            createXMLDocument(node);
        }
    }

    public static Document createXMLDocument(Node node){
        Document createdDocument = DocumentHelper.createDocument();
        Element root = createdDocument.addElement("data").addAttribute("version","2");
        createFolderTag(root);
        createTagsTag(root,node);

        OutputFormat format = OutputFormat.createPrettyPrint();
        //displaying on the console
        XMLWriter writer = null;
        try {
            writer = new XMLWriter( System.out, format );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            writer.write( createdDocument );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return createdDocument;
    }

    public static Element createFolderTag(@NotNull Element root){
        Element folderRoot = root.addElement("table").addAttribute("name",FOLDER);
        Element row = folderRoot.addElement("r");
        row.addElement(Entry.KEY_ENTRY_FOLDER_UID).addText(FOLDER_UID);
        row.addElement(Entry.KEY_ENTRY_FOLDER_TITLE).addText(FOLDER_TITLE);
        row.addElement(Entry.KEY_ENTRY_FOLDER_COLOR).addText(FOLDER_COLOR);

        Folder folder = new Folder(FOLDER_TITLE,FOLDER_COLOR,FOLDER_UID);

        return row;

    }

    public static Element createTagsTag(@NotNull Element root, Node node){
        String evernote_tag = node.selectSingleNode("tag").getText();
        Element tagsRoot = root.addElement("tags").addAttribute("name",TAGS);
        Element row = tagsRoot.addElement("r");
        row.addElement(Entry.KEY_ENTRY_TAGS).addText(evernote_tag);

        return row;
    }
}
