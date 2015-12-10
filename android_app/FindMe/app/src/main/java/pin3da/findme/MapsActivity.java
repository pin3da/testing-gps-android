package pin3da.findme;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnCameraChangeListener {

    private GoogleMap mMap;

    int state     = 0,
        numPoints = 0;

    EditText statusText;
    ArrayList<LatLng> region;
    Button  drawButton,
            sendButton;

    GetBG getBG;
    Vibrator vib;
    Handler handler;
    Runnable poller;
    Marker marker;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initState();
        startPolling();
    }

    public void startPolling() {
        handler = new Handler();
        poller = new Runnable() {
            @Override
            public void run() {
                String ans = null;
                String query = statusText.getText().toString();
                if (!query.equals("-1")) {
                    try {
                        ans = (new GetBG()).execute("http://maps.utp.edu.co:3001/status/?id=" + query).get();
                        StringTokenizer st = new StringTokenizer(ans);
                        String a = st.nextToken();
                        if (!a.equals("noinfo")) {
                            String lon = st.nextToken();
                            String lat = st.nextToken();
                            Log.v("lon", lon);
                            Log.v("lat", lat);


                            if (ans.startsWith("false")) {
                                long[] pattern = {
                                        0, 200, 100, 200, 100, 200, // S 800
                                        100, 400, 100, 400, 100, 400, // 0 1400
                                        100, 200, 100, 200, 100, 200 // S  800
                                };
                                vib.vibrate(pattern, -1);
                            }

                            if (marker != null)
                                marker.remove();

                            if (mMap != null)
                                marker = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(Double.valueOf(lat), Double.valueOf(lon)))
                                    .title("FindMe here!!"));


                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                // statusText.setText(ans);
                handler.postDelayed(this, 10000);
            }
        };
        poller.run();
    }



    public void initState () {
        statusText = (EditText) findViewById(R.id.statusText);
        drawButton = (Button) findViewById(R.id.drawArea);
        sendButton = (Button) findViewById(R.id.sendButton);

        region     = new ArrayList<>();
        getBG      = new GetBG();

        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (vib.hasVibrator()) {
            Log.v("Can Vibrate", "YES");
        } else {
            Log.v("Can Vibrate", "NO");
        }


        drawButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                state ^= 1;
                if (state > 0) {
                    region.clear();
                    drawButton.setText("Finish");
                } else {
                    drawButton.setText("Draw");
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ans = toJSON(region);
                post("http://maps.utp.edu.co:3001/register/", ans);
            }
        });
    }

    void post(String url, String json)  {
        new PostBG().execute(url, json);
    }

    String toJSON(ArrayList<LatLng> l) {
        Log.v("statusText", statusText.getText().toString());
        String ans = "{" +
                        "\"id\":\"" + statusText.getText().toString() + "\"" +
                        ",\"poly\": [";
        for (int i = 0; i < l.size(); ++i) {
            LatLng cur = l.get(i);
            if (i > 0)
                ans += ",";
            ans +=  "{" +
                        "\"lat\": " + cur.latitude +
                        ",\"lon\": " + cur.longitude +

                    "}";
        }
        ans += "]}";
        Log.v("tosend", ans);
        return ans;
    }


    /**
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraChangeListener(this);

        LatLng pere = new LatLng(4.79577342, -75.69023977);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pere));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
        mMap.setMyLocationEnabled(true);



    }

    @Override
    public void onMapClick(LatLng point) {
        if (state > 0) {
            region.add(point);
            mMap.clear();
            PolygonOptions po = new PolygonOptions();
            for (LatLng i : region) {
                po.add(i);
            }
            po.strokeColor(Color.argb(100, 92,107,192)).fillColor(Color.argb(100, 66, 165, 245));
            mMap.addPolygon(po);

        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }
}

