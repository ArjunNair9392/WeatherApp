package com.ArjunNair.sunshine;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ArjunNair.sunshine.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Contacts.Intents.UI;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;



/**
     * A Forecast fragment containing a simple view.
     */
    public class ForecastFragment extends Fragment {
    	private ArrayAdapter<String> myForecastAdapter;

        public ForecastFragment() {
        }
        
        @Override
        public void onCreate(Bundle savedInstancesState){
        	super.onCreate(savedInstancesState);
        	setHasOptionsMenu(true);
        }
        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        	inflater.inflate(R.menu.forecastfragment, menu);
        }
        
        @Override
        public boolean onOptionsItemSelected(MenuItem item){
        	int id = item.getItemId();
        	if(id == R.id.action_refresh){
        		updateWeather();
        		
        		return true;
        	}
        	
        	return super.onOptionsItemSelected(item);
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//            String[] forecastArray= {"Today-Sunny-88/63","Tomorrow-Foggy-70/46",
//            		"Weds-Cloudy-72/63", "Thurs-Rainy-64/51",
//            		"Fri-Foggy-70/46","Sat-Sunny-78/68"}; 
            
            List<String> weekForecast = new ArrayList<String>();
            myForecastAdapter=
            						new ArrayAdapter<String>(
            								getActivity(),
            								R.layout.list_item_forecast,
            								R.id.list_item_forecast_textview,
            								weekForecast);
            
            ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
            listView.setAdapter(myForecastAdapter);
            
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            	@Override
				public void onItemClick(AdapterView<?> adapterView, View view,
						int position, long l) {
            		String forecast = myForecastAdapter.getItem(position);
            		Intent intent = new Intent(getActivity(), DetailActivity.class)
            						.putExtra(Intent.EXTRA_TEXT, forecast); 
            		//Toast.makeText(getActivity(), forecast, Toast.LENGTH_LONG).show();
            		startActivity(intent);
            		
				}
			});
            
                 
            return rootView;
        }
        
        public void updateWeather(){
        	FetchWeatherTask weatherTask = new FetchWeatherTask();
    		SharedPreferences Prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    		String Location = Prefs.getString(getString(R.string.example_text),
    				getString(R.string.pref_default_display_zipcode));
        	weatherTask.execute(Location);
        }
        
        @Override
        public void onStart(){
        	super.onStart();
        	updateWeather();
        }
        
        public class FetchWeatherTask extends AsyncTask<String, Void, String[]>{
        	
        	private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        	
        	
        	
        	/* The date/time conversion code is going to be moved outside the asynctask later,
        	 * so for convenience we're breaking it out into its own method now.
        	 */
        	private String getReadableDateString(long time){
        	    // Because the API returns a unix timestamp (measured in seconds),
        	    // it must be converted to milliseconds in order to be converted to valid date.
        	    Date date = new Date(time * 1000);
        	    SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        	    return format.format(date).toString();
        	}
        	 
        	/**
        	 * Prepare the weather high/lows for presentation.
        	 */
        	private String formatHighLows(double high, double low) {
        	    // For presentation, assume the user doesn't care about tenths of a degree.
        		SharedPreferences sharedPrefs = 
        				PreferenceManager.getDefaultSharedPreferences(getActivity());
        		String unitType = sharedPrefs.getString(
        				getString(R.string.example_list),"0");
        		if (unitType.equals("1")){
        			high = (high * 1.8) + 32;
        			low = (low * 1.8) + 32;
        		} else if (!unitType.equals("0")){
        			Log.d(LOG_TAG, "Unit type not found: "+unitType);
        		}
        	    long roundedHigh = Math.round(high);
        	    long roundedLow = Math.round(low);
        	 
        	    String highLowStr = roundedHigh + "/" + roundedLow;
        	    return highLowStr;
        	}
        	 
        	/**
        	 * Take the String representing the complete forecast in JSON Format and
        	 * pull out the data we need to construct the Strings needed for the wireframes.
        	 *
        	 * Fortunately parsing is easy:  constructor takes the JSON string and converts it
        	 * into an Object hierarchy for us.
        	 */
        	private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
        	        throws JSONException {
        	 
        	    // These are the names of the JSON objects that need to be extracted.
        	    final String OWM_LIST = "list";
        	    final String OWM_WEATHER = "weather";
        	    final String OWM_TEMPERATURE = "temp";
        	    final String OWM_MAX = "max";
        	    final String OWM_MIN = "min";
        	    final String OWM_DATETIME = "dt";
        	    final String OWM_DESCRIPTION = "main";
        	    
        	    final String OWM_PRESSURE = "pressure";
        	    final String OWM_HUMIDITY = "humidity";
        	    final String OWM_SPEED = "speed";
        	 
        	    JSONObject forecastJson = new JSONObject(forecastJsonStr);
        	    JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
        	 
        	    String[] resultStrs = new String[numDays];
        	    for(int i = 0; i < weatherArray.length(); i++) {
        	        // For now, using the format "Day, description, hi/low"
        	        String day;
        	        String description;
        	        String highAndLow;
        	 
        	        // Get the JSON object representing the day
        	        JSONObject dayForecast = weatherArray.getJSONObject(i);
        	 
        	        // The date/time is returned as a long.  We need to convert that
        	        // into something human-readable, since most people won't read "1400356800" as
        	        // "this saturday".
        	        long dateTime = dayForecast.getLong(OWM_DATETIME);
        	        day = getReadableDateString(dateTime);
        	 
        	        // description is in a child array called "weather", which is 1 element long.
        	        JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
        	        description = weatherObject.getString(OWM_DESCRIPTION);
        	        
        	        // Get the speed humidity and pressure
        	        long speed = dayForecast.getLong(OWM_SPEED);
        	        long humidity = dayForecast.getLong(OWM_HUMIDITY);
        	        long pressure = dayForecast.getLong(OWM_PRESSURE);
        	        // Temperatures are in a child object called "temp".  Try not to name variables
        	        // "temp" when working with temperature.  It confuses everybody.
        	        JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
        	        double high = temperatureObject.getDouble(OWM_MAX);
        	        double low = temperatureObject.getDouble(OWM_MIN);
        	 
        	        highAndLow = formatHighLows(high, low);
        	        resultStrs[i] = day + " - " + description + " - " + highAndLow ;
        	    }
        	    for(String s: resultStrs){
        	    	Log.v(LOG_TAG,"Forecast entry: "+s);
        	    }
        	 
        	    return resultStrs;
        	}
        	
        	@Override
        	protected String[] doInBackground(String... params){
        	// These two need to be declared outside the try/catch
        	// so that they can be closed in the finally block.
        	HttpURLConnection urlConnection = null;
        	BufferedReader reader = null;
        	 
        	// Will contain the raw JSON response as a string.
        	String forecastJsonStr = null;
        	
        	String format = "json";
        	String units = "metric";
        	int numDays = 7;
        	 
        	try {
        		
        		final String FORECAST_BASE_URL = 
        				"http://api.openweathermap.org/data/2.5/forecast/daily?";
        		final String QUERY_PARAM = "q";
        		final String FORMAT_PARAM = "mode";
        		final String UNITS_PARAM = "units";
        		final String DAYS_PARAM = "cnt";
        		
        		Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
        				.appendQueryParameter(QUERY_PARAM, params[0])
        				.appendQueryParameter(FORMAT_PARAM, format)
        				.appendQueryParameter(UNITS_PARAM, units)
        				.appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
        				.build();
        		
        		URL url = new URL(builtUri.toString());
        		Log.v(LOG_TAG, "Built URI" + builtUri.toString());
        				
        		
        	    // Construct the URL for the OpenWeatherMap query
        	    // Possible parameters are available at OWM's forecast API page, at
        	    // http://openweathermap.org/API#forecast
        	    //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");
        	 
        	    // Create the request to OpenWeatherMap, and open the connection
        	    urlConnection = (HttpURLConnection) url.openConnection();
        	    urlConnection.setRequestMethod("GET");
        	    urlConnection.connect();
        	 
        	    // Read the input stream into a String
        	    InputStream inputStream = urlConnection.getInputStream();
        	    StringBuffer buffer = new StringBuffer();
        	    if (inputStream == null) {
        	        // Nothing to do.
        	        //forecastJsonStr = null;
        	    	return null;
        	    }
        	    reader = new BufferedReader(new InputStreamReader(inputStream));
        	 
        	    String line;
        	    while ((line = reader.readLine()) != null) {
        	        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
        	        // But it does make debugging a *lot* easier if you print out the completed
        	        // buffer for debugging.
        	        buffer.append(line + "\n");
        	    }
        	 
        	    if (buffer.length() == 0) {
        	        // Stream was empty.  No point in parsing.
        	        //forecastJsonStr = null;
        	    	return null;
        	    }
        	    forecastJsonStr = buffer.toString();
        	    Log.v(LOG_TAG,"Forecast JSON String: "+forecastJsonStr);
        	} catch (IOException e) {
        	    Log.e(LOG_TAG, "Error ", e);
        	    // If the code didn't successfully get the weather data, there's no point in attempting
        	    // to parse it.
        	    //forecastJsonStr = null;
        	    return null;
        	} finally{
        	    if (urlConnection != null) {
        	        urlConnection.disconnect();
        	    }
        	    if (reader != null) {
        	        try {
        	            reader.close();
        	        } catch (final IOException e) {
        	            Log.e("PlaceholderFragment", "Error closing stream", e);
        	        }
        	    }
        	}
        	try{
        		return getWeatherDataFromJson(forecastJsonStr, numDays);
        	}catch(JSONException e){
        		Log.e(LOG_TAG,e.getMessage(),e);
        		e.printStackTrace();
        	}
			return null;
        }
        	@Override
        	protected void onPostExecute(String[] result){
        		if(result!= null){
        			myForecastAdapter.clear();
        			for(String dayForecaststr : result){
        				myForecastAdapter.add(dayForecaststr);
        			}
        		}
        	}
    }
    }