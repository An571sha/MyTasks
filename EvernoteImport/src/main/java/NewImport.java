import org.apache.commons.io.FilenameUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.jsoup.Jsoup;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NewImport {
    private static Document enexDocument;
    private static String ENEX_DOCUMENT_PATH = "C:\\Users\\Animesh\\Downloads\\evernoteExport\\su tagais.enex";

    private static String NOTES = "/en-export/note";
    private static String LATITUDE = "note-attributes/latitude";
    private static String LONGITUDE = "note-attributes/longitude";
    private static String RESOURCE_ATTRIBUTE_FILENAME = "/en-export/note/resource/resource-attributes/file-name";
    private static String TITLE = "title";
    private static String CONTENT = "content";
    private static String CREATED = "created";
    private static String RESOURCE = "resource";
    private static String MIME = "mime";


    private static String DEFAULT_ZOOM = "11";

    private static List<String> fileNameList;
    private static List<Node> nodeList;
    private static List<Entry> entriesList;
    private static List<Tags> tagsList;
    private static List<Location> locationsList;
    private static List<Folder> foldersList;
    private static List<Attachments> attachmentsList;

    private static HashMap<String, String> uidForEachTag;
    private static HashMap<String, List<Tags>> tagsForEachEntry; //use this for generating row, not the list

    private static String FOLDER_UID = Entry.generateRandomUid();
    private static String FOLDER_TITLE = "Evernote";
    private static String FOLDER_COLOR = "#F0B913";

    private static String TAG = "tag";

    public static void main(String[] args) throws DocumentException {
        File inputFile = new File(ENEX_DOCUMENT_PATH);
        enexDocument =  parse(inputFile);
        collectVariables(enexDocument);
    }

    private static Document parse(File file) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(file);
        return document;
    }


    private static void collectVariables(Document document){
        nodeList = document.selectNodes(NOTES);
        foldersList = new ArrayList<>();
        tagsList = new ArrayList<>();
        entriesList = new ArrayList<>();
        locationsList = new ArrayList<>();
        fileNameList = new ArrayList<>();

        uidForEachTag = new HashMap<>();
        tagsForEachEntry = new HashMap<>();

        String title = "";
        String parsedHtml = "";
        String formattedDate = "";
        String tags_uid = "";
        String location_uid = "";
        String mime = "";
        String fileName = "";
        String attachment_uid;
        String entry_uid = "";
        String type = "photo";

        Tags tags;
        Location location;
        Entry entry;
        Attachments attachment;

        int locationCounter = 0;
        int fileNameCounter = 0;

        //find and rename duplicate filename in resources
        findAndRenameDuplicateEntries(fileNameList);


        for(Node node : nodeList){

            foldersList.add((new Folder(FOLDER_TITLE,FOLDER_COLOR,FOLDER_UID)));

            //get tags
            if( node.selectSingleNode(TAG) !=null) {
                String tag_title = node.selectSingleNode(TAG).getText();
                //store each title with a unique id
                uidForEachTag.put(tag_title, Entry.generateRandomUid());
                List<Node> evernote_tags = node.selectNodes(TAG);
                for (Node evenote_tags : evernote_tags) {
                    tags = new Tags(evenote_tags.getText(), uidForEachTag.get(evenote_tags.getText()));
                    tagsList.add(tags);
                }
            }

            //get Locations
            if( node.selectSingleNode(LATITUDE) !=null && node.selectSingleNode(LONGITUDE) !=null) {
                String latitude = node.selectSingleNode(LATITUDE).getText();
                String longitude = node.selectSingleNode(LONGITUDE).getText();
                location = new Location(Entry.addLocation(latitude, longitude), Entry.generateRandomUid(),DEFAULT_ZOOM);
                locationsList.add(location);
            }

            //get Entries
            //---------
            if(node.selectSingleNode(TITLE) != null ){
                 title = node.selectSingleNode(TITLE).getText();
            }
            if(node.selectSingleNode(CONTENT) != null ){
                String text = node.selectSingleNode(CONTENT).getText();
                // using Jsoup to parse HTML inside Content
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
            if(tagsList.size() != 0){
                for(Tags tag : tagsList) {
                    tags_uid += String.join(",", tag.tagsId)+ ",";
                }
                System.out.println(tags_uid);
                //clear the uid list later to prevent redundant results
                tags_uid="";
            }
            if(locationsList.size()!=0){
                Location location_name = locationsList.get(locationCounter);
                location_uid = location_name.location_uid;
            }
            entry = new Entry(formattedDate,parsedHtml,title,FOLDER_UID,location_uid,tags_uid);
            tagsForEachEntry.put(entry.uid,tagsList);
            entriesList.add(entry);
            //-----------
            locationCounter++;
            tagsList.clear();

            //get all the attachments
            if (node.selectSingleNode(RESOURCE) != null) {
                List<Node> attachmentsForEachEntry = node.selectNodes(RESOURCE);
                for (Node attachmentForeach : attachmentsForEachEntry) {
                    try {
                        mime = attachmentForeach.selectSingleNode(MIME).getText();


                    }catch( NullPointerException e){
                        e.printStackTrace();
                    }
                    fileName = fileNameList.get(fileNameCounter);
                    attachment_uid = Entry.generateRandomUid();
                    entry_uid = entry.uid;
                    //assign primary_photo uid
                    entry.primary_photo_uid = attachment_uid;
                    attachment = new Attachments(attachment_uid,entry_uid,type,fileName);
                    attachmentsList.add(attachment);
                    fileNameCounter++;
                }
            }
        }

    }

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
