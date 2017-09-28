package eu.bquepab.xyzreader.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import timber.log.Timber;

public class DateUtils {

    private DateUtils() {
        //Avoid instances
    }

    public static Date parsePublishedDate(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Timber.e(ex);
            Timber.i("passing today's date");
            return new Date();
        }
    }
}
