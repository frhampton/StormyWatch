package stormy.yellowbear.com.stormywatch;

import android.content.Context;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.delight.android.location.SimpleLocation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
//// YOu are on vid 7 in Working with JSON
public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private double latitude;
    private double longitude;

    private SimpleLocation  location;
    private CurrentWeather  mCurrentWeather;
    private Coordinates     mCoordinates;


    @BindView(R.id.enterCity) EditText cityName;
    @BindView(R.id.tempLable) TextView mTemperturlLable;
    @BindView(R.id.cityName)  TextView mCityNameDisplay;
    @BindView(R.id.humidityNumber)  TextView  mHumidityNumber;
    @BindView(R.id.precipitationNumber) TextView mPrecipitaionNumber;
    @BindView(R.id.summary) TextView mSummary;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        location = new SimpleLocation(this);
        if(!location.hasLocationEnabled()){
            //ask the user to enable location access
            SimpleLocation.openSettings(this);
        }
        getMyLocation();
    }



    //Getting Latitude and Longitude onCreate
    private void getMyLocation() {
        longitude =  location.getLongitude();
        latitude = location.getLatitude();
        Log.i(TAG, "From getMyLocation: \n lat: " + latitude +"\n Long: " + longitude);
        getForecast(latitude,longitude);
    }


    @OnClick(R.id.searchCity)
    public void searchCity(View view){


        String mCityName = cityName.getText().toString();
        if(mCityName.matches("")){
            Toast.makeText(this, "You did not enter a city", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, mCityName, Toast.LENGTH_LONG).show();

        getCoordinates(mCityName);
        getForecast(latitude,longitude);

    }

    private void getCoordinates(String mCityName) {
        String apiKey = getString(R.string.googleApiKey);

        String googleMapsApiURI = getString(R.string.googleAPI)+ mCityName + "&key="+ apiKey;

        if(isNetworkAvailable()){
            //HTTP Request
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(googleMapsApiURI)
                    .build();

            Call call = client.newCall(request);
            //runs code in the background
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String jsonData =response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                           mCoordinates = getCoordinatesDetails(jsonData);
                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught:", e);
                    }catch (JSONException e){
                        Log.e(TAG, "Exception caught:", e);
                    }
                }
            });
        }else {
            // Toast To tell the user network is unavailable
            Toast.makeText(this, R.string.network_unavailable , Toast.LENGTH_LONG).show();
        }
        Log.d(TAG,"Main UI code is running!");
    }

    private void updateDisplay() {
        mTemperturlLable.setText(mCurrentWeather.getTemperature() + "");
        mHumidityNumber.setText(mCurrentWeather.getHumidity()+ "%");
        mPrecipitaionNumber.setText(mCurrentWeather.getPrecipChance()*100 + "%");
        mSummary.setText(mCurrentWeather.getSummary());
       // mCityNameDisplay.setText(mCoordinates.getFormattedAddress());
    }

    public static String getTAG() {
        return TAG;
    }

    // Get Coordinates API
    private Coordinates getCoordinatesDetails(String jsonData) throws JSONException {

        //gets Geocoding results JSON
        JSONObject obj = new JSONObject(jsonData);
        JSONObject res =obj.getJSONArray("results").getJSONObject(0);

        //Gets Formatted_address
        String formattedAddress = res.getString("formatted_address");

        //Gets latitude and longitude
        JSONObject loc = res.getJSONObject("geometry").getJSONObject("location");

        latitude = loc.getDouble("lat") ;
        longitude = loc.getDouble("lng");

        Log.i(TAG, "From getCoordinatesDetails: " + formattedAddress);
        Coordinates coordinates = new Coordinates();
       return   coordinates;
   }

    // get Forecast api
    private void getForecast(double latitude, double longitude) {
        String apiKey =getString(R.string.forecastApiKey);
        String forecastURI = getString(R.string.forecast_io)+apiKey +
                "/"+ latitude +","+ longitude;

        // See if network is Available
        if(isNetworkAvailable()) {
            //HTTP Request
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastURI)
                    .build();

            Call call = client.newCall(request);
            //runs code in the background
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String jsonData =response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            mCurrentWeather = getCurrentDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });

                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught:", e);
                    }catch (JSONException e){
                        Log.e(TAG, "Exception caught:", e);
                    }

                }
            });
        }else {
            // Toast To tell the user network is unavailable
            Toast.makeText(this, R.string.network_unavailable , Toast.LENGTH_LONG).show();
        }

        Log.d(TAG,"Main UI code is running!");
    }

    //Handles the JSONObject
    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast= new JSONObject(jsonData);

        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);

        //Current Weather
        JSONObject currently = forecast.getJSONObject("currently");

        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setTimeZone(timezone);



        Log.d(TAG, "From CurrentWeather PrecipChange: " + currentWeather.getPrecipChance());
        Log.d(TAG, "From CurrentWeather Humidity: " + currentWeather.getHumidity());
        Log.d(TAG, "From CurrentWeather Summary: " + currentWeather.getSummary());
        Log.d(TAG, "From CurrentWeather Temp: " + currentWeather.getTemperature());
        Log.d(TAG, currentWeather.getFormattedTime());
        return  currentWeather;
    }

    //Return true if network is Available
    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if(networkInfo !=null && networkInfo.isConnected()){
            isAvailable= true;
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(),"error_dialog");
    }

}
