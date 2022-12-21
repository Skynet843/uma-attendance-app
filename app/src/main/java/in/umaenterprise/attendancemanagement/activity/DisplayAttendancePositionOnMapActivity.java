package in.umaenterprise.attendancemanagement.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Objects;

import in.umaenterprise.attendancemanagement.R;
import in.umaenterprise.attendancemanagement.model.AttendanceModel;

public class DisplayAttendancePositionOnMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private ArrayList<AttendanceModel> mAttendanceList=new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_attendance_positition_on_map);

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            mAttendanceList=(ArrayList<AttendanceModel>)bundle.getSerializable("AttendanceList");
        }else{
            finish();
        }

        setToolBar();
        try {
            // Loading map
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setToolBar() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    /**
     * function to load map. If map is not created it will create it for you
     * */
    private void init() {
        SupportMapFragment googleMap = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        googleMap.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
//        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setTrafficEnabled(false);
        googleMap.setIndoorEnabled(true);
        googleMap.setBuildingsEnabled(true);
        //https://stackoverflow.com/questions/13904651/android-google-maps-v2-how-to-add-marker-with-multiline-snippet
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                Context context = getApplicationContext(); //or getActivity(), YourActivity.this, etc.

                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(context);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(context);
                snippet.setTextColor(Color.BLACK);
                snippet.setTypeface(null, Typeface.BOLD);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);
                return info;
            }
        });

        int length = mAttendanceList.size();
        boolean isCameraAnimated=false;
        for (int i = 0; i < length; i++) {
            AttendanceModel attendanceModel = mAttendanceList.get(i);
            if (attendanceModel != null) {

                if (attendanceModel.getPunchInLatitude() != 0
                        && attendanceModel.getPunchInLongitude() != 0) {
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(attendanceModel.getPunchInLatitude(), attendanceModel.getPunchInLongitude()))
                            .title("Punch In Shift 1")
                            .snippet(attendanceModel.getPunchDate().concat(" (").concat(attendanceModel.getPunchInTime()).concat(")"))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                    if (!isCameraAnimated) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom
                                (new LatLng(attendanceModel.getPunchInLatitude(), attendanceModel.getPunchInLongitude()), 18));
                        isCameraAnimated = true;
                    }
                }

                if (attendanceModel.getPunchOutLatitude() != 0
                        && attendanceModel.getPunchOutLongitude() != 0) {
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(attendanceModel.getPunchOutLatitude(), attendanceModel.getPunchOutLongitude()))
                            .title("Punch Out Shift 1")
                            .snippet(attendanceModel.getPunchDate().concat(" (").concat(attendanceModel.getPunchOutTime()).concat(")"))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                    if (!isCameraAnimated) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom
                                (new LatLng(attendanceModel.getPunchOutLatitude(), attendanceModel.getPunchOutLongitude()), 18));
                        isCameraAnimated = true;
                    }
                }


                if (attendanceModel.getPunchInLatitudeS2() != 0
                        && attendanceModel.getPunchInLongitudeS2() != 0) {
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(attendanceModel.getPunchInLatitudeS2(), attendanceModel.getPunchInLongitudeS2()))
                            .title("Punch In Shift 2")
                            .snippet(attendanceModel.getPunchDate().concat(" (").concat(attendanceModel.getPunchInTimeS2()).concat(")"))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                    if (!isCameraAnimated) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom
                                (new LatLng(attendanceModel.getPunchInLatitudeS2(), attendanceModel.getPunchInLongitudeS2()), 18));
                        isCameraAnimated = true;
                    }
                }

                if (attendanceModel.getPunchOutLatitudeS2() != 0
                        && attendanceModel.getPunchOutLongitudeS2() != 0) {
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(attendanceModel.getPunchOutLatitudeS2(), attendanceModel.getPunchOutLongitudeS2()))
                            .title("Punch Out Shift 2")
                            .snippet(attendanceModel.getPunchDate().concat(" (").concat(attendanceModel.getPunchOutTimeS2()).concat(")"))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                    if (!isCameraAnimated) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom
                                (new LatLng(attendanceModel.getPunchOutLatitudeS2(), attendanceModel.getPunchOutLongitudeS2()), 18));
                        isCameraAnimated = true;
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mAttendanceList != null
                && mAttendanceList.size() > 0) {
            mAttendanceList.clear();
        }
        if(googleMap!=null){
            googleMap.clear();
        }
        super.onDestroy();
        System.gc();
    }
}
