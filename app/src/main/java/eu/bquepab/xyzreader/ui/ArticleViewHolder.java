package eu.bquepab.xyzreader.ui;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import eu.bquepab.xyzreader.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class ArticleViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.thumbnail)
    ImageView thumbnailView;
    @BindView(R.id.article_title)
    TextView titleView;
    @BindView(R.id.article_subtitle)
    TextView subtitleView;

    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    private long itemId;
    private ArticleListAdapter.OnItemClickListener clickListener;

    public ArticleViewHolder(View view, ArticleListAdapter.OnItemClickListener clickListener) {
        super(view);

        this.clickListener = clickListener;

        ButterKnife.bind(this, view);
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

        Picasso.with(thumbnailView.getContext())
               .load(thumbUrl)
               .into(thumbnailView, new Callback() {
                   @Override
                   public void onSuccess() {
                       Bitmap bitmap = ((BitmapDrawable) thumbnailView.getDrawable()).getBitmap();
                       Palette.from(bitmap)
                              .generate(new Palette.PaletteAsyncListener() {
                                  @Override
                                  public void onGenerated(Palette palette) {
                                      Palette.Swatch textSwatch = palette.getDominantSwatch();
                                      itemView.setBackgroundColor(textSwatch.getRgb());
                                      titleView.setTextColor(textSwatch.getTitleTextColor());
                                      subtitleView.setTextColor(textSwatch.getBodyTextColor());
                                  }
                              });
                   }

                   @Override
                   public void onError() {

                   }
               });
    }

    @OnClick(R.id.article_card)
    public void onClick() {
        clickListener.onClickedItem(itemId);
    }
}
