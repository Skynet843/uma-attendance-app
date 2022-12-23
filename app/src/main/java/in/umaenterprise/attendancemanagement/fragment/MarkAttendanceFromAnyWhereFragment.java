package in.umaenterprise.attendancemanagement.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.work.Data;

import com.google.android.gms.common.ConnectionResult;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import in.umaenterprise.attendancemanagement.R;
import in.umaenterprise.attendancemanagement.activity.ImagePickerActivity;
import in.umaenterprise.attendancemanagement.activity.UserDashboardActivity;
import in.umaenterprise.attendancemanagement.application.AttendanceApplication;
import in.umaenterprise.attendancemanagement.model.AttendanceModel;
import in.umaenterprise.attendancemanagement.model.LatLong;
import in.umaenterprise.attendancemanagement.model.PersonModel;
import in.umaenterprise.attendancemanagement.model.ShopTimingModel;
import in.umaenterprise.attendancemanagement.utils.CommonMethods;
import in.umaenterprise.attendancemanagement.utils.ConstantData;
import in.umaenterprise.attendancemanagement.utils.DigitalClock;
import in.umaenterprise.attendancemanagement.utils.NotificationHandler;
import in.umaenterprise.attendancemanagement.utils.OnClockTickChangeEvent;
import in.umaenterprise.attendancemanagement.utils.SharePreferences;

public class MarkAttendanceFromAnyWhereFragment extends Fragment implements
        View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 15;
    private static final String TAG = "SOUVIK";
    private ImageView imgVAttendancePunchIn, imgVAttendancePunchOut,
            imgVAttendancePunchComplete;
    private DigitalClock tvAttendanceCurrentTime;
    private TextView tvAttendancePunchType,
            tvAttendanceCurrentTimeIcon;
    private TextView tvAttendanceCurrentDate, tvAttendancePunchInTime,
            tvAttendancePunchOutTime;
    private TextView  tvAttendancePunchInTimeS2,
            tvAttendancePunchOutTimeS2;

    private boolean isLastLoginCheck = false, isPunchIn = false,
            isPunchOut = false;
    private boolean isPunchInS2 = false,
            isPunchOutS2 = false;
    private boolean isLocationChangeTrigger = false;
    private boolean isShift2=false;
    private boolean isRightLocation=false;

    private double dbllat = 0, dbllng = 0;
    private static final int ATTENDANCE_IN_IMAGE = 1007;  //Added by me
    private static final int ATTENDANCE_OUT_IMAGE = 1009;  //Added by me
    private Uri punchInImageURI = null; //Added by me
    private Uri punchOutImageURI = null; //Added by me

    private static final int ATTENDANCE_IN_IMAGES2 = 1010;  //Added by me
    private static final int ATTENDANCE_OUT_IMAGES2 = 1011;  //Added by me
    private Uri punchInImageURIS2 = null; //Added by me
    private Uri punchOutImageURIS2 = null; //Added by me

    private Location objlocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static final int REQUEST_CHECK_SETTINGS = 0;
    private PersonModel mPersonModel;

    private String currentDate = new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US).format(Calendar.getInstance().getTime());
    private String monthYear = new SimpleDateFormat(ConstantData.MONTH_YEAR_FORMAT, Locale.US).format(Calendar.getInstance().getTime());
    private AttendanceModel mPreviousAttendanceModel = null;
    private TextView mTvCurrentLocationAccuracy;
    private TextView mTvCurrentLocationLatitude, mTvCurrentLocationLongitude;
    private GoogleMap mMap;
    private FloatingActionButton mFabRefreshLocation;
    private ArrayList<ShopTimingModel> mTimeSlotArrayList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        if (!CommonMethods.isPlayServicesAvailable(getActivity()))
            return;
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.item_logout);
        if (item != null)
            item.setVisible(false);
    }

    /**
     * Creating location request object
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest
                .setInterval(5 * 1000);
        mLocationRequest
                .setFastestInterval(5 * 1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest
                .setMaxWaitTime(10 * 1000);
    }

    /**
     * Creating google api client object
     */
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(Objects.requireNonNull(getActivity()))
                .addApi(LocationServices.API)
                .addConnectionCallbacks(MarkAttendanceFromAnyWhereFragment.this)
                .addOnConnectionFailedListener(MarkAttendanceFromAnyWhereFragment.this).build();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mark_attendance_from_anywhere, container, false);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        // Building the GoogleApi client
        buildGoogleApiClient();
        createLocationRequest();
        setToolBar(view);
        init(view);
        return view;
    }

    private void setToolBar(View view) {

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.toolbar_title_mark_attendance));
        ((UserDashboardActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        Objects.requireNonNull(((UserDashboardActivity) getActivity()).getSupportActionBar()).setDisplayShowTitleEnabled(true);
        Objects.requireNonNull(((UserDashboardActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((UserDashboardActivity) Objects.requireNonNull(getActivity())).onBackPressed();
            }
        });
    }

    private void init(View view) {

        if (getArguments() != null) {
            mPersonModel = (PersonModel) getArguments().getSerializable("PersonModel");
        } else
            return;

        assert mPersonModel != null;
        /**
         * Here we get time slot list
         */
        if (!mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_HOUR_WISE)) {
            mTimeSlotArrayList = mPersonModel.getTimeSlotList();
            if (mTimeSlotArrayList == null
                    || mTimeSlotArrayList.size() == 0) {
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CommonMethods.showAlertForTimeSlotNotFound(getActivity());
                    }
                });
            }
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        imgVAttendancePunchIn = view
                .findViewById(R.id.imgVAttendancePunchIn);
        imgVAttendancePunchOut = view
                .findViewById(R.id.imgVAttendancePunchOut);
        imgVAttendancePunchComplete = view
                .findViewById(R.id.imgVAttendancePunchComplete);
        tvAttendancePunchType = view
                .findViewById(R.id.tvAttendancePunchType);
        tvAttendanceCurrentTime = view
                .findViewById(R.id.tvAttendanceCurrentTime);
        tvAttendanceCurrentTimeIcon = view
                .findViewById(R.id.tvAttendanceCurrentTimeIcon);
        tvAttendanceCurrentDate = view
                .findViewById(R.id.tvAttendanceCurrentDate);
        tvAttendancePunchInTime = view
                .findViewById(R.id.tvAttendancePunchInTime);
        tvAttendancePunchOutTime = view
                .findViewById(R.id.tvAttendancePunchOutTime);

        tvAttendancePunchInTimeS2 = view
                .findViewById(R.id.tvAttendancePunchInTimeS2);
        tvAttendancePunchOutTimeS2 = view
                .findViewById(R.id.tvAttendancePunchOutTimeS2);

        mFabRefreshLocation = view
                .findViewById(R.id.fab_refresh_location);

        mTvCurrentLocationAccuracy = view
                .findViewById(R.id.tv_current_location_accuracy);
        mTvCurrentLocationLatitude = view
                .findViewById(R.id.tv_current_location_latitude);
        mTvCurrentLocationLongitude = view
                .findViewById(R.id.tv_current_location_longitude);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
            }
        } else {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        if (ActivityCompat.shouldShowRequestPermissionRationale(Objects.requireNonNull(getActivity()),
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            showRationaleDialog();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }


        mFabRefreshLocation.setOnClickListener(this);
        imgVAttendancePunchIn.setOnClickListener(this);
        imgVAttendancePunchOut.setOnClickListener(this);

        imgVAttendancePunchIn.setVisibility(View.VISIBLE);
        imgVAttendancePunchOut.setVisibility(View.GONE);
        imgVAttendancePunchComplete.setVisibility(View.GONE);

        tvAttendanceCurrentDate.setText(currentDate);
        tvAttendancePunchInTime.setText(ConstantData.YET_TO_COME);
        tvAttendancePunchOutTime.setText(ConstantData.YET_TO_LEAVE);
        tvAttendancePunchInTimeS2.setText(ConstantData.YET_TO_COME);
        tvAttendancePunchOutTimeS2.setText(ConstantData.YET_TO_LEAVE);

        SetCurrentTime();
        CheckLastLogin();
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v.getId() == R.id.imgVAttendancePunchIn) {
            if(!isRightLocation){
                CommonMethods.showAlertDailogueWithOK(getActivity(), "Wrong Location",
                        String.format("Please go To "+mPersonModel.getWorkArea().getBranchName()+" then punch."), getString(R.string.action_ok));
                return;
            }
            if (!SharePreferences.getBool(SharePreferences.KEY_IS_PUNCH, SharePreferences.DEFAULT_BOOLEAN)) {
                if(isShift2)
                    onProfileImageClick(ATTENDANCE_IN_IMAGES2);
                else
                    onProfileImageClick(ATTENDANCE_IN_IMAGE);
            }
        } else if (v.getId() == R.id.imgVAttendancePunchOut) {
            if(!isRightLocation){
                CommonMethods.showAlertDailogueWithOK(getActivity(), "Wrong Location",
                        String.format("Please go To "+mPersonModel.getWorkArea().getBranchName()+" then punch."), getString(R.string.action_ok));
                return;
            }
            if (!SharePreferences.getBool(SharePreferences.KEY_IS_PUNCH, SharePreferences.DEFAULT_BOOLEAN)) {
                if(isShift2)
                    onProfileImageClick(ATTENDANCE_OUT_IMAGES2);
                else
                    onProfileImageClick(ATTENDANCE_OUT_IMAGE);
            }
        } else if (v.getId() == R.id.fab_refresh_location) {
            RefreshClick();
        }
    }

    /*
     * This is added by me
     */


    private void onProfileImageClick(int code) {
        Dexter.withActivity(getActivity())
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            launchCameraIntent(code);
                        } else {
                            // TODO - handle permission denied case
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void launchCameraIntent(int code) {
        Intent intent = new Intent(getActivity(), ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 3); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 4);

        intent.putExtra("lock_crop",false);
        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 105000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 140000);
        Log.d(TAG, "launchCameraIntent: My Fault");
        Toast.makeText(getActivity(),"My Fault",Toast.LENGTH_SHORT).show();
        ImagePickerActivity.first_time=true;
        startActivityForResult(intent, code);
    }

    /*
     * Add end
     */
    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_info, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_info) {
            showDialogForInformation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Check Last Login Information
     */
    private void CheckLastLogin() {

        try {
            if (CommonMethods.isNetworkConnected(Objects.requireNonNull(getActivity()))) {

                /**
                 * Here we get attendance data for selected date
                 */
                CommonMethods.showProgressDialog(getActivity());
                AttendanceApplication.refCompanyUserAttendanceDetails
                        .child(mPersonModel.getFirebaseKey())
                        .child(monthYear)
                        .orderByChild("punchDate").equalTo(currentDate)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                isLastLoginCheck = true;
                                CommonMethods.cancelProgressDialog();

                                /**
                                 * Here 2 possibilities there
                                 * Whether user's punch entry exist for current day or not
                                 * So we manage both case here
                                 */
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        mPreviousAttendanceModel = ds.getValue(AttendanceModel.class);
                                        assert mPreviousAttendanceModel != null;
                                        mPreviousAttendanceModel.setFirebaseKey(ds.getKey());

                                        String PunchInTime = mPreviousAttendanceModel.getPunchInTime() != null ?
                                                mPreviousAttendanceModel.getPunchInTime().toUpperCase()
                                                : ConstantData.YET_TO_COME;
                                        String PunchOutTime = mPreviousAttendanceModel.getPunchOutTime() != null ?
                                                mPreviousAttendanceModel.getPunchOutTime().toUpperCase()
                                                : ConstantData.YET_TO_LEAVE;

                                        String PunchInTimeS2 = mPreviousAttendanceModel.getPunchInTimeS2() != null ?
                                                mPreviousAttendanceModel.getPunchInTimeS2().toUpperCase()
                                                : ConstantData.YET_TO_COME;
                                        String PunchOutTimeS2 = mPreviousAttendanceModel.getPunchOutTimeS2() != null ?
                                                mPreviousAttendanceModel.getPunchOutTimeS2().toUpperCase()
                                                : ConstantData.YET_TO_LEAVE;

                                        tvAttendancePunchInTime.setText(PunchInTime);
                                        tvAttendancePunchOutTime.setText(PunchOutTime);
                                        tvAttendancePunchInTimeS2.setText(PunchInTimeS2);
                                        tvAttendancePunchOutTimeS2.setText(PunchOutTimeS2);


                                        if ((mPreviousAttendanceModel.getPunchInTime() != null
                                                && mPreviousAttendanceModel.getPunchOutTime() != null && mPreviousAttendanceModel.getPunchInTimeS2() != null
                                                && mPreviousAttendanceModel.getPunchOutTimeS2() != null)||mPreviousAttendanceModel.getPresentDay()==1.0) {

                                            MakeGoodByeVisible();
                                        } else {
                                            if (mPreviousAttendanceModel.getPunchInTime() == null) {
                                                MakePunchInVisible();
                                                isShift2=false;

                                            } else if (mPreviousAttendanceModel.getPunchOutTime() == null) {
                                                MakePunchOutVisible();
                                                isShift2=false;

                                            } else if(mPreviousAttendanceModel.getPunchInTimeS2()==null){
                                                MakePunchInVisible();

                                                isShift2=true;
                                            }else {
                                                MakePunchOutVisible();
                                                isShift2=true;
                                            }
                                        }

                                        if (SharePreferences.getBool(SharePreferences.KEY_IS_PUNCH, SharePreferences.DEFAULT_BOOLEAN)) {
                                            imgVAttendancePunchComplete.setVisibility(View.VISIBLE);
                                            imgVAttendancePunchIn.setVisibility(View.GONE);
                                            imgVAttendancePunchOut.setVisibility(View.GONE);
                                        }
                                        break;
                                    }
                                } else {
                                    String PunchInTime = ConstantData.YET_TO_COME;
                                    String PunchOutTime = ConstantData.YET_TO_LEAVE;
                                    String PunchInTimeS2 = ConstantData.YET_TO_COME;
                                    String PunchOutTimeS2 = ConstantData.YET_TO_LEAVE;

                                    tvAttendancePunchInTime.setText(PunchInTime);
                                    tvAttendancePunchOutTime.setText(PunchOutTime);
                                    tvAttendancePunchInTimeS2.setText(PunchInTimeS2);
                                    tvAttendancePunchOutTimeS2.setText(PunchOutTimeS2);
                                    MakePunchInVisible();
                                }
                                getLocationOfUser();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError e) {
                                lastLoginCheck(false);
                                getLocationOfUser();
                                CommonMethods.cancelProgressDialog();
                                CommonMethods.showAlertDailogueWithOK(getActivity(), getString(R.string.title_alert),
                                        String.format(getString(R.string.msg_issue_while_fetching_attendance), e.getMessage()), getString(R.string.action_ok));
                            }
                        });
            } else {
                lastLoginCheck(false);
                Toast.makeText(getActivity(),
                        getString(R.string.alert_msg_connectionError),
                        Toast.LENGTH_LONG).show();
                getLocationOfUser();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the Location using GPS,Network & Passive Provider. While user make
     * Punch and if user didn't get location from any provider then application
     * allow him to make Punch by providing alert to the user.In which user have
     * to enter valid reason and then user make further process for Punch.
     */
    private void getLocationOfUser() {

        objlocation = null;
        dbllat = 0;
        dbllng = 0;

        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        LocationRequest mLocationRequestBalancedPowerAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestBalancedPowerAccuracy
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequestHighAccuracy)
                .addLocationRequest(mLocationRequestBalancedPowerAccuracy);
        locationSettingsRequest.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(mGoogleApiClient,
                        locationSettingsRequest.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NotNull LocationSettingsResult result) {
                Status status = result.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location
                        // requests here.

                        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        if ((mGoogleApiClient != null)
                                && mGoogleApiClient.isConnected()) {
                            objlocation = LocationServices.FusedLocationApi
                                    .getLastLocation(mGoogleApiClient);

                            if (objlocation == null) {
                                /**
                                 * Here what we have done is when user hit the Punch
                                 * button and didn't get the Location then as per what
                                 * we said above, application show him alert and do
                                 * further process as per below.
                                 */
                                if (isPunchIn || isPunchOut ||isPunchInS2 || isPunchOutS2) {

                                    Toast.makeText(getActivity(),
                                            getString(R.string.msg_application_not_able_to_resolve_location)
                                            , Toast.LENGTH_SHORT).show();
                                } else if (isLastLoginCheck) {
                                    Toast.makeText(getActivity(),
                                            getString(R.string.msg_application_not_able_to_resolve_location)
                                            , Toast.LENGTH_SHORT).show();
                                }
                            } else
                                FindDistance(false);
                        }
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be
                        // fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling
                            // startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(getActivity(),
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have
                        // no way to fix the
                        // settings so we won't show the dialog.
                        mGoogleApiClient.disconnect();
                        break;
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted. Do the
                // contacts-related task you need to do.
                if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                        Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setZoomControlsEnabled(true);

                    if (mGoogleApiClient != null
                            && mGoogleApiClient.isConnected()) {
                        objlocation = LocationServices.FusedLocationApi
                                .getLastLocation(mGoogleApiClient);
                        if (objlocation != null)
                            FindDistance(false);
                    }

                    startLocationUpdates();
                }
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(Objects.requireNonNull(getActivity()), Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(getActivity(), "permission denied.", Toast.LENGTH_LONG).show();
                } else {
                    showRationaleDialog();
                }
            }
        }
    }

    private void showRationaleDialog() {
        new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setPositiveButton(getString(R.string.action_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    }
                })
                .setNegativeButton(getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .setMessage(getString(R.string.msg_requirement_of_location_permission))
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("AGENT", "onActivityResult: " + requestCode);
        Log.d("Zero","Go Gobinda");
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK
                && requestCode == REQUEST_CHECK_SETTINGS) {

            makePunchInOutFalse();

            if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (mGoogleApiClient != null
                    && mGoogleApiClient.isConnected()) {
                objlocation = LocationServices.FusedLocationApi
                        .getLastLocation(mGoogleApiClient);
                if (objlocation != null)
                    FindDistance(false);
            }
        } else if (requestCode == ATTENDANCE_IN_IMAGE && resultCode == Activity.RESULT_OK) {
            Log.d("AGENT", "onActivityResult: " + data.getParcelableExtra("path"));
            isPunchIn = true;
            isLocationChangeTrigger = false;
            punchInImageURI = data.getParcelableExtra("path");
            if (isLastLoginCheck)
                getLocationOfUser();
            else
                CheckLastLogin();
        }else if(requestCode==ATTENDANCE_OUT_IMAGE && resultCode==Activity.RESULT_OK){
            punchOutImageURI = data.getParcelableExtra("path");
            isPunchOut = true;
            isLocationChangeTrigger = false;
            if (isLastLoginCheck)
                getLocationOfUser();
            else
                CheckLastLogin();
        }else if (requestCode == ATTENDANCE_IN_IMAGES2 && resultCode == Activity.RESULT_OK) {
            Log.d("AGENT", "onActivityResult: " + data.getParcelableExtra("path"));
            isPunchInS2 = true;
            isLocationChangeTrigger = false;
            punchInImageURIS2 = data.getParcelableExtra("path");
            if (isLastLoginCheck)
                getLocationOfUser();
            else
                CheckLastLogin();
        }else if(requestCode==ATTENDANCE_OUT_IMAGES2 && resultCode==Activity.RESULT_OK){
            punchOutImageURIS2 = data.getParcelableExtra("path");
            isPunchOutS2 = true;
            isLocationChangeTrigger = false;
            if (isLastLoginCheck)
                getLocationOfUser();
            else
                CheckLastLogin();
        }
    }

    /**
     * Find the Dist between two location
     */
    private void FindDistance(boolean isFromFusedClientConnect) {
        // TODO Auto-generated method stub
        if (objlocation != null) {
            try {
                dbllat = objlocation.getLatitude();
                dbllng = objlocation.getLongitude();

                LatLong latLong = new LatLong();
                latLong.setLat(dbllat);
                latLong.setLng(dbllng);


                if (isLastLoginCheck && !isFromFusedClientConnect)
                    PunchIn_Out(tvAttendanceCurrentTime.getText().toString(), latLong);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Here we handle the PunchIn-Out means which Punch is pressed whether its
     * IN or OUT
     */
    private void PunchIn_Out(final String strPunchTime, LatLong latLong) {
        // TODO Auto-generated method stub


        if (isPunchIn || isPunchOut || isPunchInS2 || isPunchOutS2) {
            String strTitle = "";
            Spanned strMessage;

            if (isPunchIn) {
                strTitle = getString(R.string.alert_title_punch_in);
                strMessage = Html.fromHtml(String.format(getString(R.string.alert_msg_punch_in), strPunchTime));
            }else if(isPunchOut){
                strTitle = getString(R.string.alert_title_punch_out);
                strMessage = Html.fromHtml(String.format(getString(R.string.alert_msg_punch_out), strPunchTime));
            }else if(isPunchInS2){
                strTitle = getString(R.string.alert_title_punch_in);
                strMessage = Html.fromHtml(String.format(getString(R.string.alert_msg_punch_in), strPunchTime));
            }else {
                strTitle = getString(R.string.alert_title_punch_out);
                strMessage = Html.fromHtml(String.format(getString(R.string.alert_msg_punch_out), strPunchTime));
            }


            AlertDialog.Builder alertDialogBuilder =
                    new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.CustomDialogTheme);
            alertDialogBuilder.setTitle(strTitle);
            alertDialogBuilder.setMessage(strMessage)
                    .setPositiveButton(getString(R.string.action_yes), null)
                    .setNegativeButton(getString(R.string.action_no), null);

            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {
                    Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    btnPositive.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                            if (!CommonMethods.isNetworkConnected(Objects.requireNonNull(getActivity()))) {
                                CommonMethods.showConnectionAlert(getActivity());
                                makePunchInOutFalse();
                            } else {
                                if (isPunchIn) {
                                    MakePunch("Punch In", strPunchTime, latLong);
                                } else if (isPunchOut) {
                                    MakePunch("Punch Out", strPunchTime, latLong);
                                }else if(isPunchInS2){
                                    MakePunch("Punch InS2", strPunchTime, latLong);
                                }else if(isPunchOutS2){
                                    MakePunch("Punch OutS2", strPunchTime, latLong);
                                }
                            }
                        }
                    });

                    Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                    btnNegative.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                            makePunchInOutFalse();
                        }
                    });
                }
            });
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        } else
            makePunchInOutFalse();
    }

    private void MakePunch(final String strPunchType, final String strPunchTime, LatLong latLong) {
        // TODO Auto-generated method stub
        if (!isMockLocationEnabled()) {
            Date selectedDate = null;
            try {
                selectedDate = new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US).parse(currentDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }




            AttendanceModel attendanceModel = new AttendanceModel();
            if (strPunchType.equals("Punch In")) {
                attendanceModel.setPersonName(mPersonModel.getName());
                attendanceModel.setPersonMobileNo(mPersonModel.getMobileNo());
                attendanceModel.setPersonFirebaseKey(mPersonModel.getFirebaseKey());
                attendanceModel.setPunchDate(currentDate);
                if (selectedDate != null) {
                    attendanceModel.setPunchDateInMillis(selectedDate.getTime());
                }
                attendanceModel.setPunchInTime(strPunchTime);
                attendanceModel.setPresentDay(-1);
                attendanceModel.setPunchInLocationCode("Mark From AnyWhere");
                attendanceModel.setPunchInLatitude(latLong.getLat());
                attendanceModel.setPunchInLongitude(latLong.getLng());
                attendanceModel.setPunchInBy("User");

                uploadAttendanceDetails(strPunchType, attendanceModel);
            } else if (strPunchType.equals("Punch Out")) {

                attendanceModel.setPersonName(mPersonModel.getName());
                attendanceModel.setPersonMobileNo(mPersonModel.getMobileNo());
                attendanceModel.setPersonFirebaseKey(mPersonModel.getFirebaseKey());
                attendanceModel.setPunchDate(currentDate);
                /**
                 * If mPreviousAttendanceModel=null means user has not do punch in for current day
                 * and direct wants to mark punch out.
                 * So in that case we consider current day as LEAVE as user has not marked punch in
                 */
                if (mPreviousAttendanceModel == null) {
                    if (selectedDate != null) {
                        attendanceModel.setPunchDateInMillis(selectedDate.getTime());
                    }
                } else {
                    attendanceModel.setPunchDateInMillis(mPreviousAttendanceModel.getPunchDateInMillis());
                    attendanceModel.setPunchInTime(mPreviousAttendanceModel.getPunchInTime());
                    attendanceModel.setPunchInLocationCode(mPreviousAttendanceModel.getPunchInLocationCode());
                }
                attendanceModel.setPunchOutTime(strPunchTime);
                attendanceModel.setPunchInLocationCode(mPreviousAttendanceModel.getPunchInLocationCode());
                attendanceModel.setPunchInLatitude(mPreviousAttendanceModel.getPunchInLatitude());
                attendanceModel.setPunchInLongitude(mPreviousAttendanceModel.getPunchInLongitude());
                attendanceModel.setPunchInBy(mPreviousAttendanceModel.getPunchInBy());
                attendanceModel.setPunchInImage(mPreviousAttendanceModel.getPunchInImage());
                attendanceModel.setPunchOutLocationCode("Mark From AnyWhere");
                attendanceModel.setPunchOutLatitude(latLong.getLat());
                attendanceModel.setPunchOutLongitude(latLong.getLng());
                attendanceModel.setPunchOutBy("User");

                String message = "";
                if (mPreviousAttendanceModel != null) {

                    int totalWorkingMinutes = CommonMethods.calculateTotalHours(mPreviousAttendanceModel.getPunchInTime(),
                            strPunchTime);
                    String totalWorkingHours = CommonMethods.get24HoursFromMinutes(totalWorkingMinutes);
                    attendanceModel.setTotalWorkingHours(totalWorkingHours);
                    if (mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_HOUR_WISE)) {
                        attendanceModel.setPresentDay(1);
                    } else {
                        if (mTimeSlotArrayList.size() > 0) {
                            /**
                             * Here we convert current date/punch date into day name 'MONDAY','SUNDAY' format
                             */
                            SimpleDateFormat outFormat = new SimpleDateFormat("EEEE", Locale.US);
                            String currentDayName = outFormat.format(new Date()).toUpperCase();
                            /**
                             * Here we EXTRACT time slot model for particular current date/punch date
                             */
                            ShopTimingModel timeModel = null;
                            for (ShopTimingModel model :
                                    mTimeSlotArrayList) {
                                assert model != null;
                                if (model.getDay().equalsIgnoreCase(currentDayName)) {
                                    timeModel = model;
                                    break;
                                }
                            }
                            if (timeModel != null) {

                                double[] calculatedDay = CommonMethods.getDayTypeForMonthWise(timeModel, mPreviousAttendanceModel.getPunchInTime(),
                                        strPunchTime,isShift2,0);
                                attendanceModel.setOverTimeInMinutes(calculatedDay[1]);
                                if (calculatedDay[0] == 1) {
                                    message = getString(R.string.msg_punch_day_count_as_full_day);
                                } else if (calculatedDay[0] == 0.5) {
                                    message = getString(R.string.msg_punch_day_count_as_Full_Shift);
                                } else if (calculatedDay[0] == 0) {
                                    if (timeModel.isHalfDayAllow()) {
                                        message = getString(R.string.msg_punch_day_count_as_present_but_leave);
                                    } else {
                                        message = getString(R.string.msg_punch_shift_count_as_present_but_leave_);
                                    }
                                }
                                attendanceModel.setPresentDay(calculatedDay[0]);
                            }
                        }
                    }
                } else {
                    attendanceModel.setPresentDay(0);
                    message = getString(R.string.msg_punch_day_count_as_miss_punch);
                }


                if (mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_HOUR_WISE)) {
                    uploadAttendanceDetails(strPunchType, attendanceModel);
                } else {

                    AlertDialog.Builder alertDialogBuilder =
                            new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.CustomDialogTheme);
                    alertDialogBuilder.setTitle(getString(R.string.alert_title_warning));
                    alertDialogBuilder.setMessage(Html.fromHtml(message))
                            .setPositiveButton(getString(R.string.action_mark_punch_out), null)
                            .setNegativeButton(getString(R.string.action_cancel), null);

                    final AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(final DialogInterface dialog) {
                            Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                            btnPositive.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    alertDialog.dismiss();
                                    uploadAttendanceDetails(strPunchType, attendanceModel);
                                }
                            });
                            btnNegative.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    alertDialog.dismiss();
                                    makePunchInOutFalse();
                                }
                            });
                        }
                    });
                    alertDialog.setCancelable(false);
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();
                }
            }else if(strPunchType.equals("Punch InS2")){
                attendanceModel.setPersonName(mPersonModel.getName());
                attendanceModel.setPersonMobileNo(mPersonModel.getMobileNo());
                attendanceModel.setPersonFirebaseKey(mPersonModel.getFirebaseKey());
                attendanceModel.setPunchDate(currentDate);
                /**
                 * If mPreviousAttendanceModel=null means user has not do punch in for current day
                 * and direct wants to mark punch out.
                 * So in that case we consider current day as LEAVE as user has not marked punch in
                 */
                if (mPreviousAttendanceModel == null) {
                    if (selectedDate != null) {
                        attendanceModel.setPunchDateInMillis(selectedDate.getTime());
                    }
                } else {
                    attendanceModel.setPunchDateInMillis(mPreviousAttendanceModel.getPunchDateInMillis());
                    attendanceModel.setPunchInTime(mPreviousAttendanceModel.getPunchInTime());
                    attendanceModel.setPunchInLocationCode(mPreviousAttendanceModel.getPunchInLocationCode());
                }
                attendanceModel.setPunchOutTime(mPreviousAttendanceModel.getPunchOutTime());
                attendanceModel.setPunchInLocationCode(mPreviousAttendanceModel.getPunchInLocationCode());
                attendanceModel.setPunchInLatitude(mPreviousAttendanceModel.getPunchInLatitude());
                attendanceModel.setPunchInLongitude(mPreviousAttendanceModel.getPunchInLongitude());
                attendanceModel.setPunchInBy(mPreviousAttendanceModel.getPunchInBy());
                attendanceModel.setPunchInImage(mPreviousAttendanceModel.getPunchInImage());
                attendanceModel.setPunchOutLocationCode("Mark From AnyWhere");
                attendanceModel.setPunchOutLatitude(mPreviousAttendanceModel.getPunchOutLatitude());
                attendanceModel.setPunchOutLongitude(mPreviousAttendanceModel.getPunchOutLongitude());
                attendanceModel.setPunchOutBy("User");
                attendanceModel.setOverTimeInMinutes(mPreviousAttendanceModel.getOverTimeInMinutes());
                attendanceModel.setPresentDay(mPreviousAttendanceModel.getPresentDay());
                attendanceModel.setTotalWorkingHours(mPreviousAttendanceModel.getTotalWorkingHours());

                attendanceModel.setPunchInTimeS2(strPunchTime);
                attendanceModel.setPunchInLongitudeS2(latLong.getLng());
                attendanceModel.setPunchInLatitudeS2(latLong.getLat());

                uploadAttendanceDetails(strPunchType, attendanceModel);
            }else if(strPunchType.equals("Punch OutS2")){
                attendanceModel.setPersonName(mPersonModel.getName());
                attendanceModel.setPersonMobileNo(mPersonModel.getMobileNo());
                attendanceModel.setPersonFirebaseKey(mPersonModel.getFirebaseKey());
                attendanceModel.setPunchDate(currentDate);
                /**
                 * If mPreviousAttendanceModel=null means user has not do punch in for current day
                 * and direct wants to mark punch out.
                 * So in that case we consider current day as LEAVE as user has not marked punch in
                 */
                if (mPreviousAttendanceModel == null) {
                    if (selectedDate != null) {
                        attendanceModel.setPunchDateInMillis(selectedDate.getTime());
                    }
                } else {
                    attendanceModel.setPunchDateInMillis(mPreviousAttendanceModel.getPunchDateInMillis());
                    attendanceModel.setPunchInTime(mPreviousAttendanceModel.getPunchInTime());
                    attendanceModel.setPunchInLocationCode(mPreviousAttendanceModel.getPunchInLocationCode());
                }
                attendanceModel.setPunchOutTime(mPreviousAttendanceModel.getPunchOutTime());
                attendanceModel.setPunchInLocationCode(mPreviousAttendanceModel.getPunchInLocationCode());
                attendanceModel.setPunchInLatitude(mPreviousAttendanceModel.getPunchInLatitude());
                attendanceModel.setPunchInLongitude(mPreviousAttendanceModel.getPunchInLongitude());
                attendanceModel.setPunchInBy(mPreviousAttendanceModel.getPunchInBy());
                attendanceModel.setPunchInImage(mPreviousAttendanceModel.getPunchInImage());
                attendanceModel.setPunchOutLocationCode("Mark From AnyWhere");
                attendanceModel.setPunchOutLatitude(mPreviousAttendanceModel.getPunchOutLatitude());
                attendanceModel.setPunchOutLongitude(mPreviousAttendanceModel.getPunchOutLongitude());
                attendanceModel.setPunchOutBy("User");
                attendanceModel.setPresentDay(mPreviousAttendanceModel.getPresentDay());
                attendanceModel.setTotalWorkingHours(mPreviousAttendanceModel.getTotalWorkingHours());
                attendanceModel.setOverTimeInMinutes(mPreviousAttendanceModel.getOverTimeInMinutes());
                attendanceModel.setPunchInTimeS2(mPreviousAttendanceModel.getPunchInTimeS2());
                attendanceModel.setPunchInLongitudeS2(mPreviousAttendanceModel.getPunchInLongitudeS2());
                attendanceModel.setPunchInLatitudeS2(mPreviousAttendanceModel.getPunchInLatitudeS2());

                attendanceModel.setPunchOutTimeS2(strPunchTime);
                attendanceModel.setPunchOutLatitudeS2(latLong.getLat());
                attendanceModel.setPunchOutLongitudeS2(latLong.getLng());


                String message = "";
                if (mPreviousAttendanceModel != null) {

                    int totalWorkingMinutes = CommonMethods.calculateTotalHours(mPreviousAttendanceModel.getPunchInTimeS2(),
                            strPunchTime);
                    int totalWorkingMinutesPrevious=CommonMethods.calculateTotalHours(mPreviousAttendanceModel.getPunchInTime(),
                            mPreviousAttendanceModel.getPunchOutTime());
                    String totalWorkingHours = CommonMethods.get24HoursFromMinutes(totalWorkingMinutes+totalWorkingMinutesPrevious);
                    attendanceModel.setTotalWorkingHours(totalWorkingHours);
                    if (mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_HOUR_WISE)) {
                        attendanceModel.setPresentDay(1);
                    } else {
                        if (mTimeSlotArrayList.size() > 0) {
                            /**
                             * Here we convert current date/punch date into day name 'MONDAY','SUNDAY' format
                             */
                            SimpleDateFormat outFormat = new SimpleDateFormat("EEEE", Locale.US);
                            String currentDayName = outFormat.format(new Date()).toUpperCase();
                            /**
                             * Here we EXTRACT time slot model for particular current date/punch date
                             */
                            ShopTimingModel timeModel = null;
                            for (ShopTimingModel model :
                                    mTimeSlotArrayList) {
                                assert model != null;
                                if (model.getDay().equalsIgnoreCase(currentDayName)) {
                                    timeModel = model;
                                    break;
                                }
                            }
                            if (timeModel != null) {
                                double[] calculatedDay = CommonMethods.getDayTypeForMonthWise(timeModel, mPreviousAttendanceModel.getPunchInTimeS2(),
                                        strPunchTime,isShift2,(int)mPreviousAttendanceModel.getOverTimeInMinutes());
                                attendanceModel.setOverTimeInMinutes(calculatedDay[1]);
                                if (calculatedDay[0] == 1) {
                                    message = getString(R.string.msg_punch_day_count_as_full_day);
                                    attendanceModel.setOverTimeInMinutes(calculatedDay[1]+attendanceModel.getOverTimeInMinutes());
                                } else if (calculatedDay[0] == 0.5) {
                                    message = getString(R.string.msg_punch_day_count_as_Full_Shift);
                                } else if (calculatedDay[0] == 0) {
                                    if (timeModel.isHalfDayAllow()) {
                                        message = getString(R.string.msg_punch_day_count_as_present_but_leave);
                                    } else {
                                        message = getString(R.string.msg_punch_shift_count_as_present_but_leave_);
                                    }
                                }
                                calculatedDay[0]=Math.min(calculatedDay[0]+mPreviousAttendanceModel.getPresentDay(),1.0);
                                attendanceModel.setPresentDay(calculatedDay[0]);
                            }
                        }
                    }
                } else {
                    attendanceModel.setPresentDay(0);
                    message = getString(R.string.msg_punch_day_count_as_miss_punch);
                }


                if (mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_HOUR_WISE)) {
                    uploadAttendanceDetails(strPunchType, attendanceModel);
                } else {

                    AlertDialog.Builder alertDialogBuilder =
                            new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.CustomDialogTheme);
                    alertDialogBuilder.setTitle(getString(R.string.alert_title_warning));
                    alertDialogBuilder.setMessage(Html.fromHtml(message))
                            .setPositiveButton(getString(R.string.action_mark_punch_out), null)
                            .setNegativeButton(getString(R.string.action_cancel), null);

                    final AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(final DialogInterface dialog) {
                            Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                            btnPositive.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    alertDialog.dismiss();
                                    uploadAttendanceDetails(strPunchType, attendanceModel);
                                }
                            });
                            btnNegative.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    alertDialog.dismiss();
                                    makePunchInOutFalse();
                                }
                            });
                        }
                    });
                    alertDialog.setCancelable(false);
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();
                }
            }
        } else
            buildAlertMessageOnMockLocation();
    }
    private String getAlphaNumericString(int n)
    {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }


        return sb.toString();
    }
    private void uploadAttendanceDetails(final String strPunchType, AttendanceModel attendanceModel) {
        if (CommonMethods.isNetworkConnected(Objects.requireNonNull(getActivity()))) {
            CommonMethods.showProgressDialog(getActivity());

            //Added By me
            Uri img = null;
            if (strPunchType.equals("Punch In")) {
                img = punchInImageURI;
            }else if(strPunchType.equals("Punch Out")) {
                img = punchOutImageURI;
            }else if(strPunchType.equals("Punch InS2")){
                img=punchInImageURIS2;
            }else {
                img=punchOutImageURIS2;
            }

            String strImageNameInStorage = getAlphaNumericString(10)+"_"+ new SimpleDateFormat(
                    "ddMMMyyyy_HHmmss", Locale.US).format(new Date()).toString() + ".jpg";

            final StorageReference sRef = AttendanceApplication.storageReferenceAttendanceImage
                    .child(monthYear)
                    .child(strImageNameInStorage);
            sRef.putFile(img).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("AGENT", "onSuccess: "+uri.toString());
                            //Old Code
                            DatabaseReference query = null;
                            if (mPreviousAttendanceModel == null) {
                                String attendanceKey = AttendanceApplication.refCompanyUserAttendanceDetails
                                        .child(mPersonModel.getFirebaseKey())
                                        .child(monthYear).push().getKey();
                                assert attendanceKey != null;
                                query = AttendanceApplication.refCompanyUserAttendanceDetails
                                        .child(mPersonModel.getFirebaseKey())
                                        .child(monthYear).child(attendanceKey);
                                attendanceModel.setPunchInImage(uri.toString());
                            } else {
                                query = AttendanceApplication.refCompanyUserAttendanceDetails
                                        .child(mPersonModel.getFirebaseKey())
                                        .child(monthYear).child(mPreviousAttendanceModel.getFirebaseKey());
                                if(strPunchType.equals("Punch Out")) {
                                    attendanceModel.setPunchInImage(mPreviousAttendanceModel.getPunchInImage());
                                    attendanceModel.setPunchOutImage(uri.toString());
                                }else if(strPunchType.equals("Punch InS2")){
                                    attendanceModel.setPunchInImage(mPreviousAttendanceModel.getPunchInImage());
                                    attendanceModel.setPunchOutImage(mPreviousAttendanceModel.getPunchOutImage());
                                    attendanceModel.setPunchInImageS2(uri.toString());
                                }else {
                                    attendanceModel.setPunchInImage(mPreviousAttendanceModel.getPunchInImage());
                                    attendanceModel.setPunchOutImage(mPreviousAttendanceModel.getPunchOutImage());
                                    attendanceModel.setPunchInImageS2(mPreviousAttendanceModel.getPunchInImageS2());
                                    attendanceModel.setPunchOutImageS2(uri.toString());
                                }

                            }

                            query.setValue(attendanceModel)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            CommonMethods.cancelProgressDialog();
                                            SharePreferences.setBool(SharePreferences.KEY_IS_PUNCH, true);

                                            if (getActivity() != null) {
//                                                if (strPunchType.equals("Punch In")) {
//
//                                                    if (!mPersonModel.getWorkType().equalsIgnoreCase(ConstantData.WORK_TYPE_HOUR_WISE)) {
//
//                                                        Date fullDayHours = null;
//                                                        if (mTimeSlotArrayList.size() > 0) {
//                                                            SimpleDateFormat outFormat = new SimpleDateFormat("EEEE", Locale.US);
//                                                            String currentDayName = outFormat.format(new Date()).toUpperCase();
//                                                            ShopTimingModel timeModel = null;
//                                                            for (ShopTimingModel model :
//                                                                    mTimeSlotArrayList) {
//                                                                assert model != null;
//                                                                if (model.getDay().equalsIgnoreCase(currentDayName)) {
//                                                                    timeModel = model;
//                                                                    break;
//                                                                }
//                                                            }
//                                                            if (timeModel != null) {
//                                                                //24 Format
//                                                                String strFullDayHours = timeModel.getHoursForFullDay();
//
//                                                                //https://inducesmile.com/android/schedule-onetime-notification-with-android-workmanager/
//                                                /*String fullDayHoursInMinutes = new SimpleDateFormat("mm", Locale.US)
//                                                .format(fullDayHours);*/
//
//                                                                String[] fullDayHoursSplit = strFullDayHours.split(":");
//                                                                int convertHoursInMinutes = Integer.valueOf(fullDayHoursSplit[0]) * 60;
//                                                                int minutes = Integer.valueOf(fullDayHoursSplit[1]);
//                                                                //Get time before alarm
//                                                                int minutesBeforeAlert = convertHoursInMinutes + minutes;
//                                                                Calendar cal = Calendar.getInstance();
//                                                                cal.add(Calendar.MINUTE, minutesBeforeAlert);
//                                                                long timeInMillis = cal.getTimeInMillis();
//                                                                long alertTime = timeInMillis - System.currentTimeMillis();
//
//                                                                Data data = new Data.Builder()
//                                                                        .putString("UserName", mPersonModel.getName())
//                                                                        .build();
//
//                                                                NotificationHandler.scheduleReminder(alertTime, data,
//                                                                        mPersonModel.getFirebaseKey());
//                                                            }
//                                                        }
//                                                    }
//                                                    Toast.makeText(getActivity(), getString(R.string.msg_punch_in_successfully), Toast.LENGTH_SHORT).show();
//                                                } else if (strPunchType.equals("Punch Out")) {
//                                                    NotificationHandler.cancelReminder(mPersonModel.getFirebaseKey());
//                                                    Toast.makeText(getActivity(), getString(R.string.msg_punch_out_successfully), Toast.LENGTH_SHORT).show();
//                                                }

                                                /**
                                                 * Trigger notification while employee marking their attendance
                                                 */
                                                CommonMethods.sendNotificationForAttendance(getActivity(), mPersonModel, attendanceModel, strPunchType);

                                                if (getActivity() instanceof UserDashboardActivity) {
                                                    ((UserDashboardActivity) getActivity()).onBackPressed();
                                                }
                                            }
                                            //fragmentBecameVisible("RefreshClickLastLogin");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            CommonMethods.cancelProgressDialog();
                                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            //Old Code End
                        }
                    });


                }
            });

            //End


        } else {
            CommonMethods.showConnectionAlert(getActivity());
        }
    }


    private void SetCurrentTime() {
        // TODO Auto-generated method stub
        try {
            long time = new Date().getTime();
            String currentTime = new SimpleDateFormat(ConstantData.TWELVE_HOURS_FORMAT, Locale.US).format(time);

            tvAttendanceCurrentTime.setText(currentTime);
            tvAttendanceCurrentTime.setServerDate(time);
            tvAttendanceCurrentTime.setClockTickEventListener(new OnClockTickChangeEvent() {

                @Override
                public void OnTick(long milliSecond) {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void RefreshClick() {
        isLocationChangeTrigger = false;
        if (isLastLoginCheck) {
            lastLoginCheck(true);

            if (mGoogleApiClient != null) {
                mGoogleApiClient.disconnect();
                mGoogleApiClient.connect();
            }

            makePunchInOutFalse();
            getLocationOfUser();
        } else {
            lastLoginCheck(false);
            CheckLastLogin();
        }
    }


    private void lastLoginCheck(boolean check) {
        if (!check) {
            isLastLoginCheck = false;
            makePunchInOutFalse();
        }
    }

    private void makePunchInOutFalse() {
        // TODO Auto-generated method stub
        isPunchIn = false;
        isPunchOut = false;
    }


    public void onLocationChanged(Location location) {
        if (location != null) {
            mTvCurrentLocationAccuracy.setText(String.format(getString(R.string.label_location_accuracy),
                    location.getAccuracy()));
            mTvCurrentLocationLatitude.setText(String.format("%f", location.getLatitude()));
            mTvCurrentLocationLongitude.setText(String.format("%f", location.getLongitude()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 13));
            if(mPersonModel.getWorkArea()!=null){
                if(mPersonModel.getWorkArea().getBranchCode().equals("0")){
                    isRightLocation=true;
                }else {
                    Location target=new Location("");
                    target.setLatitude(mPersonModel.getWorkArea().getLat());
                    target.setLongitude(mPersonModel.getWorkArea().getLng());
                    Log.d(TAG, "onLocationChanged: "+location.distanceTo(target));
                    if(location.distanceTo(target)<=mPersonModel.getWorkArea().getRadius()){
                        isRightLocation=true;
                    }else {
                        isRightLocation=false;
                    }
                }
            }else {
                isRightLocation=true;
            }
        }

        if (isLocationChangeTrigger) {
            if (location != null) {

                try {
                    dbllat = location.getLatitude();
                    dbllng = location.getLongitude();

                    LatLong latLong = new LatLong();
                    latLong.setLat(dbllat);
                    latLong.setLng(dbllng);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        isLocationChangeTrigger = true;
    }


    private void MakePunchInVisible() {
        imgVAttendancePunchIn.setVisibility(View.VISIBLE);
        imgVAttendancePunchOut.setVisibility(View.GONE);
        imgVAttendancePunchComplete.setVisibility(View.GONE);
        tvAttendancePunchType.setText(getResources().getString(
                R.string.frag_attendance_punch_in_now));
    }
    private void MakeGoToLocation(){
        imgVAttendancePunchIn.setVisibility(View.GONE);
        imgVAttendancePunchOut.setVisibility(View.GONE);
        imgVAttendancePunchComplete
                .setVisibility(View.VISIBLE);
        tvAttendancePunchType.setText("Wrong Location");
    }

    private void MakePunchOutVisible() {

        imgVAttendancePunchIn.setVisibility(View.GONE);
        imgVAttendancePunchOut.setVisibility(View.VISIBLE);
        imgVAttendancePunchComplete.setVisibility(View.GONE);
        tvAttendancePunchType.setText(getResources().getString(
                R.string.frag_attendance_punch_out_now));
    }

    private void MakeGoodByeVisible() {

        imgVAttendancePunchIn.setVisibility(View.GONE);
        imgVAttendancePunchOut.setVisibility(View.GONE);
        imgVAttendancePunchComplete
                .setVisibility(View.VISIBLE);
        tvAttendancePunchType.setText(getResources().getString(
                R.string.frag_attendance_good_bye));
    }

    private boolean isMockLocationEnabled() {
        boolean isMockLocation = false;
        try {
            //if marshmallow
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (objlocation != null
                        && objlocation.isFromMockProvider()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                // in marshmallow this will always return true
                isMockLocation = !android.provider.Settings.Secure.getString(
                        Objects.requireNonNull(getActivity()).getContentResolver(), "mock_location").equals("0");
            }
        } catch (Exception e) {
            return isMockLocation;
        }
        return isMockLocation;
    }


    private void buildAlertMessageOnMockLocation() {
        if (Build.VERSION.SDK_INT >= 23) {
            CommonMethods.showAlertDailogueWithOK(
                    getActivity(),
                    getString(R.string.title_alert),
                    getString(R.string.alert_msg_disable_mock_location),
                    getResources().getString(R.string.action_ok));
        } else {


            AlertDialog.Builder alertDialogBuilder =
                    new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.CustomDialogTheme);
            alertDialogBuilder.setTitle(getString(R.string.title_alert));
            alertDialogBuilder.setMessage(getString(R.string.alert_msg_disable_mock_location))
                    .setPositiveButton(getString(R.string.action_ok), null);

            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {
                    Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    btnPositive.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                            startActivity(new Intent(
                                    android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
                        }
                    });
                }
            });
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }


    private void showDialogForInformation() {

        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.CustomDialogTheme);
        alertDialogBuilder.setTitle(getString(R.string.alert_title_information));
        alertDialogBuilder.setMessage(Html.fromHtml(getString(R.string.alert_message_info_for_mark_attendance_from_anywhere)))
                .setPositiveButton(getString(R.string.action_got_it), null);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnPositive.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    @Override
    public void onConnected(Bundle arg0) {
        // TODO Auto-generated method stub
        if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if ((mGoogleApiClient != null)
                && mGoogleApiClient.isConnected()) {
            objlocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
            if (objlocation != null)
                FindDistance(true);

            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if ((mGoogleApiClient != null) && mGoogleApiClient.isConnected())
            startLocationUpdates();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if ((mGoogleApiClient != null) && mGoogleApiClient.isConnected())
            stopLocationUpdates();
    }

    @Override
    public void onStop() {
        if ((mGoogleApiClient != null) && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        ImagePickerActivity.clearCache(Objects.requireNonNull(getActivity()));
        stopLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if ((mGoogleApiClient != null) && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, MarkAttendanceFromAnyWhereFragment.this);
        }
    }

    private void stopLocationUpdates() {
        if ((mGoogleApiClient != null) && mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
    }

    @Override
    public void onDestroyView() {
        if (mMap != null)
            mMap.clear();
        super.onDestroyView();
    }
}

