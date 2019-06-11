public class Attachment {
    String uid;
    String entry_uid;
    String type;
    String filename;
    byte[] data;

    public Attachment(String uid, String entry_uid, String type, String filename , byte[] data) {
        this.uid = uid;
        this.entry_uid = entry_uid;
        this.type = type;
        this.filename = filename;
        this.data = data;
    }


}
