package com.clinic.anhe.medicinetracker.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.clinic.anhe.medicinetracker.R;
import com.clinic.anhe.medicinetracker.model.PatientsCardViewModel;
import com.clinic.anhe.medicinetracker.model.ShiftRecordModel;
import com.clinic.anhe.medicinetracker.networking.VolleyCallBack;
import com.clinic.anhe.medicinetracker.utils.DayType;
import com.clinic.anhe.medicinetracker.utils.GlobalVariable;
import com.clinic.anhe.medicinetracker.utils.Shift;
import com.clinic.anhe.medicinetracker.networking.VolleyCallBack;
import com.clinic.anhe.medicinetracker.networking.VolleyController;
import com.clinic.anhe.medicinetracker.networking.VolleyStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddPatientDialogFragment extends DialogFragment {

    private RadioGroup mDayRadioGroup;
    private RadioGroup mShiftGroup;

    private EditText mPatientName;
    private EditText mPatientIC;

    private TextView mConfirmButton;
    private TextView mCancelButton;

    private static PatientListDayFragment parent;
    private Context mContext;
    private VolleyController volleyController;
    private GlobalVariable globalVariable;
    private String day="";
    private String shift="";
    private List<PatientsCardViewModel> patientList = new ArrayList<>();

    public static AddPatientDialogFragment newInstance(PatientListDayFragment parentFrag) {
        AddPatientDialogFragment fragment = new AddPatientDialogFragment();
        parent= parentFrag;
        return fragment;
    }

    @Override
    public void onDestroyView() {
//        Log.d("OnDestoryView", "Chloe");
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_patient, container, false);

        mContext = view.getContext();

        preparePatientData();

        mDayRadioGroup = view.findViewById(R.id.add_patient_day_radiogroup);
        mShiftGroup = view.findViewById(R.id.add_patient_shift_radiogroup);
        mPatientName = view.findViewById(R.id.add_patient_name);
        mPatientIC = view.findViewById(R.id.add_patient_ic);

        mConfirmButton = view.findViewById(R.id.add_patient_confirmbutton);
        mCancelButton = view.findViewById(R.id.add_patient_cancelbutton);

        mDayRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.add_patient_oddday:
                        day = "一三五";
                        break;
                    case R.id.add_patient_evenday:
                        day = "二四六";
                        break;
                }
            }
        });

        mShiftGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.add_patient_morning:
                        shift = Shift.morning.toString();
                        break;
                    case R.id.add_patient_afternoon:
                        shift = Shift.afternoon.toString();
                        break;
                    case R.id.add_patient_night:
                        shift = Shift.night.toString();
                        break;
                }
            }
        });

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: first check if day and shift is clicked
                if(day.equals("")) {
                    Toast.makeText(mContext, "請選擇週時", Toast.LENGTH_SHORT).show();
                } else if(shift.equals("")) {
                    Toast.makeText(mContext, "請選擇班次", Toast.LENGTH_SHORT).show();
                } else {
                    boolean existed = false;
                    //here check if the patient already existed
                    for (PatientsCardViewModel p : patientList) {
                        if(p.getPatientName().equalsIgnoreCase(mPatientName.getText().toString())
                                && p.getPatientIC().equalsIgnoreCase(mPatientIC.getText().toString())) {
                            Toast.makeText(mContext,"病患已存在",Toast.LENGTH_SHORT).show();
                            existed = true;
                            dismiss();
                        }
                    }
                    if(!existed) {
                        addPatientToDatabase(new VolleyCallBack() {
                            @Override
                            public void onResult(VolleyStatus status) {
                                if (status == VolleyStatus.SUCCESS) {
                                    Toast.makeText(mContext, "加入病患成功", Toast.LENGTH_SHORT).show();
                                    parent.refreshrecyclerView();
                                    dismiss();
                                }
                            }
                        });
                    }
                }

            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_round);

        }

        setRetainInstance(true);
        return view;

    }


    public void addPatientToDatabase(final VolleyCallBack volleyCallBack) {

        String url = "http://" + globalVariable.getInstance().getIpaddress() +
                ":" + globalVariable.getInstance().getPort() + "/anho/patient/add?name=" + mPatientName.getText().toString()
                + "&ic=" + mPatientIC.getText().toString() + "&day=" + day + "&shift=" + shift;
        StringRequest stringRequest =
                new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if(response.toString().equals("saved")) {
                                    volleyCallBack.onResult(VolleyStatus.SUCCESS);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("VOLLEY", error.toString());
                                volleyCallBack.onResult(VolleyStatus.FAIL);
                            }
                        } ){/**
                 * Passing some request headers
                 */
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    String credentials = "admin1:secret1";
                    String auth = "Basic "
                            + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", auth);
                    return headers;
                }};

        volleyController.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    private void preparePatientData() {
        String url ="http://" + globalVariable.getInstance().getIpaddress() +
        ":" + globalVariable.getInstance().getPort() +"/anho/patient/all";
        parsePatientData(url, new VolleyCallBack() {
            @Override
            public void onResult(VolleyStatus status) {
                if(status==VolleyStatus.SUCCESS) {
                }
            }
        });

    }

    private void parsePatientData(String url, final VolleyCallBack volleyCallBack) {
        JsonArrayRequest jsonArrayRequest =
                new JsonArrayRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                for(int i = 0; i < response.length(); i++){
                                    JSONObject object = null;
                                    try {
                                        object = response.getJSONObject(i);
                                        Integer pid = object.getInt("pid");
                                        String name = object.getString("name");
                                        String shift = object.getString("shift");
                                        String ic = object.getString("ic");
                                        String day = object.getString("day");
                                        boolean deleted = object.getBoolean("deleted");
//                                        Log.d("patient jason object" , name + pid + shift + day + ic);
                                        PatientsCardViewModel patient = new PatientsCardViewModel(pid, name, ic, shift, day,deleted);
                                        //here we don't check where the patient is deleted, becoz we want to avoid dup record
                                        //reverse patient undeleted has to be manual
                                        if(!patientList.contains(patient)) {
                                            patientList.add(patient);
                                        }
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
                                //    Log.d("VOLLEY", error.toString());
                                volleyCallBack.onResult(VolleyStatus.FAIL);
                            }
                        } ){/**
                 * Passing some request headers
                 */
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    String credentials = "admin1:secret1";
                    String auth = "Basic "
                            + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", auth);
                    return headers;
                }};

        volleyController.getInstance(mContext).addToRequestQueue(jsonArrayRequest);

    }
}
