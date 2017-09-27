package eu.bquepab.xyzreader.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import eu.bquepab.xyzreader.R;
import eu.bquepab.xyzreader.data.ArticleLoader;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_ITEM_ID = "item_id";

    private Unbinder unbinder;
    private long itemId;
    @BindView(R.id.photo)
    ImageView photoView;
    @BindView(R.id.article_body)
    RecyclerView bodyView;
    @BindView(R.id.share_fab)
    FloatingActionButton shareFab;
    @BindView(R.id.detail_toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.meta_bar)
    LinearLayout metaBar;
    @BindView(R.id.article_title)
    TextView titleView;
    @BindView(R.id.article_author)
    TextView authorView;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            itemId = getArguments().getLong(ARG_ITEM_ID);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_detail, container, false);
        unbinder = ButterKnife.bind(this, view);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), itemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursor == null || cursor.isClosed() || !cursor.moveToFirst()) {
            return;
        }

        final String title = cursor.getString(ArticleLoader.Query.TITLE);
        final String author = Html.fromHtml(
                DateUtils.getRelativeTimeSpanString(cursor.getLong(ArticleLoader.Query.PUBLISHED_DATE), System.currentTimeMillis(),
                                                    DateUtils.HOUR_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL)
                         .toString() + " by " + cursor.getString(ArticleLoader.Query.AUTHOR))
                                  .toString();
        final String body = Html.fromHtml(cursor.getString(ArticleLoader.Query.BODY))
                                .toString();

        ArticleBodyListAdapter bodyAdapter = new ArticleBodyListAdapter(getContext(), body);
        bodyView.setAdapter(bodyAdapter);
        bodyView.setLayoutManager(new LinearLayoutManager(getContext()));

        String photoUrl = cursor.getString(ArticleLoader.Query.PHOTO_URL);

        Picasso.with(getContext())
               .load(photoUrl)
               .into(photoView, new Callback() {
                   @Override
                   public void onSuccess() {
                       Bitmap bitmap = ((BitmapDrawable) photoView.getDrawable()).getBitmap();
                       Palette.from(bitmap)
                              .generate(new Palette.PaletteAsyncListener() {
                                  @Override
                                  public void onGenerated(Palette palette) {
                                      Palette.Swatch textSwatch = palette.getDominantSwatch();
                                      if (null != textSwatch) {
                                          metaBar.setBackgroundColor(textSwatch.getRgb());
                                          titleView.setTextColor(textSwatch.getTitleTextColor());
                                          authorView.setTextColor(textSwatch.getBodyTextColor());
                                          collapsingToolbar.setExpandedTitleColor(textSwatch.getTitleTextColor());
                                          collapsingToolbar.setContentScrimColor(textSwatch.getRgb());
                                      }
                                      collapsingToolbar.setTitle(title);
                                      titleView.setText(title);
                                      authorView.setText(author);
                                  }
                              });
                   }

                   @Override
                   public void onError() {
                   }
               });

        shareFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                                                                            .setType("text/plain")
                                                                            .setText(body)
                                                                            .getIntent(), getString(R.string.action_share)));
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private class ArticleBodyListAdapter extends RecyclerView.Adapter<ArticleBodyViewHolder> {

        StaticLayout sl;
        TextView textView;
        final String text;
        int itemCount;
        private static final int maxLines = 20;

        public ArticleBodyListAdapter(Context context, String text) {
            this.text = text;
            textView = new TextView(context);
            textView.setText(text);

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    sl = new StaticLayout(ArticleBodyListAdapter.this.text, textView.getPaint(), ArticleDetailFragment.this.bodyView.getWidth(),
                                          Layout.Alignment.ALIGN_NORMAL, 1, 0, true);

                    int lineCount = sl.getLineCount();
                    itemCount = lineCount / maxLines;
                    if (lineCount % maxLines > 0) {
                        itemCount++;
                    }
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public ArticleBodyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.list_item_article_body, parent, false);
            return new ArticleBodyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ArticleBodyViewHolder holder, int position) {
            int startChar = sl.getLineStart(position * maxLines);
            int endChar;
            if ((position + 1) * maxLines >= sl.getLineCount()) {
                endChar = text.length();
            } else {
                endChar = sl.getLineStart((position + 1) * maxLines);
            }
            holder.bind(text.substring(startChar, endChar));
        }

        @Override
        public int getItemCount() {
            return itemCount;
        }
    }

    class ArticleBodyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.article_body)
        TextView bodyView;

        public ArticleBodyViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        public void bind(String body) {
            bodyView.setText(body);
        }
    }
}
