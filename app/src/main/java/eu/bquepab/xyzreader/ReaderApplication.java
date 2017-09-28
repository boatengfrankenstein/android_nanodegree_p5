package eu.bquepab.xyzreader;

import android.app.Application;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.picasso.Picasso;
import timber.log.Timber;

public class ReaderApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        // Normal app init code...

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        Picasso.Builder picassoBuilder = new Picasso.Builder(this);
        picassoBuilder.indicatorsEnabled(BuildConfig.DEBUG)
                      .loggingEnabled(BuildConfig.DEBUG);
        Picasso.setSingletonInstance(picassoBuilder.build());
    }
}
