package diaro;

import org.dom4j.Element;

public class Attachment {
    public String uid;
    public String entry_uid;
    public String type;
    public String filename;
    public  byte[] data;

    //Evernote
    public Attachment(String uid, String entry_uid, String type, String filename , byte[] data) {
        this.uid = uid;
        this.entry_uid = entry_uid;
        this.type = type;
        this.filename = filename;
        this.data = data;
    }
    //Journey
    public Attachment(String uid, String entry_uid, String type, String filename) {
        this.uid = uid;
        this.entry_uid = entry_uid;
        this.type = type;
        this.filename = filename;
    }

    public static void generateAttachmentTable(Attachment attachment , Element row ) {
        row.addElement(Entry.KEY_UID).addText(attachment.uid);
        row.addElement(Entry.KEY_ENTRY_UID).addText(attachment.entry_uid);
        row.addElement("type").addText("photo");
        row.addElement(Entry.KEY_ENTRY_FILENAME).addText(attachment.filename);
    }



}
