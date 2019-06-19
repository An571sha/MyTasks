package diaro;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.List;
import java.util.Map;

public class XmlGenerator {

    //xml table names
    private static String diaro_folders = "diaro_folders";
    private static String diaro_entries = "diaro_entries";
    private static String diaro_attachments = "diaro_attachments";
    private static String diaro_tags = "diaro_tags";
    private static String diaro_locations = "diaro_locations";

    public static Document generateXmlForDiaro(String FOLDER_UID,String FOLDER_TITLE, String FOLDER_COLOR,
                                                Map<String,String> uidForEachTag,
                                                List<Location> locationsList,
                                                List<Entry> entriesList,
                                                List<Attachment> attachmentList
                                                ){

        Document createdXmlDocument = DocumentHelper.createDocument();
        //create the root element of the document
        Element root = createdXmlDocument.addElement("data").addAttribute("version", "2");

        //adding folders table
        // generate folder table. All the folders have a constant
        // uid, title and colour. As evernote does not provide any data regarding folders.
        Element folderRoot = root.addElement("table").addAttribute("name", diaro_folders);
        Element folderRow =  folderRoot.addElement("r");
        folderRow.addElement(Entry.KEY_UID).addText(FOLDER_UID);
        folderRow.addElement(Entry.KEY_ENTRY_FOLDER_TITLE).addText(FOLDER_TITLE);
        folderRow.addElement(Entry.KEY_ENTRY_FOLDER_COLOR).addText(FOLDER_COLOR);

        //adding tags table
        if(uidForEachTag.entrySet().size() >0) {
            Element tagRoot = root.addElement("table").addAttribute("name", diaro_tags);
            for (Map.Entry<String, String> tag : uidForEachTag.entrySet()) {
                assert tagRoot != null;
                Element tagRow = tagRoot.addElement("r");
                Tags.generateTagTable(tag, tagRow);
            }
        }
        //adding location table
        if(locationsList.size() > 0) {
            Element locationRoot = root.addElement("table").addAttribute("name", diaro_locations);
            for (Location location : locationsList) {

                assert locationRoot != null;
                Element locationRow = locationRoot.addElement("r");
                Location.generateLocationTable(location, locationRow);
            }
        }

        //adding zipEntries table
        if(entriesList.size() > 0 ) {
            Element entryRoot = root.addElement("table").addAttribute("name", diaro_entries);
            for (Entry entry : entriesList) {
                Element entriesRow = entryRoot.addElement("r");
                Entry.generateEntryTable(entry, entriesRow, FOLDER_UID);
            }
        }
        //adding attachments table
        if(attachmentList.size() > 0) {
            Element attachmentsRoot = root.addElement("table").addAttribute("name", diaro_attachments);
            for (Attachment attachment : attachmentList) {

                assert attachmentsRoot != null;
                Element attachmentRow = attachmentsRoot.addElement("r");
                Attachment.generateAttachmentTable(attachment, attachmentRow);

            }
        }
        return createdXmlDocument;

    }
}
