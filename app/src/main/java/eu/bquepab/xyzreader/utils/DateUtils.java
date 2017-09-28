package eu.bquepab.xyzreader.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
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

    public static String createRelativeTimeSpanString(Date date) {
        SimpleDateFormat outputFormat = new SimpleDateFormat();
        GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

        String relativeTimeSpan;

        if (!date.before(START_OF_EPOCH.getTime())) {
            relativeTimeSpan = android.text.format.DateUtils.getRelativeTimeSpanString(date.getTime(), System.currentTimeMillis(),
                                                                                       android.text.format.DateUtils.HOUR_IN_MILLIS,
                                                                                       android.text.format.DateUtils.FORMAT_ABBREV_ALL)
                                                            .toString();
        } else {
            relativeTimeSpan = outputFormat.format(date);
        }

        return relativeTimeSpan;
    }
}
