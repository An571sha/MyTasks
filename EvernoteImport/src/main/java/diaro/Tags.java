package diaro;

import org.dom4j.Element;

import java.util.Map;

public class Tags {

    public String tagsId;
    public String title;

    public Tags(String tagsId, String title) {
        this.tagsId = tagsId;
        this.title = title;
    }

    public static void generateTagTable(Map.Entry<String,String> tag, Element tagRow ){
        tagRow.addElement(Entry.KEY_UID).addText(tag.getValue());
        tagRow.addElement(Entry.KEY_ENTRY_TAGS_TITLE).addText(tag.getKey());
    }

}
