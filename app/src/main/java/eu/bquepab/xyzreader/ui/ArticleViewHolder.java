package eu.bquepab.xyzreader.ui;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import eu.bquepab.xyzreader.R;
import eu.bquepab.xyzreader.utils.DateUtils;
import java.util.Date;

public class ArticleViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.thumbnail)
    ImageView thumbnailView;
    @BindView(R.id.article_title)
    TextView titleView;
    @BindView(R.id.article_subtitle)
    TextView subtitleView;

    private long itemId;
    private final ArticleListAdapter.OnItemClickListener clickListener;

    public ArticleViewHolder(View view, ArticleListAdapter.OnItemClickListener clickListener) {
        super(view);

        this.clickListener = clickListener;

        ButterKnife.bind(this, view);
    }

    public void bind(final String title, final Date publishedDate, final String author, final String thumbUrl, long itemId) {
        this.itemId = itemId;

        Picasso.with(thumbnailView.getContext())
               .load(thumbUrl)
               .into(new Target() {
                   @Override
                   public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                       Palette.from(bitmap)
                              .generate(new Palette.PaletteAsyncListener() {
                                  @Override
                                  public void onGenerated(Palette palette) {
                                      Palette.Swatch textSwatch = palette.getDominantSwatch();
                                      if (null != textSwatch) {
                                          itemView.setBackgroundColor(textSwatch.getRgb());
                                          titleView.setTextColor(textSwatch.getTitleTextColor());
                                          subtitleView.setTextColor(textSwatch.getBodyTextColor());
                                      }
                                      setText(title, publishedDate, author);
                                  }
                              });
                       thumbnailView.setImageBitmap(bitmap);
                   }

                   @Override
                   public void onBitmapFailed(Drawable errorDrawable) {

                   }

                   @Override
                   public void onPrepareLoad(Drawable placeHolderDrawable) {

                   }
               });

    }

    private void setText(String title, Date publishedDate, String author) {
        titleView.setText(title);
        subtitleView.setText(Html.fromHtml(DateUtils.createRelativeTimeSpanString(publishedDate) + "<br/>" + " by " + author));
    }

    @OnClick(R.id.article_card)
    public void onClick() {
        clickListener.onClickedItem(itemId);
    }
}
