package eu.bquepab.xyzreader.ui;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import eu.bquepab.xyzreader.R;
import eu.bquepab.xyzreader.data.ArticleLoader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import timber.log.Timber;

public class ArticleListAdapter extends RecyclerView.Adapter<ArticleViewHolder> {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");

    private Cursor mCursor;

    public ArticleListAdapter(Cursor cursor) {
        mCursor = cursor;
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public ArticleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.list_item_article, parent, false);
        return new ArticleViewHolder(view);
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Timber.e(ex);
            Timber.i("passing today's date");
            return new Date();
        }
    }

    @Override
    public void onBindViewHolder(ArticleViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String title = mCursor.getString(ArticleLoader.Query.TITLE);
        String author = mCursor.getString(ArticleLoader.Query.AUTHOR);
        Date publishedDate = parsePublishedDate();
        String thumbUrl = mCursor.getString(ArticleLoader.Query.THUMB_URL);
        float aspectRatio = mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO);
        long itemId = getItemId(position);

        holder.bind(title, publishedDate, author, thumbUrl, aspectRatio, itemId);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }
}
