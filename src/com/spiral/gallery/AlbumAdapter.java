package com.spiral.gallery;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.widget.StaggeredGridView.LayoutParams;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**
 * This adapter will provide the views the GridView needs.
 * It's data source is a JSONArray where each element of that array is an object that 
 * contains 3 elements:
 * <p>
 * <ul>
 * <li>src_small</li>
 * <li>src_big</li>
 * <li>caption</li>
 * <ul>
 * <p>
 * @author Nelson R. Pérez
 *
 */
public class AlbumAdapter extends BaseAdapter {
	private static final String TAG = "AlbumAdapter";
	
	/* Fields of every JSONObject contained in the JSONArray data */
	public static final String URI_THUMBNAIL = "src_small";
	public static final String URI_BIG = "src_big";
	public static final String URI_CAPTION = "caption";
	
	/* Context needed to get hold of system services */
	private Context mContext;
	
	/* The actual data source, containing all url for the images */
	private JSONArray mArray;
	
	/* Image loader */
	private ImageLoader mImageLoader;
	
	/* Customized options for the image loading procedure */
	private DisplayImageOptions options;
	
	/*
	 * This hashmap will hold a table that will be build to specifically select
	 * entries that should be expanded into 3 columns.
	 * 
	 * This translates to entries at indexes 9, 19, 29, 39, 49..
	 * 
	 * Assuming a maximum of 10000 photos, we'll fill this table once and then just
	 * consult it to know whether to expand or not a child view.
	 */
	private HashMap<Integer,Boolean> checkTable;
	private final int MAX_PHOTOS = 10000;
	
	/* Variable that will hold the density value of the screen.
	 * We will use this value to decide whether to display a small or a big
	 * image in the gallery. Since displaying a high resolution image on a low 
	 * density screen is a waste of resources, we prefer to allow only HDPI screens
	 * and above to display high resolution images in the grid */
	private float mCurrentDensity = 1;
	
	public AlbumAdapter(Context context, ImageLoader l){
		mContext = context;
		mImageLoader = l;
		setupAdapter();
	}
	
	public AlbumAdapter(Context context, ImageLoader l, JSONArray array){
		mContext = context;
		mImageLoader = l;
		mArray = array;
		setupAdapter();
	}
	
	public void setData(JSONArray array){
		mArray = array;
	}
	
	private void setupAdapter(){
	    /* Options specify different properties of the dynamic image loading procedure */
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.ic_stub)
		.showImageForEmptyUri(R.drawable.ic_empty)
		.showImageOnFail(R.drawable.ic_error)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
		
		/* Fill in the lookup table used to check which image to expand */
		checkTable = new HashMap<Integer, Boolean>();
		long before = System.currentTimeMillis();
		for(int i = 0; i < MAX_PHOTOS; i++)
			if((i + 1) % 10 == 0)
				checkTable.put(i, true);
		long after = System.currentTimeMillis();
		Log.d(TAG,"Filling the table took me: "+(after-before)+" ms");
		
		/* Saving density information in an instance variable for later use */
		Log.d(TAG,"Density: "+mContext.getResources().getDisplayMetrics().density);
		mCurrentDensity = mContext.getResources().getDisplayMetrics().density;
	}

	@Override
	public int getCount() {
		if(mArray == null)
			return 0;
		else
			return mArray.length();
	}

	@Override
	public Object getItem(int position) {
		Object element = null;
		try {
			element = mArray.get(position);
		} catch (JSONException e) {
			Log.e(TAG,"JSONException. Msg: "+e.getMessage());
		} catch(NullPointerException e){
			Log.e(TAG,"NullPointerException. Msg: "+e.getMessage());
		}
		return element;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			imageView = (ImageView) inflater.inflate(R.layout.item_grid_image, parent, false);
		}else{
			imageView = (ImageView) convertView;
			imageView.setImageResource(R.drawable.ic_empty);
		}
		
		final LayoutParams lp;
        lp = new LayoutParams(imageView.getLayoutParams());
		if(checkTable.get(Integer.valueOf(position)) != null){
			Log.d(TAG,"Extening view at position "+position);
            lp.span = 3;
            lp.height = (int) mContext.getResources().getDimension(R.dimen.grid_item_size_big);
		}else{
			lp.span = 1;
			lp.height = (int) mContext.getResources().getDimension(R.dimen.grid_item_size_small);
		}
        imageView.setLayoutParams(lp);
        imageView.setScaleType(ScaleType.CENTER_CROP);
		
		try {
			JSONObject element;
			element = (JSONObject) mArray.get(position);
			String uri = null;
			if(mCurrentDensity <= 1)
				uri = element.getString(URI_THUMBNAIL);
			else
				uri = element.getString(URI_BIG);
			mImageLoader.displayImage(uri, imageView, options);
		} catch (JSONException e) {
			Log.e(TAG,"JSONException at calling the get method on an JSONArray. Msg: "+e.getMessage());
		}
		return imageView;
	}
}
