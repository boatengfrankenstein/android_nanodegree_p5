package eu.bquepab.xyzreader.ui;

import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import eu.bquepab.xyzreader.R;
import eu.bquepab.xyzreader.data.ArticleLoader;
import eu.bquepab.xyzreader.data.ItemsContract;
import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Cursor cursor;
    private long startId;

    @BindView(R.id.pager)
    ViewPager pager;
    private CustomPagerAdapter pagerAdapter;

    @BindColor(R.color.page_margin_drawable)
    int colorPageMarginDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView()
                       .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);

        ButterKnife.bind(this);

        getSupportLoaderManager().initLoader(0, null, this);

        pagerAdapter = new CustomPagerAdapter(getSupportFragmentManager());

        pager.setAdapter(pagerAdapter);
        pager.setPageMargin((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        pager.setPageMarginDrawable(new ColorDrawable(colorPageMarginDrawable));
        pager.setPageTransformer(true, new ParallaxPageTransformer());

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                startId = ItemsContract.Items.getItemId(getIntent().getData());
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        this.cursor = cursor;
        pagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (startId > 0) {
            this.cursor.moveToFirst();
            // TODO: optimize
            while (!this.cursor.isAfterLast()) {
                if (this.cursor.getLong(ArticleLoader.Query._ID) == startId) {
                    final int position = this.cursor.getPosition();
                    pager.setCurrentItem(position, false);
                    break;
                }
                this.cursor.moveToNext();
            }
            startId = 0;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        cursor = null;
        pagerAdapter.notifyDataSetChanged();
    }

    private class CustomPagerAdapter extends FragmentStatePagerAdapter {
        public CustomPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            cursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(cursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public int getCount() {
            return (cursor != null) ? cursor.getCount() : 0;
        }
    }

    /**
     * Base solution from:
     * https://medium.com/@BashaChris/the-android-viewpager-has-become-a-fairly-popular-component-among-android-apps-its-simple-6bca403b16d4
     */
    public class ParallaxPageTransformer implements ViewPager.PageTransformer {

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(1);
            } else if (position <= 1) { // [-1,1]
                List<View> views = getAllViews(view);
                for (int i = 0; i < views.size(); i++) {
                    View child = views.get(i);
                    if (child.getId() == R.id.photo) {
                        child.setTranslationX(-position * (pageWidth / 2)); //Half the normal speed
                        break;
                    }
                }
            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(1);
            }
        }

        private List<View> getAllViews(View v) {
            if (!(v instanceof ViewGroup) || ((ViewGroup) v).getChildCount() == 0) { // It's a leaf
                List<View> r = new ArrayList<>();
                r.add(v);
                return r;
            } else {
                List<View> list = new ArrayList<>();
                list.add(v); // If it's an internal node add itself
                int children = ((ViewGroup) v).getChildCount();
                for (int i = 0; i < children; ++i) {
                    list.addAll(getAllViews(((ViewGroup) v).getChildAt(i)));
                }
                return list;
            }
        }
    }
}
