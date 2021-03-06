package com.clinic.anhe.medicinetracker.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.inputmethod.InputMethodManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.clinic.anhe.medicinetracker.R;
import com.clinic.anhe.medicinetracker.ViewModel.CashFlowViewModel;
import com.clinic.anhe.medicinetracker.adapters.CashflowTodayRecyclerViewAdapter;
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

public class CashflowMonthFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private CashflowTodayRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<MedicineRecordCardViewModel> recordList;
    private List<MedicineRecordCardViewModel> lastmonthRecordList;
    private TextView mSelectEndDate;
    private ImageView mStartSearch;
    private FloatingActionButton mAddFinanceRecordFAB;

    private int total = 0;
    private TextView mTotal;
    private EditText mActualCash;
    private ImageView mDifferenceButton;
    private boolean differenceButtonEnabled;
    private int lastmonthTotal = 0;
    private TextView mLastMonthTotal;

    private int allowance = 0;
    private int bank = 0;
    private TextView mAllowanceTotal;
    private TextView mBankTotal;

    private String firstDay;
    private String lastmonthFirstDay;
    private String lastmonthLastDay;
    private TextView mDisplay;
    private Context mContext;
    private CashFlowViewModel cashFlowViewModel;
    String url = "";
    private VolleyController volleyController;
    private GlobalVariable globalVariable;


    public static CashflowMonthFragment newInstance(){
        CashflowMonthFragment fragment = new CashflowMonthFragment();
        return fragment;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ArgumentVariables.ARG_PATIENT_DETAIL_SEARCH_ENDDATE, mSelectEndDate.getText().toString());
        outState.putBoolean(ArgumentVariables.ARG_DIFFERENCEBUTTON, differenceButtonEnabled);
        outState.putString(ArgumentVariables.ARG_ACTUAL_CASH, mActualCash.getText().toString());
        outState.putString(ArgumentVariables.ARG_CASHFLOW_MONTH_TOTAL, mTotal.getText().toString());
        outState.putString(ArgumentVariables.ARG_CASHFLOW_LASTMONTH_TOTAL, mLastMonthTotal.getText().toString() );
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cashflow_month, container,false);

        Calendar c = Calendar.getInstance();
        Date date = c.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String defaultDate = simpleDateFormat.format(date);

        //calucate last month total
        Calendar lastmonth = Calendar.getInstance();
        lastmonth.add(Calendar.MONTH, -1);
        lastmonth.set(Calendar.DAY_OF_MONTH, 1);
        Date lastmonthDate = lastmonth.getTime();
        lastmonthFirstDay = simpleDateFormat.format(lastmonthDate);

        lastmonth.set(Calendar.DAY_OF_MONTH, lastmonth.getActualMaximum(Calendar.DAY_OF_MONTH));
        lastmonthDate = lastmonth.getTime();
        lastmonthLastDay = simpleDateFormat.format(lastmonthDate);

        //getLastMonthRecordList();
        lastmonthRecordList = new ArrayList<>();
        new MyTask().execute("");

        mSelectEndDate = view.findViewById(R.id.cashflow_month_enddate);
        mTotal = view.findViewById(R.id.cashflow_month_total);
        mBankTotal = view.findViewById(R.id.cashflow_month_bank_total);
        mAllowanceTotal = view.findViewById(R.id.cashflow_month_allowance_total);
        mActualCash = view.findViewById(R.id.cashflow_month_actualcash);
        mAddFinanceRecordFAB = view.findViewById(R.id.add_finance_record_fab);
        mLastMonthTotal = view.findViewById(R.id.cashflow_month_lastmonth_total);


        if(savedInstanceState != null) {
            mSelectEndDate.setText(savedInstanceState.getString(ArgumentVariables.ARG_PATIENT_DETAIL_SEARCH_ENDDATE));
            differenceButtonEnabled = savedInstanceState.getBoolean(ArgumentVariables.ARG_DIFFERENCEBUTTON);
            mActualCash.setText(savedInstanceState.getString(ArgumentVariables.ARG_ACTUAL_CASH));
            mTotal.setText(savedInstanceState.getString(ArgumentVariables.ARG_CASHFLOW_MONTH_TOTAL));
            mLastMonthTotal.setText(savedInstanceState.getString(ArgumentVariables.ARG_CASHFLOW_LASTMONTH_TOTAL));
            recordList = cashFlowViewModel.getMonthListLiveData().getValue();

        }
        else {
            mSelectEndDate.setText(defaultDate);
            differenceButtonEnabled = false;
            recordList = new ArrayList<>();

        }

        cashFlowViewModel = ViewModelProviders.of(getParentFragment()).get(CashFlowViewModel.class);

        mContext = view.getContext();
        View keyboardView = view.findFocus();
        if (keyboardView != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        //if recordlist live data size > 0, enable calculated diff button
        if(cashFlowViewModel.getMonthListLiveData().getValue().size() > 0) {
            enableDifferenceButton();
        }

        int month = c.get(Calendar.MONTH) + 1;

        mDisplay = view.findViewById(R.id.cashflow_month_display);
        String today = "" + c.get(Calendar.YEAR) + "年"
                + month  + "月份結算" ;
        mDisplay.setText(today);

        //set first day of the the month
        c.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDayOfMonth = c.getTime();
        firstDay = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(firstDayOfMonth);

        mSelectEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(mContext, "select end date", Toast.LENGTH_LONG ).show();
                EndDatePickerDialogFragment endDate = EndDatePickerDialogFragment.newInstance(
                        CashflowMonthFragment.this, mContext);
                endDate.show(getFragmentManager(), "enddate");
            }
        });

        mDifferenceButton = view.findViewById(R.id.cashflow_month_difference_button);
        mDifferenceButton.setEnabled(differenceButtonEnabled);
        if(!differenceButtonEnabled) {
            mDifferenceButton.setColorFilter(getResources().getColor(R.color.menuTextIconColor));
        } else {
            mDifferenceButton.setColorFilter(getResources().getColor(R.color.colorPrimaryDark));
        }

        mStartSearch = view.findViewById(R.id.cashflow_month_searchbutton);

        mRecyclerView = view.findViewById(R.id.cashflow_month_recyclerview);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new CashflowTodayRecyclerViewAdapter(cashFlowViewModel, "month");
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mDifferenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View keyboardView = getView().findFocus();
                if (keyboardView != null) {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                int actual = Integer.parseInt(mActualCash.getText().toString());
                int diff = actual - total;
                url = "http://" + globalVariable.getInstance().getIpaddress() + ":" + globalVariable.getInstance().getPort()
                        + "/anho/record/actualcash?cash=" + actual + "&diff=" + diff
                        + "&start=" + firstDay + "&end=" + mSelectEndDate.getText().toString();
                generateActualCash(url, new VolleyCallBack() {
                    @Override
                    public void onResult(VolleyStatus status) {
                        if(status == VolleyStatus.SUCCESS) {
                            mTotal.setText(String.valueOf(actual));
                            total = actual;
                            cashFlowViewModel.getMonthListLiveData().setValue(recordList);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
                Toast.makeText(mContext, "產生正負金額 " + diff, Toast.LENGTH_SHORT).show();
            }
        });

        mStartSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(mContext, "start search....", Toast.LENGTH_LONG ).show();
                url = "http://" + globalVariable.getInstance().getIpaddress() + ":" + globalVariable.getInstance().getPort()
                        + "/anho/record/charged/rangedate?start=" +
                        firstDay + "&end=" + mSelectEndDate.getText().toString();
                refreshRecyclerView();
                parseRecordListData(url, new VolleyCallBack() {
                    @Override
                    public void onResult(VolleyStatus status) {
                        cashFlowViewModel.getMonthListLiveData().setValue(recordList);
                        calculateTotal();
                        //mTotal.setText(String.valueOf(total));
                        mAdapter.notifyDataSetChanged();
                        enableDifferenceButton();
//                        mDifferenceButton.setEnabled(true);
//                        differenceButtonEnabled = true;
//                        mDifferenceButton.setColorFilter(getResources().getColor(R.color.colorPrimaryDark));
                    }
                });
            }
        });

        mAddFinanceRecordFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddFinanceRecordDialogFragment addFinanceRecordDialogFragment = AddFinanceRecordDialogFragment.newInstance(CashflowMonthFragment.this);
                addFinanceRecordDialogFragment.show(getFragmentManager(), "addFinanceRecord");
                //Toast.makeText(mContext, "加入出納項目", Toast.LENGTH_SHORT).show();
            }
        });
        setRetainInstance(true);
        return view;
    }

    public void refreshRecyclerView() {
        recordList.removeAll(recordList);
        cashFlowViewModel.getMonthListLiveData().setValue(recordList);
        mAdapter.notifyDataSetChanged();

    }

    public void enableDifferenceButton() {
        differenceButtonEnabled = true;
        mDifferenceButton.setEnabled(true);
        mDifferenceButton.setColorFilter(getResources().getColor(R.color.colorPrimaryDark));
    }

    private class MyTask extends AsyncTask<String, Integer, String> {
        boolean result = false;
        // Runs in UI before background thread is called
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            result = false;

            // Do something like display a progress bar
        }

        // This is run in a background thread
        @Override
        protected String doInBackground(String... params) {
            Log.d("we are in another thread", "weee");
            url = "http://" + globalVariable.getInstance().getIpaddress() + ":" + globalVariable.getInstance().getPort()
                    + "/anho/record/charged/rangedate?start=" +
                    lastmonthFirstDay + "&end=" + lastmonthLastDay;
            Log.d("last month", lastmonthFirstDay + lastmonthLastDay);

            parseLastMonthRecordListData(url, new VolleyCallBack() {
                @Override
                public void onResult(VolleyStatus status) {
                    if(status == VolleyStatus.SUCCESS) {
                        cashFlowViewModel.getLastMonthListLiveData().setValue(lastmonthRecordList);
                        //calculateLastMonthTotal();
                        result = true;
                    }
                }
            });

            while(!result) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return "interrupted";
                }
            }
            return "success";
        }

        // This is called from background thread but runs in UI
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            // Do things like update the progress bar
        }

        // This runs in UI when background thread finishes
        @Override
        protected void onPostExecute(String result) {
            //super.onPostExecute(result);
            int total = calculateLastMonthTotal();
            mLastMonthTotal.setText(String.valueOf(total));
            Log.d("updating last month total",  total + "weee");
            // Do things like hide the progress bar or change a TextView
        }
    }


    public void getLastMonthRecordList() {
        url = "http://" + globalVariable.getInstance().getIpaddress() + ":" + globalVariable.getInstance().getPort()
                + "/anho/record/charged/rangedate?start=" +
                lastmonthFirstDay + "&end=" + lastmonthLastDay;
        Log.d("last month", lastmonthFirstDay + lastmonthLastDay);
        lastmonthRecordList = new ArrayList<>();
        parseLastMonthRecordListData(url, new VolleyCallBack() {
            @Override
            public void onResult(VolleyStatus status) {
                if(status == VolleyStatus.SUCCESS) {
                    cashFlowViewModel.getLastMonthListLiveData().setValue(lastmonthRecordList);
                    calculateLastMonthTotal();
                }
            }
        });

    }

    public void getRecordList() {
        url = "http://" + globalVariable.getInstance().getIpaddress() + ":" + globalVariable.getInstance().getPort()
                + "/anho/record/charged/rangedate?start=" +
                firstDay + "&end=" + mSelectEndDate.getText().toString();
        recordList.removeAll(recordList);
        parseRecordListData(url, new VolleyCallBack() {
            @Override
            public void onResult(VolleyStatus status) {
                if(status == VolleyStatus.SUCCESS) {
                    cashFlowViewModel.getMonthListLiveData().setValue(recordList);
                    enableDifferenceButton();
                    calculateTotal();
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    private void parseRecordListData(String url, final VolleyCallBack volleyCallBack) {
        JsonArrayRequest jsonArrayRequest =
                new JsonArrayRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
//                                total = 0;
                                for(int i = 0; i < response.length(); i++){
                                    JSONObject object = null;
                                    try {
                                        object = response.getJSONObject(i);

                                        String createAt = object.getString("createAt");
                                        Integer rid = object.getInt("rid");
                                        Integer pid = object.getInt("pid");
                                        Integer mid = object.getInt("mid");
                                        String name = object.getString("medicineName");
                                        Integer quantity = object.getInt("quantity");
                                        Integer subtotal = object.getInt("subtotal");
                                        String createBy = object.getString("createBy");
                                        String payment = object.getString("payment");
                                        String chargeAt = object.getString("chargeAt");
                                        String chargeBy = object.getString("chargeBy");
                                        String patientName = object.getString("patientName");
                                        MedicineRecordCardViewModel item = new MedicineRecordCardViewModel(rid, createAt, mid, name, quantity,
                                                subtotal, payment, pid, createBy);
                                        item.setPatientName(patientName);
                                        item.setChargeAt(chargeAt);
                                        item.setChargeBy(chargeBy);
                                        if(!recordList.contains(item)) {
                                            recordList.add(item);
                                        }
//                                        if(name.equalsIgnoreCase("實際金額")) {
//                                            //do nothing
//                                        } else {
//                                            total += subtotal.intValue();
//                                        }
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


    private void parseLastMonthRecordListData(String url, final VolleyCallBack volleyCallBack) {
        JsonArrayRequest jsonArrayRequest =
                new JsonArrayRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
//                                total = 0;
                                for(int i = 0; i < response.length(); i++){
                                    JSONObject object = null;
                                    try {
                                        object = response.getJSONObject(i);

                                        String createAt = object.getString("createAt");
                                        Integer rid = object.getInt("rid");
                                        Integer pid = object.getInt("pid");
                                        Integer mid = object.getInt("mid");
                                        String name = object.getString("medicineName");
                                        Integer quantity = object.getInt("quantity");
                                        Integer subtotal = object.getInt("subtotal");
                                        String createBy = object.getString("createBy");
                                        String payment = object.getString("payment");
                                        String chargeAt = object.getString("chargeAt");
                                        String chargeBy = object.getString("chargeBy");
                                        String patientName = object.getString("patientName");
                                        MedicineRecordCardViewModel item = new MedicineRecordCardViewModel(rid, createAt, mid, name, quantity,
                                                subtotal, payment, pid, createBy);
                                        item.setPatientName(patientName);
                                        item.setChargeAt(chargeAt);
                                        item.setChargeBy(chargeBy);
                                        if(!lastmonthRecordList.contains(item)) {
                                            lastmonthRecordList.add(item);
                                        }
//                                        if(name.equalsIgnoreCase("實際金額")) {
//                                            //do nothing
//                                        } else {
//                                            total += subtotal.intValue();
//                                        }
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

    public void calculateTotal () {
//        Log.d("size of the new record",cashFlowViewModel.getMonthListLiveData().getValue().size()+"");
        total = 0;
        bank = 0;
        allowance = 0;
        for(MedicineRecordCardViewModel r : cashFlowViewModel.getMonthListLiveData().getValue()) {
            if(r.getMedicineName().equalsIgnoreCase("實際金額")) {
                //do nothing
            } else {
//                Log.d("the subtotal is",r.getSubtotal() + "");
                total += r.getSubtotal().intValue();
                if (r.getMedicineName().equalsIgnoreCase("存入銀行")) {
                    bank += r.getSubtotal().intValue();
                } else if (r.getMedicineName().equalsIgnoreCase("補零用金")) {
                    allowance += r.getSubtotal().intValue();
                }
//                Log.d("the total is",total + "");
            }
        }
        mTotal.setText(String.valueOf(total));
        mBankTotal.setText(String.valueOf(bank));
        mAllowanceTotal.setText(String.valueOf(allowance));
    }


    public int calculateLastMonthTotal () {
//        Log.d("size of the new record",cashFlowViewModel.getMonthListLiveData().getValue().size()+"");
        lastmonthTotal = 0;
        for(MedicineRecordCardViewModel r : cashFlowViewModel.getLastMonthListLiveData().getValue()) {
            if(r.getMedicineName().equalsIgnoreCase("實際金額")) {
                //do nothing
            } else {
                //Log.d("the subtotal is",r.getSubtotal() + "" + r.getMedicineName());
                lastmonthTotal += r.getSubtotal().intValue();
            }
        }
        Log.d("the total from last month  is",lastmonthTotal + "");
        return lastmonthTotal;
       // mLastMonthTotal.setText(String.valueOf(lastmonthTotal));
    }

    private void generateActualCash(String url, final VolleyCallBack volleyCallBack) {
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
                                        String[] createArray = createAt.split("T");
                                        Integer rid = object.getInt("rid");
                                        Integer pid = object.getInt("pid");
                                        Integer mid = object.getInt("mid");
                                        String name = object.getString("medicineName");
                                        Integer quantity = object.getInt("quantity");
                                        Integer subtotal = object.getInt("subtotal");
                                        String createBy = object.getString("createBy");
                                        String payment = object.getString("payment");
                                        String chargeAt = object.getString("chargeAt");
                                        String chargeBy = object.getString("chargeBy");
                                        String patientName = object.getString("patientName");
                                        MedicineRecordCardViewModel item = new MedicineRecordCardViewModel(rid, createArray[0], mid, name, quantity,
                                                subtotal, payment, pid, createBy);
                                        item.setPatientName(patientName);
                                        item.setChargeAt(chargeAt);
                                        item.setChargeBy(chargeBy);
                                        //Log.d("adding the actual cash data",name);
                                        if(!recordList.contains(item)) {
                                            Log.d("adding the actual cash data",name);
                                            recordList.add(item);
                                        } else {
                                            Log.d("data already in the record list",name);
                                        }
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

    public void setEndDateTextView(String endDate) {
        mSelectEndDate.setText(endDate);
    }
}
