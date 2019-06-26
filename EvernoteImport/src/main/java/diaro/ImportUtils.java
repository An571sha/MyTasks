package diaro;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.dom4j.Document;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ImportUtils {
    /**
     * @param date input Date from import
     * @param dateFormat date format for imports
     * @return boolean
     */
    public static boolean checkDateFormat(String date, String dateFormat)
    {
        SimpleDateFormat df1=new SimpleDateFormat(dateFormat);
        System.out.println(df1.toPattern());
        df1.setLenient(false);
        try {
            df1.parse(date);
        } catch (ParseException e) {

            return false;
        }
        return true;
    }

    public static String concatLatLng(String lat, String lng){
        return String.format("(%s , %s)", lat, lng);
    }

    public static long formatDateToTimeStamp(String date, String format){
        DateTimeFormatter formatter = DateTimeFormat.forPattern(format);
        DateTime dt = formatter.parseDateTime(date);
        DateTime dtNew = dt.plusHours(18);
        return dtNew.getMillis();
    }

    /** creates a new FileName
     * @param fileNameString name of the file in attachment
     * @return the newString
     */
    public static String generateNewFileName(String fileNameString, String... additionals) {
        String extension = FilenameUtils.getExtension(fileNameString);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        return timeStamp + String.join("",additionals) + "." + extension;
    }

    public static String generateNewFileName(String fileNameString) {
      return generateNewFileName(fileNameString,"","");
    }

    /** <p>checks if the jsonObject is null
     * or if it is a String, is it empty or null
     *
     *  @param rootJsonObject rootJsonObject
     * @param key key for rootJsonObject
     * @return boolean
     */
    public static boolean isNullOrEmpty(JSONObject rootJsonObject, String key){
        if (rootJsonObject.isNull(key)) {
            return true;
        }

        if (rootJsonObject.get(key) instanceof String) {
            //check if rootJsonObject is a String
            return rootJsonObject.getString(key).isEmpty();

        }
        return false;
    }

    /**
     * @param text entry text
     * @return String array of formatted tile,text
     */
    public static String[] formatDayOneTextAndTitle(String text){
        //if EOL is found inside the first 100 chars
        String title;
        if (text.contains("\n") && text.indexOf("\n") <= 100) {

            title = StringUtils.substring(text,0,text.indexOf("\n"));
            text = StringUtils.substring(text,text.indexOf("\n")+1);
            return new String[]{title,text};

        }else {
            //if text is smaller than 100 chars with no EOL within 100 chars
            if (text.length() < 100) {
                title = text;
                text = "";
                return new String[]{title,text};
            } else {
                title = "";
                return new String[]{title,text};
            }
        }
    }

    public static boolean checkPhotosExtension(ZipEntry zipEntry){
        return zipEntry.getName().endsWith(".png") || zipEntry.getName().endsWith(".jpg") || zipEntry.getName().endsWith(".gif") || zipEntry.getName().endsWith(".jpeg");
    }

    public static void writeXmlAndImagesToZip(Document createdDocument, String inputZipFilePath , String outputZipFilePath , HashMap attachmentsWithNewName) throws IOException,SecurityException {
        StopWatch stopwatch = new StopWatch();
        stopwatch.start();
        //creating new zipOutputStream
        ZipOutputStream append = new ZipOutputStream(new FileOutputStream(outputZipFilePath));
        //increase the buffer as required
        byte[] buffer = new byte[512];
        ZipFile journeyZipFile = new ZipFile(inputZipFilePath);
        Enumeration<? extends ZipEntry> zipEntries = journeyZipFile.entries();
        System.out.println("start appending");
        //loop through the entries
        while (zipEntries.hasMoreElements()) {
            ZipEntry newEntry;
            ZipEntry entry = zipEntries.nextElement();
            //creating entryName String object to get the filename without path
            String entryName = new File(entry.getName()).getName();

            //get the attachment name from list
            if(attachmentsWithNewName.containsKey(entryName)) {
                //create a new entry with the new attachment name
                newEntry = new ZipEntry("media/photo/" + attachmentsWithNewName.get(entryName));
            }else{

                newEntry = new ZipEntry("media/photo/" + entryName);
            }
            //checking for compatible attachments
            if(entry.getName().endsWith("jpg") || entry.getName().endsWith("png") || entry.getName().endsWith("gif") || entry.getName().endsWith("jpeg")) {
                System.out.println("append: " + newEntry);
                try {
                    //copying the attachments from old zip to new zip
                    append.putNextEntry(newEntry);
                    InputStream in = journeyZipFile.getInputStream(entry);
                    while (0 < in.available()) {
                        int read = in.read(buffer);
                        if (read > 0) {
                            append.write(buffer, 0, read);
                        }
                    }
                }catch (ZipException e){
                    e.printStackTrace();
                }
            }
        }
        append.closeEntry();

        // now append xml
        ZipEntry diaroZipFile = new ZipEntry("DiaroBackup.xml");
        System.out.println("append: " + diaroZipFile.getName());
        append.putNextEntry(diaroZipFile);
        append.write(Entry.toBytes(createdDocument));
        append.closeEntry();

        // close
        append.close();
        System.out.println("end appending");
        journeyZipFile.close();
        stopwatch.stop();
        System.out.println("Time taken on main Thread: " + stopwatch.getTime(TimeUnit.MILLISECONDS));

    }

}
