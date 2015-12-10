package pin3da.sp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.location.LocationServices;


import java.io.IOException;
import java.util.UUID;


public class send_position extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    protected TextView mLatitudeText;
    protected TextView mLongitudeText;
    protected TextView providerText;
    protected TextView statusText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_position);


        mLatitudeText = (TextView) findViewById((R.id.lat));
        mLongitudeText = (TextView) findViewById((R.id.lon));
        providerText = (TextView) findViewById((R.id.provider));
        statusText = (TextView) findViewById((R.id.status));


        // Don't initialize location manager, retrieve it from system services.
        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                providerText.setText("Changed " + provider);
            }

            @Override
            public void onProviderEnabled(String provider) {
                providerText.setText(provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                providerText.setText("No provider");
            }

            @Override
            public void onLocationChanged(Location location) {
                // Do work with new location. Implementation of this method will be covered later.
                mLongitudeText.setText("" + location.getLongitude());
                mLatitudeText.setText("" + location.getLatitude());
                send_location(location);
            }
        };

        long minTime = 1 * 1000; // Minimum time interval for update in seconds, i.e. 5 seconds.
        long minDistance = 0; // Minimum distance change for update in meters, i.e. 10 meters.

        // Assign LocationListener to LocationManager in order to rec1eive location updates.
        // Acquiring provider that is used for location updates will also be covered later.
        // Instead of LocationListener, PendingIntent can be assigned, also instead of
        // provider name, criteria can be used, but we won't use those approaches now.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                              PackageManager.PERMISSION_GRANTED) {

                ((TextView) findViewById((R.id.lat))).setText("NECESITO PERMISOS!");

                return;
            }
        }
        locationManager.requestLocationUpdates(getProviderName(), minTime,
                minDistance, locationListener);

    }

    String getProviderName() {
        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM); // Chose your desired power consumption level.
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // Choose your accuracy requirement.
        criteria.setSpeedRequired(false); // Chose if speed for first location fix is required.
        criteria.setAltitudeRequired(false); // Choose if you use altitude.
        criteria.setBearingRequired(false); // Choose if you use bearing.
        criteria.setCostAllowed(false); // Choose if this provider can waste money :-)

        return locationManager.getBestProvider(criteria, true);
    }

    void send_location(Location location) {
        InstanceID instanceID = InstanceID.getInstance(this);
        String id = instanceID.getId();
        statusText.setText(id);
        String pos = "{\"lon\":" + location.getLongitude() +
                     ", \"lat\":" + location.getLatitude() +
                     ", \"id\":" + "\"" + id + "\"" + "}" ;

        providerText.setText(pos);
        post("http://maps.utp.edu.co:3001/tracking/", pos);
    }

    void post(String url, String json)  {
        new PostBG().execute(url, json);
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
