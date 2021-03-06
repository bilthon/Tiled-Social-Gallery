package com.spiral.gallery;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.support.v4.widget.StaggeredGridView;
import android.support.v4.widget.StaggeredGridView.OnItemClickListener;

/**
 * MainActivity that will intially just display a login button at the base of the screen.
 * <p>
 * After the user has logged in and given the app permissions, a gallery will be populated
 * with all of the user's facebook photos.
 * <p>
 * @author Nelson R. Pérez
 * @version 1.0.0
 *
 */
public class MainActivity extends AbsGridActivity implements Request.Callback, OnItemClickListener {
	private static final String TAG = "MainFragment";
	private UiLifecycleHelper uiHelper;
	
	/**
	 * Callback called whenever there is a session change
	 */
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG,"onCreate");
		uiHelper = new UiLifecycleHelper(this, callback);
	    uiHelper.onCreate(savedInstanceState);
	    
	    LoginButton authButton = (LoginButton) findViewById(R.id.fb_btn);
	    authButton.setReadPermissions(Arrays.asList("user_likes", "user_status"));

		listView = (StaggeredGridView) findViewById(R.id.grid);
		listView.setColumnCount(3);
		listView.setOnItemClickListener(this);		
	}
	
	@Override
	public void onItemClick(ViewGroup parent, View view, int position) {
		Log.d(TAG,"onItemClick. position: "+position);
		StaggeredGridView gridView = (StaggeredGridView) parent;
		JSONObject element = (JSONObject) gridView.getAdapter().getItem(position);
		Intent intent = new Intent(this, DetailActivity.class);
		String uri;
		try {
			uri = element.getString(AlbumAdapter.URI_BIG);
			intent.putExtra(AlbumAdapter.URI_BIG, uri);
			startActivity(intent);
		} catch (JSONException e) {
			Log.e(TAG,"JSONException. Msg: "+e.getMessage());
		}
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    uiHelper.onResume();
	}
	
	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	    
	    /* If we have data in the adapter, lets save it */
	    AlbumAdapter adapter = (AlbumAdapter) this.listView.getAdapter();
	    if(adapter != null){
	    	JSONArray data = adapter.getData();
	    	outState.putString(AlbumAdapter.ADAPTER_DATA_KEY, data.toString());
	    }
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		String data = savedInstanceState.getString(AlbumAdapter.ADAPTER_DATA_KEY);
		if(data != null){
			AlbumAdapter adapter = new AlbumAdapter(this, imageLoader);
			JSONArray array;
			try {
				array = new JSONArray(data);
				adapter.setData(array);
				this.listView.setAdapter(adapter);
			} catch (JSONException e) {
				Log.e(TAG,"JSONException. Msg: "+e.getMessage());
			}
		}else{
			Log.w(TAG,"Data is null");
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * Called on every session state change.
	 */
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    if (state.isOpened()) {
	        Log.i(TAG, "Logged in...");
	        if(hasPhotoPermissions())
	        	getUserPhotos();
	        else
	        	requestPhotoPermissions();
	    } else if (state.isClosed()) {
	        Log.i(TAG, "Logged out...");
	        this.listView.setAdapter(null);
	    }
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	}
	
	private void requestPhotoPermissions(){
	    Session session = Session.getActiveSession();
		Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, Arrays.asList("user_photos"));	    
		session.requestNewReadPermissions(newPermissionsRequest);
	}
	
	private boolean hasPhotoPermissions(){
	    Session session = Session.getActiveSession();
	    for(String permission : session.getPermissions())
	    	if(permission.equals("user_photos"))
	    		return true;
	    return false;
	}
	
	private void getUserPhotos(){
	    Session session = Session.getActiveSession();
	    Log.d(TAG,"getUserData. session: "+session+", isOpened: "+session.isOpened());
		if (session != null && session.isOpened()) {
	        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
	        	
	            @Override
	            public void onCompleted(GraphUser user, Response response) {
	            	Log.d(TAG,"User id: "+user.getId());
	            	performQuery(user);
	            }
	        }); 
	        Request.executeBatchAsync(request);
	    } 
	}
	
	private void performQuery(GraphUser user){
		Log.d(TAG,"performQuery");
		String fqlQuery = "select src_small, src_big, caption FROM photo WHERE owner = "+user.getId();
	    Bundle params = new Bundle();
	    params.putString("q", fqlQuery);
	    Session session = Session.getActiveSession();
        Request request = new Request(session,
                "/fql",                         
                params,                         
                HttpMethod.GET,
        		this); 
        Request.executeBatchAsync(request);
	}

	@Override
	public void onCompleted(Response response) {
		GraphObject graphObject = response.getGraphObject();
		JSONArray array = (JSONArray) graphObject.getProperty("data");
		this.listView.setAdapter(new AlbumAdapter(this, imageLoader, array));
	}
}
