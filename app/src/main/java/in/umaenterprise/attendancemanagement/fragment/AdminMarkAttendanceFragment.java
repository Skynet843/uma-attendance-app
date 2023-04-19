package in.umaenterprise.attendancemanagement.fragment;

import android.animation.Animator;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import belka.us.androidtoggleswitch.widgets.ToggleSwitch;
import in.umaenterprise.attendancemanagement.R;
import in.umaenterprise.attendancemanagement.activity.AdminDashboardActivity;
import in.umaenterprise.attendancemanagement.application.AttendanceApplication;
import in.umaenterprise.attendancemanagement.model.AttendanceModel;
import in.umaenterprise.attendancemanagement.model.PersonModel;
import in.umaenterprise.attendancemanagement.model.ShopTimingModel;
import in.umaenterprise.attendancemanagement.utils.CommonMethods;
import in.umaenterprise.attendancemanagement.utils.ConstantData;

public class AdminMarkAttendanceFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private double presentDay;
    private TextView mTvDate;
    private String mSelectedDate;
    private String mSelectedMonthYear;
    private SimpleDateFormat actualTimeFormat = new SimpleDateFormat("HH:mm", Locale.US);
    private SimpleDateFormat requiredTimeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
    private AttendanceModel mPreviousAttendanceModel = null;
    private PersonModel mPersonModel;
    private Spinner day_status;
    private String mPreviousSelectedDate="";
    private EditText mEtAdminNote;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem itemList=menu.findItem(R.id.item_list);
        if(itemList!=null)
            itemList.setVisible(false);

        MenuItem itemShare=menu.findItem(R.id.item_share);
        if(itemShare!=null)
            itemShare.setVisible(false);

        MenuItem itemInfo=menu.findItem(R.id.item_info);
        if(itemInfo!=null)
            itemInfo.setVisible(false);

        MenuItem itemShowAttendancePostionOnMap=menu.findItem(R.id.item_show_attendance_position_on_map);
        if(itemShowAttendancePostionOnMap!=null)
            itemShowAttendancePostionOnMap.setVisible(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_mark_attendance, container, false);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        //https://gist.github.com/ferdy182/d9b3525aa65b5b4c468a
        view.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.colorDivider));
        // To run the animation as soon as the view is layout in the view hierarchy we add this
        // listener and remove it
        // as soon as it runs to prevent multiple animations if the view changes bounds
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                       int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                int cx = 20;
                int cy = 20;

                // get the hypothenuse so the radius is from one corner to the other
                int radius = (int) Math.hypot(right, bottom);

                Animator reveal = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, radius);
                reveal.setInterpolator(new DecelerateInterpolator(2f));
                reveal.setDuration(1000);
                reveal.start();
            }
        });


        setToolbar(view);
        init(view);
        return view;
    }

    private void setToolbar(View view) {

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.toolbar_title_admin_update_attendance));
        ((AdminDashboardActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AdminDashboardActivity) getActivity()).getSupportActionBar()).setDisplayShowTitleEnabled(true);
        Objects.requireNonNull(((AdminDashboardActivity) getActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AdminDashboardActivity) Objects.requireNonNull(getActivity())).onBackPressed();
            }
        });
    }

    private void init(View view) {

        if (getArguments() != null) {
            mPersonModel = (PersonModel) getArguments().getSerializable("PersonModel");
            mPreviousSelectedDate =  getArguments().getString("SelectedDate");
        }
        mEtAdminNote=view.findViewById(R.id.etAdminNote);
        RelativeLayout rlSelectDate = view.findViewById(R.id.rl_select_date);
        TextView tvPersonName = view.findViewById(R.id.tv_person_name);
        TextView tvPersonMobilNo = view.findViewById(R.id.tv_person_mobile_no);
        mTvDate = view.findViewById(R.id.tv_date);
        if(mPersonModel!=null) {
            tvPersonName.setText(mPersonModel.getName());
            tvPersonMobilNo.setText(mPersonModel.getMobileNo());
        }

        if (mPreviousSelectedDate != null) {
            mTvDate.setText(mPreviousSelectedDate);
            /**
             * Don't change this.
             * If user come for update attendance for particular day then user is not able to change
             * date.If we allows to give rights to change date then there is a case
             * for duplicate attendance entry for same date
             */
            rlSelectDate.setOnClickListener(null);

            mSelectedDate = mPreviousSelectedDate;

            SimpleDateFormat fullDate = new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US);
            SimpleDateFormat monthYear = new SimpleDateFormat(ConstantData.MONTH_YEAR_FORMAT, Locale.US);

            Date previousSelectedDate = null;
            try {
                previousSelectedDate = fullDate.parse(mPreviousSelectedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (previousSelectedDate != null) {
                mSelectedMonthYear = monthYear.format(previousSelectedDate);
            }
        }

        // Setup Spinner
        day_status=view.findViewById(R.id.spinner_admin_day_status);
        ArrayAdapter<CharSequence> areaAdapter=ArrayAdapter.createFromResource(view.getContext(),R.array.day_type, android.R.layout.simple_spinner_item);
        areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        day_status.setAdapter(areaAdapter);
        day_status.setOnItemSelectedListener(this);
        day_status.setEnabled(false);
        getAttendanceData();
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            default:
                break;
        }
    }



    private void getAttendanceData() {
        mPreviousAttendanceModel = null;
        if (!mSelectedDate.isEmpty()) {

            /**
             * Here we get attendance data for selected date
             */
            AttendanceApplication.refCompanyUserAttendanceDetails
                    .child(mPersonModel.getFirebaseKey())
                    .child(mSelectedMonthYear)
                    .orderByChild("punchDate").equalTo(mSelectedDate)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            CommonMethods.cancelProgressDialog();
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    mPreviousAttendanceModel = ds.getValue(AttendanceModel.class);
                                    assert mPreviousAttendanceModel != null;
                                    mPreviousAttendanceModel.setFirebaseKey(ds.getKey());
                                    mPreviousAttendanceModel.setEditedByAdmin(true);
                                    int val=Math.max(0,(int)(mPreviousAttendanceModel.getPresentDay()*2));
                                    day_status.setSelection(val);
                                    day_status.setEnabled(true);
                                    mPreviousAttendanceModel.setPunchInBy("Admin");
                                    mEtAdminNote.setText(mPreviousAttendanceModel.getAdminNote());
                                }
                            }else{
                                Date selectedDate = null;
                                try {
                                    selectedDate = new SimpleDateFormat(ConstantData.DATE_FORMAT, Locale.US).parse(mSelectedDate);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                mPreviousAttendanceModel = new AttendanceModel();
                                mPreviousAttendanceModel.setPersonName(mPersonModel.getName());
                                mPreviousAttendanceModel.setPersonMobileNo(mPersonModel.getMobileNo());
                                mPreviousAttendanceModel.setPersonFirebaseKey(mPersonModel.getFirebaseKey());
                                mPreviousAttendanceModel.setPunchDate(mSelectedDate);
                                if (selectedDate != null) {
                                    mPreviousAttendanceModel.setPunchDateInMillis(selectedDate.getTime());
                                }
                                mPreviousAttendanceModel.setOverTimeInMinutes(0);
                                mPreviousAttendanceModel.setPresentDay(0);
                                mPreviousAttendanceModel.setPunchInLocationCode("Mark From AnyWhere");
                                mPreviousAttendanceModel.setPunchInBy("Admin");
                                mPreviousAttendanceModel.setEditedByAdmin(true);
                                day_status.setSelection(0);
                                day_status.setEnabled(true);

                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            CommonMethods.cancelProgressDialog();
                            CommonMethods.showAlertDailogueWithOK(getActivity(), getString(R.string.title_alert),
                                    "Issue while fetching attendance : " + databaseError.getMessage(), getString(R.string.action_ok));
                        }
                    });
        }
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_submit, menu);
        menu.findItem(R.id.item_info).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_submit) {
            mPreviousAttendanceModel.setPresentDay(presentDay);
            mPreviousAttendanceModel.setType(type);
            mPreviousAttendanceModel.setAdminNote(mEtAdminNote.getText().toString());
            uploadAttendanceDetails(mPreviousAttendanceModel);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadAttendanceDetails(final AttendanceModel attendanceModel) {


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Objects.requireNonNull(getActivity())
                ,R.style.CustomDialogTheme);
        alertDialogBuilder.setTitle(getString(R.string.alert_title_mark_attendance));
        alertDialogBuilder.setMessage(getString(R.string.alert_message_mark_attendance))
                .setPositiveButton(R.string.action_yes, null)
                .setNegativeButton(R.string.action_no, null);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnPositive.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();

                        if (CommonMethods.isNetworkConnected(Objects.requireNonNull(getActivity()))) {
                            CommonMethods.showProgressDialog(getActivity());

                            Query query = null;
                            if (mPreviousAttendanceModel.getFirebaseKey() == null) {
                                String attendanceKey = AttendanceApplication.refCompanyUserAttendanceDetails
                                        .child(mPersonModel.getFirebaseKey())
                                        .child(mSelectedMonthYear).push().getKey();
                                assert attendanceKey != null;
                                query = AttendanceApplication.refCompanyUserAttendanceDetails
                                        .child(mPersonModel.getFirebaseKey())
                                        .child(mSelectedMonthYear).child(attendanceKey);
                            } else {
                                query = AttendanceApplication.refCompanyUserAttendanceDetails
                                        .child(mPersonModel.getFirebaseKey())
                                        .child(mSelectedMonthYear).child(mPreviousAttendanceModel.getFirebaseKey());
                            }

                            ((DatabaseReference) query).setValue(attendanceModel)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            CommonMethods.cancelProgressDialog();
                                            if (mPreviousAttendanceModel == null) {
                                                Toast.makeText(getActivity(), getString(R.string.msg_attendance_added_successfully), Toast.LENGTH_SHORT).show();
                                            }else{
                                                Toast.makeText(getActivity(), getString(R.string.msg_attendance_updated_successfully), Toast.LENGTH_SHORT).show();
                                            }

                                            if (getActivity() != null) {
                                                //0 is Branch List,1 is User List, 2 is Calender View and 3 Mark Attendance means current fragment
                                                if (getActivity().getSupportFragmentManager().getBackStackEntryCount() > 2) {
                                                    FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(2);
                                                    String tag = backEntry.getName();
                                                    Fragment fragment = getFragmentManager().findFragmentByTag(tag);
                                                    /**
                                                     * Here we refresh attendance history data again
                                                     */
                                                    if (fragment instanceof UserAttendanceHistoryInCalendarFragment) {
                                                        ((UserAttendanceHistoryInCalendarFragment)fragment).getData();
                                                    }
                                                }
                                                if (getActivity() instanceof AdminDashboardActivity) {
                                                    ((AdminDashboardActivity) getActivity()).onBackPressed();
                                                }
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            CommonMethods.cancelProgressDialog();
                                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            CommonMethods.showConnectionAlert(getActivity());
                        }
                    }
                });

                Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                btnNegative.setOnClickListener(new View.OnClickListener() {
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

    String type="Absent Day";
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 0:
                presentDay=0;
                type="Absent Day";
                break;
            case 1:
                presentDay=0.5;
                type="Half Day";
                break;
            case 2:
                presentDay=1;
                type="Present Day";
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}