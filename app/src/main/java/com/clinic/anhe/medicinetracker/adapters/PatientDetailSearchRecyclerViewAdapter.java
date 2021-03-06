package com.clinic.anhe.medicinetracker.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.clinic.anhe.medicinetracker.R;
import com.clinic.anhe.medicinetracker.ViewModel.PatientDetailViewModel;
import com.clinic.anhe.medicinetracker.fragments.PatientDetailSearchFragment;
import com.clinic.anhe.medicinetracker.model.MedicineRecordCardViewModel;
import com.clinic.anhe.medicinetracker.networking.VolleyCallBack;
import com.clinic.anhe.medicinetracker.networking.VolleyController;
import com.clinic.anhe.medicinetracker.networking.VolleyStatus;
import com.clinic.anhe.medicinetracker.utils.GlobalVariable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientDetailSearchRecyclerViewAdapter extends RecyclerView.Adapter<PatientDetailSearchRecyclerViewAdapter.PatientSearchViewHolder>{
//    private List<MedicineRecordCardViewModel> recordList;
    private Context mContext;
    private Map<String,Integer> employee;
//    private String[] employeeList;
    private VolleyController volleyController;
    private GlobalVariable globalVariable;
    private String ip;
    private String port;
    private PatientDetailViewModel patientDetailViewModel;


    public PatientDetailSearchRecyclerViewAdapter(PatientDetailViewModel patientDetailViewModel) {
        this.patientDetailViewModel = patientDetailViewModel;
    }
    @NonNull
    @Override
    public PatientDetailSearchRecyclerViewAdapter.PatientSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_patient_detail_search, parent, false);
        mContext = view.getContext();
        employee = new HashMap<>();

        globalVariable = GlobalVariable.getInstance();
        ip = globalVariable.getIpaddress();
        port = globalVariable.getPort();

        String url = "http://" + ip + ":" + port + "/anho/employee/all";
        parseEmployeeData(url, new VolleyCallBack() {
            @Override
            public void onResult(VolleyStatus status) {
                notifyDataSetChanged();
            }
        });
        PatientSearchViewHolder patientSearchViewHolder = new PatientSearchViewHolder(view);
        return patientSearchViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PatientDetailSearchRecyclerViewAdapter.PatientSearchViewHolder holder, int position) {
        MedicineRecordCardViewModel current = patientDetailViewModel.getSearchListLiveData().getValue().get(position);
        holder.itemQuantity.setText(String.valueOf(current.getQuantity()));
        holder.itemSubtotal.setText(String.valueOf(current.getSubtotal()));
        holder.itemCreateDate.setText(current.getCreateAt().toString());
        holder.itemPatientName.setText(current.getPatientName().toString());
        for(Map.Entry<String, Integer> e : employee.entrySet()) {
            if (e.getValue() == Integer.parseInt(current.getCreateBy())) {
                holder.itemCreateBy.setText(e.getKey());
                break;
            }
        }
        holder.itemName.setText(current.getMedicineName());
        holder.itemPayment.setText(current.getPayment().equalsIgnoreCase("CASH") ? "現" : "月");
        if(current.getChargeBy().equalsIgnoreCase("null")) {
            holder.itemChargeBy.setText("尚未結帳");
            holder.itemChargeAt.setText("0000-00-00");
            holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.unchargeViewColor));
        } else {
            for(Map.Entry<String, Integer> e : employee.entrySet()) {
                if (e.getValue() == Integer.parseInt(current.getChargeBy())) {
                    holder.itemChargeBy.setText(e.getKey());
                    break;
                }
            }
            holder.itemChargeAt.setText(current.getChargeAt());
        }
    }

    @Override
    public int getItemCount() {
        return patientDetailViewModel.getSearchListLiveData().getValue().size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    public class PatientSearchViewHolder extends ViewHolder {
        private TextView itemCreateDate;
        private TextView itemCreateBy;
        private TextView itemName;
        private TextView itemPayment;
        private TextView itemQuantity;
        private TextView itemSubtotal;
        private TextView itemChargeBy;
        private TextView itemChargeAt;
        private TextView itemPatientName;

        public PatientSearchViewHolder(View itemView) {
            super(itemView);
            itemCreateDate = itemView.findViewById(R.id.patient_detail_search_createdate);
            itemCreateBy = itemView.findViewById(R.id.patient_detail_search_createby);
            itemName = itemView.findViewById(R.id.patient_detail_search_itemname);
            itemPayment = itemView.findViewById(R.id.patient_detail_search_payment);
            itemQuantity = itemView.findViewById(R.id.patient_detail_search_quantity);
            itemSubtotal = itemView.findViewById(R.id.patient_detail_search_subtotal);
            itemChargeBy = itemView.findViewById(R.id.patient_detail_search_chargeby);
            itemChargeAt = itemView.findViewById(R.id.patient_detail_search_chargedate);
            itemPatientName = itemView.findViewById(R.id.patient_detail_search_patientname);
        }
    }

    private void parseEmployeeData(String url, final VolleyCallBack volleyCallBack) {
        JsonArrayRequest jsonArrayRequest =
                new JsonArrayRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                for(int i = 0; i < response.length(); i++) {
//                                    employeeList = new String[response.length()];
                                    JSONObject object = null;
                                    try {
                                        object = response.getJSONObject(i);
                                        String name = object.getString("name");
                                        Integer eid = object.getInt("eid");
                                        employee.put(name,eid);
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
                                //Log.d("VOLLEY", error.toString());
                                volleyCallBack.onResult(VolleyStatus.FAIL);
                            }
                        } ){
                    /**
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
                    }
                };

        volleyController.getInstance(mContext).addToRequestQueue(jsonArrayRequest);

    }


}


