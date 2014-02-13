package com.spiral.gallery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

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
	
	public AlbumAdapter(Context context, ImageLoader l){
		mContext = context;
		mImageLoader = l;
	}
	
	public AlbumAdapter(Context context, ImageLoader l, JSONArray array){
		mContext = context;
		mImageLoader = l;
		mArray = array;
	}
	
	public void setData(JSONArray array){
		mArray = array;
	}
	
	private void setupOptions(){
	    /* Options specify different properties of the dynamic image loading procedure */
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.ic_stub)
		.showImageForEmptyUri(R.drawable.ic_empty)
		.showImageOnFail(R.drawable.ic_error)
		.cacheInMemory(true)
		.cacheOnDisc(true)
//		.considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.build();
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
		try {
			JSONObject element;
			element = (JSONObject) mArray.get(position);
			String uri = element.getString(URI_THUMBNAIL);
			if(mImageLoader.getMemoryCache().get(uri) != null){
				Log.d(TAG, "Getting image from cache");
				imageView.setImageBitmap(mImageLoader.getMemoryCache().get(uri));
			}else{
				mImageLoader.displayImage(uri, imageView, options);
				Log.d(TAG,"Getting image from network. Mem cache has size: "+mImageLoader.getMemoryCache().keys().size());
			}
		} catch (JSONException e) {
			Log.e(TAG,"JSONException at calling the get method on an JSONArray. Msg: "+e.getMessage());
		}
		return imageView;
	}
}
