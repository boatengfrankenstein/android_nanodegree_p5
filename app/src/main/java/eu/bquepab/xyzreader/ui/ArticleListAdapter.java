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

    public interface OnItemClickListener {
        void onClickedItem(long itemId);
    }

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    private Cursor cursor;
    private OnItemClickListener clickListener;

    public ArticleListAdapter(Cursor cursor, OnItemClickListener clickListener) {
        this.cursor = cursor;
        this.clickListener = clickListener;
    }

    @Override
    public long getItemId(int position) {
        cursor.moveToPosition(position);
        return cursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public ArticleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.list_item_article, parent, false);
        return new ArticleViewHolder(view, clickListener);
    }

    private Date parsePublishedDate() {
        try {
            String date = cursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Timber.e(ex);
            Timber.i("passing today's date");
            return new Date();
        }
    }

    @Override
    public void onBindViewHolder(ArticleViewHolder holder, int position) {
        cursor.moveToPosition(position);
        String title = cursor.getString(ArticleLoader.Query.TITLE);
        String author = cursor.getString(ArticleLoader.Query.AUTHOR);
        Date publishedDate = parsePublishedDate();
        String thumbUrl = cursor.getString(ArticleLoader.Query.THUMB_URL);
        long itemId = getItemId(position);

        holder.bind(title, publishedDate, author, thumbUrl, itemId);
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }
}
