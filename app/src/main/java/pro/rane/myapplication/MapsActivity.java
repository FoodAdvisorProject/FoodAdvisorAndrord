package pro.rane.myapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";

    private GoogleMap mMap;
    private String info;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Bundle b = getIntent().getExtras();
        if (b != null)
            info = b.getString("qrCodeInformation");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    /*connection to obtain the array  of positions*/

    //TODO Riscrivere adeguatamente la funzione in modo che il programma si blocchi (magari mostrando una progress bar) finchè  la chiamata HTTP non è stata evasa correttamente
    private static String[][] getCoordinates(String tran_id) throws JSONException {

        String dummy_tran_id = "1";

        String richiesta = "http://foodadvisor.rane.pro:8080/getArticleTravel?tran_id=" + dummy_tran_id;

        DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
        HttpGet request = new HttpGet(richiesta);
        // Depends on your web service
        request.setHeader("Content-type", "application/json");

        InputStream inputStream = null;
        String result = "";
        try {
            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            inputStream = entity.getContent();
            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            result = sb.toString();
        } catch (Exception e) {
            Log.i("Errore http request",""+e.getMessage());
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (Exception squish) {
                Log.i(squish.getMessage(), squish.getMessage());
            }
        }

        JSONArray jObject;
        String[][] coordinates;

        jObject = new JSONArray(result);
        String[] latitude = new String[jObject.length()];
        String[] longitude = new String[jObject.length()];

        for (int i = 0; i < jObject.length(); i++) {

            latitude[i] = jObject.getJSONObject(i).getString(LATITUDE);
            longitude[i] = jObject.getJSONObject(i).getString(LONGITUDE);
        }

        /* Decommentare ad implementazione finita
        coordinates = new String[latitude.length][longitude.length];

            for(int i =0; i < latitude.length;i++){
                coordinates[i][0] = latitude[i];
                coordinates[i][1] = longitude[i];
            }
            */

        String[][] dummy_coordinates = {{"45.465454", "9.186515999999983"}, {"41.9027835", "12.496365500000024"}};

        //return coordinates;
        return dummy_coordinates;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        String[][] coordinates  = {{"45.465454", "9.186515999999983"}, {"41.9027835", "12.496365500000024"},{"40.9027835", "15.496365500000024"}}; //= null;
        try {
            coordinates = getCoordinates(info);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Integer a;
        mMap = googleMap;
        View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
        TextView numTxt = (TextView) marker.findViewById(R.id.num_txt);

        for (a = 0; a < coordinates.length; a++) {

            if(a==0){
                numTxt.setText("Go");
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(coordinates[a][0]), Double.parseDouble(coordinates[a][1])))
                        .title("GO")
                        .snippet("Start point "+ a.toString())
                        .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker)))
                );
            }else {
                numTxt.setText(a.toString());
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(coordinates[a][0]), Double.parseDouble(coordinates[a][1])))
                        .title(a.toString())
                        .snippet("Arrival point " + a.toString())
                        .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker)))
                );
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(coordinates[a][0]), Double.parseDouble(coordinates[a][1])),5));
        }



    }
    // Convert a view to bitmap
    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new DrawerLayout.LayoutParams(DrawerLayout.LayoutParams.WRAP_CONTENT, ViewPager.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }
}
