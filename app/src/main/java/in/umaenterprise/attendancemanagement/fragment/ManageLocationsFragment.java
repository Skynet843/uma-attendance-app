package in.umaenterprise.attendancemanagement.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import in.umaenterprise.attendancemanagement.R;
import in.umaenterprise.attendancemanagement.adapter.LocationsAdapter;
import in.umaenterprise.attendancemanagement.application.AttendanceApplication;
import in.umaenterprise.attendancemanagement.model.LatLong;
import in.umaenterprise.attendancemanagement.model.LocationModel;
import in.umaenterprise.attendancemanagement.model.PersonModel;
import in.umaenterprise.attendancemanagement.utils.CommonMethods;
import in.umaenterprise.attendancemanagement.utils.ConstantData;

public class ManageLocationsFragment extends Fragment {

    private RecyclerView mRvLocations;
    private TextView mTvNoLocations;
    private LocationsAdapter mAdapter;
    private ArrayList<LocationModel> mLocationList;
    private ValueEventListener valueEventListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_locations, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.toolbar_title_manage_locations));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });

        mRvLocations = view.findViewById(R.id.rv_locations);
        mTvNoLocations = view.findViewById(R.id.tv_no_locations);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_location);

        mLocationList = new ArrayList<>();
        mRvLocations.setLayoutManager(new LinearLayoutManager(getActivity()));
        
        mAdapter = new LocationsAdapter(getActivity(), mLocationList, new LocationsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(LocationModel model) {
                // Do nothing on click
            }

            @Override
            public void onEditClick(LocationModel model) {
                showAddUpdateLocationDialog(model, true);
            }

            @Override
            public void onDeleteClick(LocationModel model) {
                confirmDeleteLocation(model);
            }
        });
        mRvLocations.setAdapter(mAdapter);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddUpdateLocationDialog(new LocationModel(), false);
            }
        });

        fetchLocations();
    }

    private void fetchLocations() {
        CommonMethods.showProgressDialog(getActivity());
        valueEventListener = AttendanceApplication.refCompanyLocations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                CommonMethods.cancelProgressDialog();
                mLocationList.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        LocationModel model = snapshot.getValue(LocationModel.class);
                        if (model != null) {
                            model.setFirebaseKey(snapshot.getKey());
                            mLocationList.add(model);
                        }
                    }
                }
                
                if (mLocationList.isEmpty()) {
                    mTvNoLocations.setVisibility(View.VISIBLE);
                    mRvLocations.setVisibility(View.GONE);
                } else {
                    mTvNoLocations.setVisibility(View.GONE);
                    mRvLocations.setVisibility(View.VISIBLE);
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                CommonMethods.cancelProgressDialog();
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddUpdateLocationDialog(final LocationModel model, final boolean isUpdate) {
        // Let's create an AlertDialog directly
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.CustomDialogTheme);
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_update_location, null);
        alertDialogBuilder.setView(dialogView);
        alertDialogBuilder.setTitle(isUpdate ? getString(R.string.alert_title_update_location) : getString(R.string.alert_title_add_branch));
        
        final EditText adEtName = dialogView.findViewById(R.id.et_company_branch_name);
        final EditText adEtAddress = dialogView.findViewById(R.id.et_company_branch_address);
        final EditText adEtLat = dialogView.findViewById(R.id.et_company_branch_latitude);
        final EditText adEtLng = dialogView.findViewById(R.id.et_company_branch_longitude);
        final EditText adEtRadius = dialogView.findViewById(R.id.et_company_branch_radius);
        final EditText adEtCode = dialogView.findViewById(R.id.et_company_branch_code);
        // Branch code is always read-only — auto-generated on add, locked on edit

        if (isUpdate) {
            adEtName.setText(model.getName());
            adEtAddress.setText(model.getAddress());
            adEtLat.setText(String.valueOf(model.getLatitude()));
            adEtLng.setText(String.valueOf(model.getLongitude()));
            adEtRadius.setText(String.valueOf((int) model.getRadius()));
            adEtCode.setText(model.getCode());
        } else {
            // Auto-generate unique branch code for new location
            String nextCode = generateNextBranchCode();
            adEtCode.setText(nextCode);
            model.setCode(nextCode);
        }
        
        alertDialogBuilder.setPositiveButton(getString(R.string.action_save), null);
        alertDialogBuilder.setNegativeButton(getString(R.string.action_cancel), null);
        
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = adEtName.getText().toString().trim();
                        String address = adEtAddress.getText().toString().trim();
                        String latStr = adEtLat.getText().toString().trim();
                        String lngStr = adEtLng.getText().toString().trim();
                        String radiusStr = adEtRadius.getText().toString().trim();
                        
                        if (name.isEmpty() || address.isEmpty() || latStr.isEmpty() || lngStr.isEmpty() || radiusStr.isEmpty()) {
                            Toast.makeText(getActivity(), getString(R.string.msg_all_fields_required), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        double lat = Double.parseDouble(latStr);
                        double lng = Double.parseDouble(lngStr);
                        float radius = Float.parseFloat(radiusStr);
                        
                        if (radius < 0 || radius > 1000) {
                            Toast.makeText(getActivity(), getString(R.string.msg_enter_valid_radius), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        model.setName(name);
                        model.setAddress(address);
                        model.setLatitude(lat);
                        model.setLongitude(lng);
                        model.setRadius((int) radius);
                        // code is preserved from existing (edit) or regenerated at save time (add)
                        if (!isUpdate) {
                            model.setCode(generateNextBranchCode());
                        }
                        
                        if (isUpdate) {
                            AttendanceApplication.refCompanyLocations.child(model.getFirebaseKey()).setValue(model);
                            Toast.makeText(getActivity(), getString(R.string.msg_location_updated_successfully), Toast.LENGTH_SHORT).show();
                        } else {
                            String key = AttendanceApplication.refCompanyLocations.push().getKey();
                            if (key != null) {
                                AttendanceApplication.refCompanyLocations.child(key).setValue(model);
                                Toast.makeText(getActivity(), getString(R.string.msg_location_added_successfully), Toast.LENGTH_SHORT).show();
                            }
                        }
                        alertDialog.dismiss();
                    }
                });
            }
        });
        alertDialog.show();
    }

    private void confirmDeleteLocation(final LocationModel model) {
        new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.CustomDialogTheme)
                .setTitle(getString(R.string.alert_title_delete_location))
                .setMessage(getString(R.string.alert_message_delete_location))
                .setPositiveButton(getString(R.string.action_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reassignEmployeesAndDeleteLocation(model);
                    }
                })
                .setNegativeButton(getString(R.string.action_no), null)
                .show();
    }

    private void reassignEmployeesAndDeleteLocation(final LocationModel model) {
        CommonMethods.showProgressDialog(getActivity());
        final String deletedCode = model.getCode();

        AttendanceApplication.refCompanyUserDetails
                .orderByChild("userType").equalTo(ConstantData.TYPE_USER)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, Object> updates = new HashMap<>();
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                PersonModel person = ds.getValue(PersonModel.class);
                                if (person != null && person.getWorkArea() != null
                                        && deletedCode != null
                                        && deletedCode.equals(person.getWorkArea().getBranchCode())) {
                                    // Build Any Area sentinel for this employee
                                    Map<String, Object> workAreaUpdate = new HashMap<>();
                                    workAreaUpdate.put("branchName", "Any Area");
                                    workAreaUpdate.put("branchCode", "0");
                                    workAreaUpdate.put("lat", 0);
                                    workAreaUpdate.put("lng", 0);
                                    workAreaUpdate.put("radius", 0);
                                    updates.put(ds.getKey() + "/workArea", workAreaUpdate);
                                }
                            }
                        }

                        if (!updates.isEmpty()) {
                            // Batch update all affected employees, then delete location
                            AttendanceApplication.refCompanyUserDetails
                                    .updateChildren(updates)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            deleteLocation(model);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            CommonMethods.cancelProgressDialog();
                                            Toast.makeText(getActivity(),
                                                    "Failed to reassign employees: " + e.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            // No employees to reassign, delete directly
                            deleteLocation(model);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        CommonMethods.cancelProgressDialog();
                        Toast.makeText(getActivity(),
                                "Failed to fetch employees: " + databaseError.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void deleteLocation(final LocationModel model) {
        AttendanceApplication.refCompanyLocations.child(model.getFirebaseKey()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        CommonMethods.cancelProgressDialog();
                        Toast.makeText(getActivity(),
                                getString(R.string.msg_location_deleted_successfully),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        CommonMethods.cancelProgressDialog();
                        Toast.makeText(getActivity(),
                                "Failed to delete location: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String generateNextBranchCode() {
        int maxCode = 0;
        for (LocationModel loc : mLocationList) {
            String code = loc.getCode();
            if (code != null && !code.isEmpty()) {
                try {
                    int num = Integer.parseInt(code);
                    if (num > maxCode) {
                        maxCode = num;
                    }
                } catch (NumberFormatException ignored) {
                    // skip non-numeric codes
                }
            }
        }
        return String.valueOf(maxCode + 1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (valueEventListener != null) {
            AttendanceApplication.refCompanyLocations.removeEventListener(valueEventListener);
        }
    }
}
