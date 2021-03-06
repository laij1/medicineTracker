package com.clinic.anhe.medicinetracker.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.clinic.anhe.medicinetracker.R;
import com.clinic.anhe.medicinetracker.ViewModel.CashFlowViewModel;
import com.clinic.anhe.medicinetracker.adapters.CashflowTodayRecyclerViewAdapter;
import com.clinic.anhe.medicinetracker.adapters.PatientDetailSearchRecyclerViewAdapter;
import com.clinic.anhe.medicinetracker.model.MedicineRecordCardViewModel;
import com.clinic.anhe.medicinetracker.networking.VolleyCallBack;
import com.clinic.anhe.medicinetracker.networking.VolleyController;
import com.clinic.anhe.medicinetracker.networking.VolleyStatus;
import com.clinic.anhe.medicinetracker.utils.ArgumentVariables;
import com.clinic.anhe.medicinetracker.utils.GlobalVariable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CashflowSearchFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private CashflowTodayRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<MedicineRecordCardViewModel> recordList;
    private VolleyController volleyController;
    private GlobalVariable globalVariable;
    private String ip;
    private String port;
    private Context mContext;
    private TextView mDisplay;
    //here is for the select dates layout
    private TextView mSelectStartDate;
    private TextView mSelectEndDate;
    private ImageView mStartSearch;
    private TextView mRevenue;
    private int revenue = 0;
    String url = "";
    private CashFlowViewModel cashFlowViewModel;

    public static CashflowSearchFragment newInstance() {
        CashflowSearchFragment fragment = new CashflowSearchFragment();
        return fragment;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ArgumentVariables.ARG_PATIENT_DETAIL_SEARCH_STARTDATE, mSelectStartDate.getText().toString());
        outState.putString(ArgumentVariables.ARG_PATIENT_DETAIL_SEARCH_ENDDATE, mSelectEndDate.getText().toString());
        outState.putString(ArgumentVariables.ARG_CASHFLOW_SEARCH_REVENUE, mRevenue.getText().toString());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cashflow_search, container, false);

        cashFlowViewModel = ViewModelProviders.of(getParentFragment()).get(CashFlowViewModel.class);

        mContext = view.getContext();

        globalVariable = GlobalVariable.getInstance();
        ip = globalVariable.getIpaddress();
        port = globalVariable.getPort();

        recordList = new ArrayList<>();

        mRevenue = view.findViewById(R.id.cashflow_search_revenue);

        mDisplay = view.findViewById(R.id.cashflow_search_display);
        //TODO: get current day and display
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH) +1;
        String today = "" + c.get(Calendar.YEAR) + "年"
                + month + "月" + c.get(Calendar.DAY_OF_MONTH) + "日" ;

        mDisplay.setText(today);

        //here for the search
        mSelectStartDate = view.findViewById(R.id.cashflow_search_startdate);
        mSelectEndDate = view.findViewById(R.id.cashflow_search_enddate);
        //Calendar cal = Calendar.getInstance();
        Date date = c.getTime();
        String defaultDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);

        if(savedInstanceState != null) {

            mSelectStartDate.setText(savedInstanceState.getString(ArgumentVariables.ARG_PATIENT_DETAIL_SEARCH_STARTDATE));
            mSelectEndDate.setText(savedInstanceState.getString(ArgumentVariables.ARG_PATIENT_DETAIL_SEARCH_ENDDATE));
            mRevenue.setText(savedInstanceState.getString(ArgumentVariables.ARG_CASHFLOW_SEARCH_REVENUE));
        }
        else {
            mSelectStartDate.setText(defaultDate);
            mSelectEndDate.setText(defaultDate);
        }
        mStartSearch = view.findViewById(R.id.cashflow_search_button);

        mSelectStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(mContext, "select start date", Toast.LENGTH_LONG ).show();
                StartDatePickerDialogFragment startDate = StartDatePickerDialogFragment.newInstance(
                        CashflowSearchFragment.this, mContext);
                startDate.show(getFragmentManager(),"startdate");
            }
        });

        mSelectEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(mContext, "select end date", Toast.LENGTH_LONG ).show();
                EndDatePickerDialogFragment endDate = EndDatePickerDialogFragment.newInstance(
                        CashflowSearchFragment.this, mContext);
                endDate.show(getFragmentManager(), "enddate");
            }
        });

        mRecyclerView = view.findViewById(R.id.cashflow_search_recyclerview);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new CashflowTodayRecyclerViewAdapter(cashFlowViewModel, "search");
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);


        mStartSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(mContext, "start search....", Toast.LENGTH_LONG ).show();
                url = "http://" + ip + ":" + port + "/anho/record/charged/rangedate?start=" +
                        mSelectStartDate.getText().toString() + "&end=" + mSelectEndDate.getText().toString();
                refreshRecyclerView();
                parseRecordListData(url, new VolleyCallBack() {
                    @Override
                    public void onResult(VolleyStatus status) {
                        cashFlowViewModel.getSearchListLiveData().setValue(recordList);
                        mAdapter.notifyDataSetChanged();
                        mRevenue.setText(revenue + "");
                    }
                });
            }
        });

        setRetainInstance(true);
        return view;
    }

    public void refreshRecyclerView() {
        recordList.removeAll(recordList);
        revenue = 0;
        cashFlowViewModel.getSearchListLiveData().setValue(recordList);
        mAdapter.notifyDataSetChanged();

    }

    private void parseRecordListData(String url, final VolleyCallBack volleyCallBack) {
        JsonArrayRequest jsonArrayRequest =
                new JsonArrayRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                for(int i = 0; i < response.length(); i++){
                                    JSONObject object = null;
                                    try {
                                        object = response.getJSONObject(i);

                                        String createAt = object.getString("createAt");
                                        Integer rid = object.getInt("rid");
                                        Integer pid = object.getInt("pid");
                                        Integer mid = object.getInt("mid");
                                        String name = object.getString("medicineName");
                                        String pName = object.getString("patientName");
                                        Integer quantity = object.getInt("quantity");
                                        Integer subtotal = object.getInt("subtotal");
                                        String createBy = object.getString("createBy");
                                        String payment = object.getString("payment");
                                        String chargeAt = object.getString("chargeAt");
                                        String chargeBy = object.getString("chargeBy");
                                        MedicineRecordCardViewModel item = new MedicineRecordCardViewModel(rid, createAt, mid, name, quantity,
                                                subtotal, payment, pid, createBy);
                                        item.setChargeAt(chargeAt);
                                        item.setChargeBy(chargeBy);
                                        item.setPatientName(pName);
                                        if(mid >= 5 ) {
                                            revenue += subtotal.intValue();
                                        }
                                        recordList.add(item);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

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

        volleyController.getInstance(mContext).addToRequestQueue(jsonArrayRequest);
    }

    public void setStartDateTextView(String startDate) {
        mSelectStartDate.setText(startDate);
    }

    public void setEndDateTextView(String endDate) {
        mSelectEndDate.setText(endDate);
    }

}
