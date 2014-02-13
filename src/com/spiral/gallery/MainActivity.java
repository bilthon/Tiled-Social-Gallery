package com.spiral.gallery;

import java.util.ArrayList;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.widget.GridView;

public class MainActivity extends LoaderActivity implements Request.Callback {
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

		listView = (GridView) findViewById(R.id.gridview);
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
		
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    if (state.isOpened()) {
	        Log.i(TAG, "Logged in...");
	        if(hasPhotoPermissions())
	        	getUserPhotos();
	        else
	        	requestPhotoPermissions();
	    } else if (state.isClosed()) {
	        Log.i(TAG, "Logged out...");
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
		for(int i = 0; i < array.length(); i++)
			try {
				Log.d(TAG,"element at "+i+": "+array.get(i));
			} catch (JSONException e) {
				Log.e(TAG,"JSONException! Msg: "+e.getMessage());
			}
		this.listView.setAdapter(new AlbumAdapter(this, imageLoader, array));
	}
}
