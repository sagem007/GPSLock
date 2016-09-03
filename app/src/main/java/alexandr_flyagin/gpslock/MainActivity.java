package alexandr_flyagin.gpslock;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * step1: buildGoogleApiClient
 * step2: createLocationRequest
 * step3: buildLocationSettingsRequest
 * step4: по щелчку кнопки выполняется поиск местоположения (через checkLocationSettings метод)
 * step5: по щелчку опций в диалоговом окне
 * step6: в зависимости от действия, полученного из диалогового окна (startResolutionForResult или проверка действия в onActivityResult)
 */
public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult> {

    protected static final String TAG = "MainActivity";

    // Константы для диалогового окна настроек
    protected static final int CODE_REQUEST_GOOGLE_PLAY_SERVICES = 0;
    protected static final int CODE_REQUEST_CHECK_SETTINGS = 1;
    protected static final int CODE_REQUEST_ACCESS_PERMISSION = 2;

    // Частота обновления
    public static final long UPDATE_INTERVAL = 5000;

    // Скорость обновления не больше заданной величины
    public static final long FASTEST_UPDATE_INTERVAL =
            UPDATE_INTERVAL / 2;

    @BindView(R.id.btn_start_fused_location)
    Button btn_start_fused_location;
    @BindView(R.id.btn_grant_permission)
    Button btn_grant_permission;
    @BindView(R.id.btn_stop_fused_location)
    Button btn_stop_fused_location;
    @BindView(R.id.btn_send_fused_location)
    Button btn_send_fused_location;

    @BindView(R.id.mPostalcodeTextView)
    TextView mPostalcodeTextView;
    @BindView(R.id.mCityTextView)
    TextView mCityTextView;
    @BindView(R.id.mDateTextView)
    TextView mDateTextView;
    @BindView(R.id.mLatitudeTextView)
    TextView mLatitudeTextView;
    @BindView(R.id.mLongitudeTextView)
    TextView mLongitudeTextView;
    @BindView(R.id.mSpeedTextView)
    TextView mSpeedTextView;
    @BindView(R.id.mBearingTextView)
    TextView mBearingTextView;
    @BindView(R.id.mAltitudeTextView)
    TextView mAltitudeTextView;
    @BindView(R.id.mAccuracyTextView)
    TextView mAccuracyTextView;

    // Обеспечивает точку входа для служб Google Play
    protected GoogleApiClient mGoogleApiClient;

    // Параметры хранилищ для запросов к FusedLocationProviderApi
    protected LocationRequest mLocationRequest;

    // Хранит типы служб определения местоположения для пользователя
    // Используется в checkLocationSettings (чтобы определить,
    // есть ли у устройства оптимальные настройки местоположения)
    protected LocationSettingsRequest mLocationSettingsRequest;

    // Предоставляет данные местоположения
    protected Location mCurrentLocation;

    // Флаг состояние запроса обновлений информации о местоположении
    // Значение изменяется, когда пользователь нажимает кнопки StartLocation и StopLocation
    protected Boolean mRequestingLocationUpdates;

    @Override
    protected void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnected()) {
            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
            if (resultCode == ConnectionResult.SUCCESS) {
                Toast.makeText(getApplicationContext(), "isGooglePlayServicesAvailable = SUCCESS", Toast.LENGTH_LONG).show();
                mGoogleApiClient.connect();
            } else {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, CODE_REQUEST_GOOGLE_PLAY_SERVICES);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient != null)
            if (mGoogleApiClient.isConnected())
                mGoogleApiClient.disconnect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CODE_REQUEST_ACCESS_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permissions was granted.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Permissions denied.", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @OnClick(R.id.btn_start_fused_location)
    void start_click() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //step 4
            checkLocationSettings();
        }
    }

    @OnClick(R.id.btn_send_fused_location)
    void send_click() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {

            new SendLocationsTask().execute();
        }
    }

    @OnClick(R.id.btn_stop_fused_location)
    void stop_click() {
        if (mGoogleApiClient.isConnected()) {
            stopLocation();
        }
    }

    @OnClick(R.id.btn_grant_permission)
    void permission_click() {
        if (
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.INTERNET,
                            Manifest.permission.READ_PHONE_STATE
                    },
                    CODE_REQUEST_ACCESS_PERMISSION);
        } else if (
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permissions already was granted.", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mRequestingLocationUpdates = false;

        //step 1
        buildGoogleApiClient();

        //step 2
        createLocationRequest();

        //step 3
        buildLocationSettingsRequest();
    }

    // step 1
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    // step 2
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Устанавливает желаемый интервал для обновленя информации о местоположении
        // Этот интервал неточный
        // Вы можете не получить обновления вообще, если ресурсы определения местоположения
        // недоступны, или вы можете получить их медленнее, чем требуется
        // Вы можете также получить обновления быстрее, чем требуется,
        // если другие приложения запрашивают местоположение в более быстром интервале
        mLocationRequest.setInterval(UPDATE_INTERVAL);

        // Устанавливает самый быстрый интервал для обновления информации о местоположении
        // Этот интервал точен, и приложение никогда не будет получать обновления быстрее,
        // чем данное значение
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    // step 3
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    // step 4
    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }

    // Запрос обновления информации через FusedLocationApi
    protected void startLocation() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = true;
                Toast.makeText(MainActivity.this, "Location is on.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Приостановка обовления информации от FusedLocationApi
    protected void stopLocation() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = false;
                Toast.makeText(MainActivity.this, "Location is off.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            updateLocationUI();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        updateLocationUI();
        Toast.makeText(this, "Location updated.", Toast.LENGTH_SHORT).show();
    }

    // Вызывается после вывода диалогового окна настроек
    // step 5
    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                startLocation();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    status.startResolutionForResult(MainActivity.this, CODE_REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                }
                break;
        }
    }


    // OnActivityResult используется в случае,
    // когда LocationSettingsStatusCodes.RESOLUTION_REQUIRED получен в методе OnResult
    // step 6
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CODE_REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        startLocation();
                        break;
                }
                break;
        }
    }

    // Обновление информации на экране
    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            double lat = mCurrentLocation.getLatitude();
            double lon = mCurrentLocation.getLongitude();
            double alt = mCurrentLocation.getAltitude();
            float brg = mCurrentLocation.getBearing();
            float acc = mCurrentLocation.getAccuracy();
            float spd = mCurrentLocation.getSpeed();
            long tm = mCurrentLocation.getTime();

            mLatitudeTextView.setText(String.format(" %s: %f", getResources().getString(R.string.latitude_label),
                    lat));
            mLongitudeTextView.setText(String.format(" %s: %f", getResources().getString(R.string.longitude_label),
                    lon));
            mDateTextView.setText(String.format(" %s: %s", getResources().getString(R.string.date_label),
                    new Date(tm)));
            mSpeedTextView.setText(String.format(" %s: %s", getResources().getString(R.string.speed_label),
                    spd));
            mBearingTextView.setText(String.format(" %s: %s", getResources().getString(R.string.bearing_label),
                    brg));
            mAltitudeTextView.setText(String.format(" %s: %s", getResources().getString(R.string.altitude_label),
                    alt));
            mAccuracyTextView.setText(String.format(" %s: %s", getResources().getString(R.string.accuracy_label),
                    acc));
            updateCityAndPincode(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

            new AddLocationTask().execute(new LocationStore(-1, lon, lat, alt, acc, brg, spd, tm));
        }
    }

    // Получение информции о городе и почтовом индексе
    private void updateCityAndPincode(double latitude, double longitude) {
        try {
            Geocoder gc = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = gc.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                mCityTextView.setText(String.format(" %s: %s",
                        getResources().getString(R.string.city_label), addresses.get(0).getLocality()));
                mPostalcodeTextView.setText(String.format(" %s: %s",
                        getResources().getString(R.string.postalcode_label), addresses.get(0).getPostalCode()));
            }
        } catch (Exception e) {
        }
    }

    public class AddLocationTask extends AsyncTask<LocationStore, Void, Boolean> {
        @Override
        protected Boolean doInBackground(LocationStore... params) {
            LocationStore ls = params[0];
            LocalDB_Helper ldbh = new LocalDB_Helper(MainActivity.this);
            try {
                SQLiteDatabase db = ldbh.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(LocalDB_Helper.COLUMN_LATITUDE, ls.getLatitude());
                values.put(LocalDB_Helper.COLUMN_LONGITUDE, ls.getLongitude());
                values.put(LocalDB_Helper.COLUMN_ALTITUDE, ls.getAltitude());
                values.put(LocalDB_Helper.COLUMN_ACCURACY, ls.getAccuracy());
                values.put(LocalDB_Helper.COLUMN_BEARING, ls.getBearing());
                values.put(LocalDB_Helper.COLUMN_SPEED, ls.getSpeed());
                values.put(LocalDB_Helper.COLUMN_TIME, ls.getTime());
                db.insert(LocalDB_Helper.TABLE_NAME, null, values);
                db.close();
                return true;
            } catch (SQLiteException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast.makeText(MainActivity.this, "Database unavailable.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class SendLocationsTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... params) {
            LocalDB_Helper ldbh = new LocalDB_Helper(MainActivity.this);
            RestService restService = new RestService();
            TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            try {
                String selectQuery = "SELECT * FROM " + LocalDB_Helper.TABLE_NAME;
                SQLiteDatabase db = ldbh.getWritableDatabase();
                Cursor cursor = db.rawQuery(selectQuery, null);

                if (cursor.moveToFirst()) {
                    do {
                        LocationStore ls = new LocationStore();
                        ls.setIMEI(mTelephonyMgr.getDeviceId());
                        ls.setId(Integer.parseInt(cursor.getString(0)));
                        ls.setLatitude(Double.parseDouble(cursor.getString(1)));
                        ls.setLongitude(Double.parseDouble(cursor.getString(2)));
                        ls.setAltitude(Double.parseDouble(cursor.getString(3)));

                        ls.setAccuracy(Float.parseFloat(cursor.getString(4)));
                        ls.setBearing(Float.parseFloat(cursor.getString(5)));
                        ls.setSpeed(Float.parseFloat(cursor.getString(6)));

                        ls.setTime(Long.parseLong(cursor.getString(7)));

                        // Send to WebApi code
                        restService.getService().addLocation(ls, new Callback<LocationStore>() {
                            @Override
                            public void success(LocationStore ls, Response response) {
                            }

                            @Override
                            public void failure(RetrofitError error) {
                            }
                        });
                    } while (cursor.moveToNext());
                }
                cursor.close();
                db.close();
                return true;
            } catch (SQLiteException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast.makeText(MainActivity.this, "API server error.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Success.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
