package com.spiral.gallery;

import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.StaggeredGridView;
import android.widget.AbsListView;

public abstract class AbsGridActivity extends AbsDynamicLoaderActivity {
	protected static final String STATE_PAUSE_ON_SCROLL = "STATE_PAUSE_ON_SCROLL";
	protected static final String STATE_PAUSE_ON_FLING = "STATE_PAUSE_ON_FLING";
	protected StaggeredGridView listView;

	protected boolean pauseOnScroll = false;
	protected boolean pauseOnFling = true;

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		pauseOnScroll = savedInstanceState.getBoolean(STATE_PAUSE_ON_SCROLL,
				false);
		pauseOnFling = savedInstanceState
				.getBoolean(STATE_PAUSE_ON_FLING, true);
	}

	@Override
	public void onResume() {
		super.onResume();
//		applyScrollListener();
	}

//	private void applyScrollListener() {
//		listView.setOnScrollListener(new PauseOnScrollListener(imageLoader,
//				pauseOnScroll, pauseOnFling));
//	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(STATE_PAUSE_ON_SCROLL, pauseOnScroll);
		outState.putBoolean(STATE_PAUSE_ON_FLING, pauseOnFling);
	}

}
