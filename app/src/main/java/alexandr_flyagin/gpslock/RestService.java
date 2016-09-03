package alexandr_flyagin.gpslock;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

public class RestService {
    public interface ApiService {
        // http://localhost:port/api/Locations
        @POST("/Locations")
        void addLocation(@Body LocationStore data, Callback<LocationStore> callback);

        // http://localhost:port/api/Locations
        @POST("/Devices")
        void addDevice(@Body DeviceStore data, Callback<DeviceStore> callback);
    }

    private static final String URL = "http://0.0.0.0/api/";
    private retrofit.RestAdapter restAdapter;
    private ApiService apiService;

    public RestService() {
        restAdapter = new retrofit.RestAdapter.Builder()
                .setEndpoint(URL)
                .setLogLevel(retrofit.RestAdapter.LogLevel.FULL)
                .build();
        apiService = restAdapter.create(ApiService.class);
    }

    public ApiService getService() {
        return apiService;
    }
}
