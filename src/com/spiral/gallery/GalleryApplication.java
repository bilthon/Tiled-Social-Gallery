package com.spiral.gallery;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import android.app.Application;
import android.graphics.Bitmap.CompressFormat;

public class GalleryApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Create global configuration and initialize ImageLoader with this configuration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
        	.memoryCache(new LruMemoryCache(2 * 1024 * 1024))
        	.discCacheSize(50 * 1024 * 1024)
        	.discCacheExtraOptions(480, 800, CompressFormat.JPEG, 75, null)
            .build();
        ImageLoader.getInstance().init(config);
    }
}
