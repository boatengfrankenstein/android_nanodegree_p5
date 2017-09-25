package eu.bquepab.xyzreader.ui;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;
import eu.bquepab.xyzreader.R;
import eu.bquepab.xyzreader.data.ItemsContract;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class ArticleViewHolder extends RecyclerView.ViewHolder {
    DynamicHeightNetworkImageView thumbnailView;
    TextView titleView;
    TextView subtitleView;

    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    private long itemId;

    public ArticleViewHolder(View view) {
        super(view);
        thumbnailView = (DynamicHeightNetworkImageView) view.findViewById(R.id.thumbnail);
        titleView = (TextView) view.findViewById(R.id.article_title);
        subtitleView = (TextView) view.findViewById(R.id.article_subtitle);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext()
                 .startActivity(new Intent(Intent.ACTION_VIEW, ItemsContract.Items.buildItemUri(itemId)));
            }
        });
    }

    public void bind(String title, Date publishedDate, String author, String thumbUrl, float aspectRatio, long itemId) {
        this.itemId = itemId;
        titleView.setText(title);

        if (!publishedDate.before(START_OF_EPOCH.getTime())) {

            subtitleView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(publishedDate.getTime(), System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                                        DateUtils.FORMAT_ABBREV_ALL)
                             .toString() + "<br/>" + " by " + author));
        } else {
            subtitleView.setText(Html.fromHtml(outputFormat.format(publishedDate) + "<br/>" + " by " + author));
        }

        thumbnailView.setImageUrl(thumbUrl, ImageLoaderHelper.getInstance(thumbnailView.getContext())
                                                             .getImageLoader());
        thumbnailView.setAspectRatio(aspectRatio);
    }
}
