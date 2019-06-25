package diaro;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImportStringUtils {
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
     * @param firstAdditional additional string
     * @param secondAdditional additional string
     * @return the newString
     */
    public static String generateNewFileName(String fileNameString, String firstAdditional, String secondAdditional) {
        String extension = FilenameUtils.getExtension(fileNameString);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        return timeStamp + firstAdditional + secondAdditional + "." + extension;
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
     * @param title entry title
     * @return String array of formatted tile,text
     */
    public static String[] formatTextAndTitle(String text , String title){
        //if EOL is found inside the first 100 chars
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
}
