package pro.rane.foodadvisor;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.location.LocationListener;
import android.provider.Settings;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class NewProductFragment extends Fragment{

    pro.rane.foodadvisor.SessionManager session;
    float latitude = 0.0f;
    float longitude = 0.0f;
    private TextView txtLat;
    private TextView txtLng;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private EditText editTextproductName;
    private EditText editTextproductDesc;

    private ProgressBar pb;
    private Button btnNewProduct;
    private Button btnGetLocation;

    public NewProductFragment(){
        //must be empty
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_new_product, container, false);
        //if you want to lock screen for always Portrait mode
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        session = new pro.rane.foodadvisor.SessionManager(getContext());

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        pb = (ProgressBar) rootView.findViewById(R.id.loadingBar);
        pb.setVisibility(View.INVISIBLE);

        editTextproductName = (EditText) rootView.findViewById(R.id.prodName);
        editTextproductDesc = (EditText) rootView.findViewById(R.id.prodDesc);

        editTextproductName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    Utility.hideKeyboard(v);
                }
            }
        });

        editTextproductDesc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    Utility.hideKeyboard(v);
                }
            }
        });

        txtLat = (TextView) rootView.findViewById(R.id.latitude);
        txtLng = (TextView) rootView.findViewById(R.id.longitude);
        txtLat.setText(getString(R.string.latitude).concat(Float.toString(latitude)));
        txtLng.setText(getString(R.string.longitude).concat(Float.toString(longitude)));

        btnGetLocation = (Button) rootView.findViewById(R.id.btnLocation);
        btnGetLocation.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        pb.setVisibility(View.VISIBLE);
                        btnGetLocation.setVisibility(View.INVISIBLE);
                        locationListener = new MyLocationListener();
                        try {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000,1, locationListener);
                        }catch (SecurityException e){
                            e.printStackTrace();
                        }
                    } else {
                        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Il GPS è spento")
                                .setContentText("FoodAdvisor ha bisogno del GPS per funzionanre correttamente!")
                                .setConfirmText("Ho capito!").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        }).show();
                    }
                }
            });

        btnNewProduct = (Button) rootView.findViewById(R.id.new_product_button);
        btnNewProduct.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TextUtils.isEmpty(editTextproductName.getText().toString())){
                    editTextproductName.setError("Campo obbligatorio");
                    return;
                }

                if (TextUtils.isEmpty(editTextproductDesc.getText().toString())){
                    editTextproductDesc.setError("Campo obbligatorio");
                    return;
                }

                if (latitude== 0.0f && longitude == 0.0f){
                    Toast.makeText(getActivity().getApplicationContext(),"Attendere valore coordinate",Toast.LENGTH_SHORT).show();
                    return;
                }

                btnNewProduct.setVisibility(View.INVISIBLE);
                btnGetLocation.setVisibility(View.INVISIBLE);
                pb.setVisibility(View.VISIBLE);


                String productName = editTextproductName.getText().toString();
                String productDesc = editTextproductDesc.getText().toString();
                HashMap<String, String> user = session.getUserDetails();
                String id = user.get(SessionManager.KEY_ID);

                String url = "http://foodadvisor.rane.pro:8080/addArticle";

                Toast.makeText(getActivity().getBaseContext(),"Url : "+url,Toast.LENGTH_SHORT).show();

                JSONObject req = new JSONObject();
                try{
                    req.put("name",productName);
                    req.put("creator_id",id);
                    req.put("description",productDesc);
                    req.put("longitude",longitude);
                    req.put("latitude",latitude);
                    req.put("photo","null");
                }catch (JSONException e){
                    e.printStackTrace();
                }

                //Toast.makeText(getBaseContext(),req.toString(),Toast.LENGTH_SHORT).show();

                pb.setVisibility(View.INVISIBLE);
                btnNewProduct.setVisibility(View.VISIBLE);
                btnGetLocation.setVisibility(View.VISIBLE);

                Intent startPostAct= new Intent(getActivity(), PostActivity.class);
                startPostAct.putExtra("url", url);
                startPostAct.putExtra("req",req.toString());
                startActivity(startPostAct);

            }
        });


        return rootView;
    }




    /*----------Listener class to get coordinates ------------- */
    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            pb.setVisibility(View.INVISIBLE);
            btnGetLocation.setVisibility(View.VISIBLE);
            /*Toast.makeText(getActivity().getBaseContext(),"Coordinate settate:\nLat: " +
                            loc.getLatitude()+ "\nLng: " + loc.getLongitude(),
                    Toast.LENGTH_SHORT).show();*/
            longitude = (float) loc.getLongitude();
            latitude = (float) loc.getLatitude();
            txtLat.setText(getString(R.string.latitude).concat(Float.toString(latitude)));
            txtLng.setText(getString(R.string.longitude).concat(Float.toString(longitude)));
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
        }

    }

}
