package pro.rane.foodadvisor;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

import org.json.JSONObject;


public class ScanFragment extends Fragment implements QRCodeReaderView.OnQRCodeReadListener{

    private static final int MY_PERMISSION_REQUEST_CAMERA = 0;
    private static final String url = "http://foodadvisor.rane.pro:8080/getTransaction?tran_id=";

    private ViewGroup cameraLayout;
    private ProgressBar pb;
    private Button newTranBtn;

    private TextView resultTextView;
    private QRCodeReaderView qrCodeReaderView;
    private CheckBox flashlightCheckBox;
    private PointsOverlayView pointsOverlayView;

    private String tran_id;
    private float latitude;
    private float longitude;



    // TODO: 05/04/2017 get /getTransaction
    // TODO: 05/04/2017 post /addTransaction
    // TODO: 05/04/2017 Coordinate GPS prendere
    // TODO: 05/04/2017 getTransaction prende article_id e seller_id, buyer_id si ricava dal SessionManager, le coordinate dal GPS

    public ScanFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scan, container, false);

        pb = (ProgressBar) rootView.findViewById(R.id.progrBar3);
        newTranBtn = (Button) rootView.findViewById(R.id.btnTran);
        cameraLayout = (ViewGroup) rootView.findViewById(R.id.camera_layout);


        pb.setVisibility(View.INVISIBLE);
        newTranBtn.setVisibility(View.INVISIBLE);


        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            View content =  inflater.inflate(R.layout.content_decoder, cameraLayout, true);
            qrCodeReaderView = (QRCodeReaderView) content.findViewById(R.id.qrdecoderview);
            resultTextView = (TextView) content.findViewById(R.id.result_text_view);
            flashlightCheckBox = (CheckBox) content.findViewById(R.id.flashlight_checkbox);
            pointsOverlayView = (PointsOverlayView) content.findViewById(R.id.points_overlay_view);

            qrCodeReaderView.setAutofocusInterval(2000L);
            qrCodeReaderView.setOnQRCodeReadListener(this);
            qrCodeReaderView.setBackCamera();
            flashlightCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    qrCodeReaderView.setTorchEnabled(isChecked);
                }
            });

            qrCodeReaderView.setQRDecodingEnabled(true);
            qrCodeReaderView.startCamera();

        } else {
            requestCameraPermission();
        }

        newTranBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newTranBtn.setVisibility(View.INVISIBLE);
                pb.setVisibility(View.VISIBLE);
                tran_id =resultTextView.getText().toString();
                getTransaction(tran_id);
            }
        });

        return rootView;
    }

    private void getTransaction(String transaction_id) {
        RequestQueue queue = Volley.newRequestQueue(getContext());

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET,url.concat(transaction_id),null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getContext(),response.toString(),Toast.LENGTH_LONG).show();
                addTransaction(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub

            }
        });

        queue.add(jsonRequest);

        return ;

    }

    private void addTransaction(JSONObject response) {
        // TODO: 05/04/17 addTransaction 
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
            Snackbar.make(cameraLayout, "Per continuare sono necessari i permessi della fotocamera.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override public void onClick(View view) {

                    ActivityCompat.requestPermissions(getActivity(), new String[] {
                            Manifest.permission.CAMERA
                    }, MY_PERMISSION_REQUEST_CAMERA);
                }
            }).show();
        } else {
            Snackbar.make(cameraLayout, "Permessi non disponibili. Richiedo i permessi.",
                    Snackbar.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(getActivity(), new String[] {
                    Manifest.permission.CAMERA
            }, MY_PERMISSION_REQUEST_CAMERA);
        }
    }


    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        resultTextView.setText(text);
        pointsOverlayView.setPoints(points);
        newTranBtn.setVisibility(View.VISIBLE);
    }
}
