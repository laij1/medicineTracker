package com.clinic.anhe.medicinetracker.adapters;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.clinic.anhe.medicinetracker.R;
import com.clinic.anhe.medicinetracker.ViewModel.DashboardViewModel;
import com.clinic.anhe.medicinetracker.ViewModel.SelectedPatientViewModel;
import com.clinic.anhe.medicinetracker.fragments.SelectPatientFragment;
import com.clinic.anhe.medicinetracker.fragments.SelectPatientsDialogFragment;
import com.clinic.anhe.medicinetracker.model.EmployeeCardViewModel;
import com.clinic.anhe.medicinetracker.model.PatientsCardViewModel;
import com.clinic.anhe.medicinetracker.model.ShiftRecordModel;
import com.clinic.anhe.medicinetracker.networking.VolleyCallBack;
import com.clinic.anhe.medicinetracker.networking.VolleyController;
import com.clinic.anhe.medicinetracker.networking.VolleyStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardRecyclerViewAdapter extends RecyclerView.Adapter<DashboardRecyclerViewAdapter.EmployeeViewHolder>{

    private List<EmployeeCardViewModel> employeeList;
    private Context mContext;
    private Fragment mFragment;
    private DashboardViewModel dashboardViewModel;
    private List<ShiftRecordModel> shiftList;
    private VolleyController volleyController;

    public DashboardRecyclerViewAdapter(List<EmployeeCardViewModel> employeeList, Fragment mFragment, DashboardViewModel dashboardViewModel){
        this.employeeList = employeeList;
        this.mFragment = mFragment;
        this.dashboardViewModel = dashboardViewModel;
        this.shiftList = new ArrayList<>();
    }


    @NonNull

    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.cardview_nurse_simple, parent, false);
                    .inflate(R.layout.cardview_dashboard_nurse, parent, false);

        mContext = view.getContext();

        DashboardRecyclerViewAdapter.EmployeeViewHolder employeeViewHolder = new DashboardRecyclerViewAdapter.EmployeeViewHolder(view);
        return employeeViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        //when position ==1
        Log.d("holder positon", ""+ position);
        EmployeeCardViewModel current = employeeList.get(position);
        holder.mNurseName.setText(current.getEmployeeName());

        prepareShiftRecordData(holder, current);

    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    public class EmployeeViewHolder extends RecyclerView.ViewHolder {
        private TextView mNurseName;
        private RecyclerView mRecyclerView;
        private DashboardPatientAssignViewAdapter mAdapter;
        private LinearLayoutManager mLayoutManager;
        private List<String> patientAssignList;

        public EmployeeViewHolder(View itemView) {
            super(itemView);
            mNurseName = itemView.findViewById(R.id.dashboard_nursename);
            mRecyclerView = itemView.findViewById(R.id.dashboard_patient_assign_recyclerview);
            //list should get from shift_record

            patientAssignList = new ArrayList<>();

            mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
            mAdapter = new DashboardPatientAssignViewAdapter(patientAssignList);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(mLayoutManager);
            Log.d("how often does view holder constructor being called", "Chloe");


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EmployeeCardViewModel current = employeeList.get(getAdapterPosition());
                    // if the livedata nurse is the same as the ui, show patient list live data
                    if(current.getEmployeeName()
                            .equalsIgnoreCase(dashboardViewModel.getNurseLiveData().getValue())) {
                        //do nothing
                    } else {  // else remove patient list data
                        dashboardViewModel.getNurseLiveData().setValue(current.getEmployeeName());
                        dashboardViewModel.getSelectedPatientsList().removeAll(dashboardViewModel.getSelectedPatientsList());
                        dashboardViewModel.getSelectedPatientsLiveData().setValue(dashboardViewModel.getSelectedPatientsList());
                    }

                    SelectPatientsDialogFragment fragment = SelectPatientsDialogFragment.newInstance(
                            employeeList.get(getAdapterPosition()).getEmployeeName(), mAdapter, patientAssignList);
                    fragment.show(mFragment.getFragmentManager(), "selectdashboardpatients");


                }
            });
        }
    }


    private void prepareShiftRecordData(EmployeeViewHolder holder, EmployeeCardViewModel current) {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String url = "http://192.168.0.4:8080/anhe/shift/record?createAt=" + date;
        parseShiftRecordData(url, new VolleyCallBack() {
            @Override
            public void onResult(VolleyStatus status) {
                if(status==VolleyStatus.SUCCESS) {
                    for(ShiftRecordModel s: shiftList) {
                        Log.d("nurse is: " + s.getNurse(), "adding to patient Assign List" + s.getPatient() );
                        if(s.getNurse().equalsIgnoreCase(current.getEmployeeName())) {
                            if(!holder.patientAssignList.contains(s.getPatient())) {
                                holder.patientAssignList.add(s.getPatient());
                            }
                        }
                    }
                    holder.mAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    private void parseShiftRecordData(String url, final VolleyCallBack volleyCallBack) {
        JsonArrayRequest jsonArrayRequest =
                new JsonArrayRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                for(int i = 0; i < response.length(); i++) {
                                    JSONObject object = null;
                                    try {
                                        object = response.getJSONObject(i);
                                        String nurse = object.getString("nurse");
                                        Integer sid = object.getInt("sid");
                                        String patient = object.getString("patient");
                                        String shift = object.getString("shift");
                                        String day = object.getString("day");
                                        String createAt = object.getString("createAt");
                                        shiftList.add(new ShiftRecordModel(sid, createAt, nurse, patient,shift, day));
                                        Log.d("getting shift record", nurse + patient);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                volleyCallBack.onResult(VolleyStatus.SUCCESS);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("VOLLEY", error.toString());
                                volleyCallBack.onResult(VolleyStatus.FAIL);
                            }
                        } );

        volleyController.getInstance(mContext).addToRequestQueue(jsonArrayRequest);

    }
}
