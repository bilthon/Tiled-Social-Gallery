package com.spiral.gallery;

import android.os.Bundle;
import android.widget.ImageView;

public class DetailActivity extends AbsDynamicLoaderActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		ImageView image = (ImageView) findViewById(R.id.Detail);
		String uri = getIntent().getExtras().getString(AlbumAdapter.URI_BIG);
		this.imageLoader.displayImage(uri, image, options);
	}
}
