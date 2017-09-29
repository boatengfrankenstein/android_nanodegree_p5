package eu.bquepab.xyzreader.remote;

import java.net.MalformedURLException;
import java.net.URL;
import timber.log.Timber;

public class Config {
    public static final URL BASE_URL;

    static {
        URL url = null;
        try {
            //Own copy of mocked data
            url = new URL(
                    "https://gist.githubusercontent.com/niltsiar/b7073b2c177543145343fd47b5062e41/raw/6cb374308058ab74db3a42cdee2278388e53efd2/xyz-reader.json");
        } catch (MalformedURLException ignored) {
            // TODO: throw a real error
            Timber.e("Please check your internet connection.");
        }

        BASE_URL = url;
    }
}
