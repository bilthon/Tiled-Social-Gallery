package com.spiral.gallery;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class DetailActivity extends AbsDynamicLoaderActivity {
	private String TAG = "DetailActivity";
	private ProgressDialog mProgress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		ImageView image = (ImageView) findViewById(R.id.Detail);
		String uri = getIntent().getExtras().getString(AlbumAdapter.URI_BIG);
		this.imageLoader.displayImage(uri, image, new ImageLoadingListener(){


			@Override
			public void onLoadingStarted(String imageUri, View view) {
				mProgress = new ProgressDialog(DetailActivity.this);
				mProgress.setIndeterminate(true);
				mProgress.setTitle(getResources().getString(R.string.download_dialog_title));
				mProgress.show();
			}

			@Override
			public void onLoadingFailed(String imageUri, View view,
					FailReason failReason) {
				mProgress.dismiss();
			}

			@Override
			public void onLoadingComplete(String imageUri, View view,
					Bitmap loadedImage) {
				mProgress.dismiss();
			}

			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				mProgress.dismiss();
			}
		});
	}
}
