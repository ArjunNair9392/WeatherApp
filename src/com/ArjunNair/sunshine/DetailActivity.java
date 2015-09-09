package com.ArjunNair.sunshine;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ShareActionProvider;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.widget.EditText;
import android.widget.TextView;
import android.content.*;

public class DetailActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.activity_detail);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.detail, menu);
//		return true;
//	}
	


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public class PlaceholderFragment extends Fragment {
			SettingsActivity sa = new SettingsActivity();
			private  final String LOG_TAG = PlaceholderFragment.class.getSimpleName();
			//private  final String FORECAST_SHARE_HASHTAG = " #WeatherApp";
			SharedPreferences _sharedPreferences  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			String name = _sharedPreferences.getString("example_text", "pref_default_display_zipcode");
			private String mForecastStr;
			
		public PlaceholderFragment() {
			setHasOptionsMenu(true);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			Intent intent = getActivity().getIntent();
			View rootView = inflater.inflate(R.layout.fragment_detail,
					container, false);
			
			if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
				mForecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
				((TextView) rootView.findViewById(R.id.detail_text))
				.setText(mForecastStr);

			}
			return rootView;
		}
		
		private Intent createShareForecastIntent() {
			
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT,
					mForecastStr +" #"+name
					);
			return shareIntent;
		}
		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			// Inflate the menu; this adds items to the action bar if it is present.
			inflater.inflate(R.menu.detailfragment, menu);
			
			// Retrieve the share menu item
			MenuItem menuItem = menu.findItem(R.id.action_share);
			
			// Get the provider and hole onto it to set/change the share intent.
			
			ShareActionProvider mShareActionProvider = 
					(ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
			
			// Attach an intent to this ShareActionProvider, You can update this at any time,
			// like when the user selects a new piece of data they might like to share.
			if(mShareActionProvider != null){
				mShareActionProvider.setShareIntent(createShareForecastIntent());
			}
			else{
				Log.d(LOG_TAG, "Share Action Provider is null?");
			}
			 super.onCreateOptionsMenu(menu, inflater);
		}
	}
}
