package eu.bquepab.xyzreader.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Layout;
import android.text.StaticLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import eu.bquepab.xyzreader.R;
import eu.bquepab.xyzreader.data.ArticleLoader;
import eu.bquepab.xyzreader.utils.DateUtils;

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
    @BindView(R.id.article_author)
    TextView authorView;
    @Nullable
    @BindView(R.id.article_title)
    TextView titleView;
    @BindView(R.id.app_bar)
    AppBarLayout appBar;
    @BindString(R.string.action_share)
    String shareAction;

    private String bodyText;

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

        if (null != titleView) {
            ViewCompat.setNestedScrollingEnabled(bodyView, false);
        }

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

                DateUtils.createRelativeTimeSpanString(DateUtils.parsePublishedDate(cursor.getString(ArticleLoader.Query.PUBLISHED_DATE))) + " by "
                + cursor.getString(ArticleLoader.Query.AUTHOR))
                                  .toString();
        final String body = Html.fromHtml(cursor.getString(ArticleLoader.Query.BODY))
                                .toString();
        this.bodyText = body;

        final String photoUrl = cursor.getString(ArticleLoader.Query.PHOTO_URL);

        ArticleBodyListAdapter bodyAdapter = new ArticleBodyListAdapter(getContext(), body);
        bodyView.setAdapter(bodyAdapter);
        bodyView.setLayoutManager(new LinearLayoutManager(getContext()));

        Picasso.with(getContext())
               .load(photoUrl)
               .into(new Target() {
                   @Override
                   public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                       Palette.from(bitmap)
                              .generate(new Palette.PaletteAsyncListener() {
                                  @Override
                                  public void onGenerated(Palette palette) {
                                      Palette.Swatch textSwatch = palette.getDominantSwatch();
                                      if (null != textSwatch) {
                                          metaBar.setBackgroundColor(textSwatch.getRgb());
                                          authorView.setTextColor(textSwatch.getBodyTextColor());
                                          if (null == titleView) {
                                              collapsingToolbar.setExpandedTitleColor(textSwatch.getTitleTextColor());
                                              collapsingToolbar.setStatusBarScrimColor(palette.getDarkMutedColor(0xFFF));
                                          } else {
                                              titleView.setTextColor(textSwatch.getTitleTextColor());
                                          }
                                          collapsingToolbar.setCollapsedTitleTextColor(textSwatch.getTitleTextColor());
                                          collapsingToolbar.setContentScrimColor(textSwatch.getRgb());
                                      }
                                      if (null == titleView) {
                                          collapsingToolbar.setTitle(title);
                                      } else {
                                          titleView.setText(title);
                                          appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                                              boolean showing = true;
                                              int scrollRange = -1;

                                              @Override
                                              public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                                                  if (-1 == scrollRange) {
                                                      scrollRange = appBarLayout.getTotalScrollRange();
                                                  }
                                                  if ((scrollRange + verticalOffset) <= toolbar.getHeight()) {
                                                      collapsingToolbar.setTitle(title);
                                                      showing = true;
                                                  } else if (showing) {
                                                      collapsingToolbar.setTitle(null);
                                                      showing = false;
                                                  }
                                              }
                                          });
                                      }
                                      authorView.setText(author);

                                      photoView.setImageBitmap(bitmap);
                                  }
                              });
                   }

                   @Override
                   public void onBitmapFailed(Drawable errorDrawable) {

                   }

                   @Override
                   public void onPrepareLoad(Drawable placeHolderDrawable) {

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

    @OnClick(R.id.share_fab)
    public void onFabClicked() {
        startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                                                                    .setType("text/plain")
                                                                    .setText(bodyText)
                                                                    .getIntent(), shareAction));
    }

    private class ArticleBodyListAdapter extends RecyclerView.Adapter<ArticleBodyViewHolder> {

        StaticLayout sl;
        final TextView textView;
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
