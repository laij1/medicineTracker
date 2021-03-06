package com.clinic.anhe.medicinetracker.adapters;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.clinic.anhe.medicinetracker.R;
import com.clinic.anhe.medicinetracker.ViewModel.CheckoutViewModel;
import com.clinic.anhe.medicinetracker.fragments.AddMedicineDialogFragment;
import com.clinic.anhe.medicinetracker.fragments.PatientDetailCashFragment;
import com.clinic.anhe.medicinetracker.fragments.PatientDetailMonthFragment;
import com.clinic.anhe.medicinetracker.fragments.SignatureDialogFragment;
import com.clinic.anhe.medicinetracker.model.MedicineRecordCardViewModel;
import com.clinic.anhe.medicinetracker.networking.VolleyCallBack;
import com.clinic.anhe.medicinetracker.networking.VolleyController;
import com.clinic.anhe.medicinetracker.networking.VolleyStatus;
import com.clinic.anhe.medicinetracker.utils.CounterFab;
import com.clinic.anhe.medicinetracker.utils.GlobalVariable;
import com.clinic.anhe.medicinetracker.utils.PaymentType;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PatientDetailCashRecyclerViewAdapter extends RecyclerView.Adapter<PatientDetailCashRecyclerViewAdapter.PatientCashViewHolder> {

    private List<MedicineRecordCardViewModel> recordList;
    private VolleyController volleyController;
    private GlobalVariable globalVariable;
    private String ip;
    private String port;
    private Context mContext;
    private Fragment mFragement;
    private Map<String,Integer> employee;
    private String[] employeeList;
    private MedicineRecordCardViewModel deletedMedicineRecord;
    private CounterFab checkoutCounterFab;
    private CheckoutViewModel checkoutViewModel;
    private int checkouTotal = 0;



    public PatientDetailCashRecyclerViewAdapter(List<MedicineRecordCardViewModel> recordList, Context mContext,
                                                Fragment fragment, CounterFab counterFab, CheckoutViewModel viewModel) {
        this.recordList = recordList;
        this.mContext = mContext;
        this.mFragement = fragment;
        this.checkoutCounterFab = counterFab;
        this.checkoutViewModel = viewModel;

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    @NonNull
    @Override
    public PatientDetailCashRecyclerViewAdapter.PatientCashViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_patient_detail_cash, parent, false);
        globalVariable = GlobalVariable.getInstance();
        ip = globalVariable.getIpaddress();
        port = globalVariable.getPort();

        employee = new HashMap<>();
        String url = "http://" + ip + ":" + port + "/anho/employee/all";
        parseEmployeeData(url, new VolleyCallBack() {
            @Override
            public void onResult(VolleyStatus status) {
                    notifyDataSetChanged();
            }
        });
        PatientCashViewHolder patientCashViewHolder = new PatientCashViewHolder(view);
        return patientCashViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PatientCashViewHolder holder, int position) {
        MedicineRecordCardViewModel current = recordList.get(position);
        holder.itemQuantity.setText(String.valueOf(current.getQuantity()));
        holder.itemSubtotal.setText(String.valueOf(current.getSubtotal()));
        holder.itemCreateDate.setText(current.getCreateAt().toString());
        for(Map.Entry<String, Integer> e : employee.entrySet()) {
            if (e.getValue() == Integer.parseInt(current.getCreateBy())) {
                holder.itemCreateBy.setText(e.getKey());
                break;
            }
        }
        holder.itemName.setText(current.getMedicineName());
        holder.itemPayment.setText(current.getPayment().equalsIgnoreCase("CASH") ? "現" : "月");

        if(mFragement instanceof PatientDetailCashFragment) {
            if(checkoutViewModel.getCashCheckoutLiveData().getValue().contains(current)) {
                holder.itemButton.setImageResource(R.drawable.ic_check);
            } else {
                holder.itemButton.setImageResource(R.drawable.ic_add);
            }
        } else if (mFragement instanceof PatientDetailMonthFragment) {
            if(checkoutViewModel.getMonthCheckoutLiveData().getValue().contains(current)) {
                holder.itemButton.setImageResource(R.drawable.ic_check);
            } else {
                holder.itemButton.setImageResource(R.drawable.ic_add);
            }
        }



    }


    public class PatientCashViewHolder extends RecyclerView.ViewHolder {

        private TextView itemCreateDate;
        private TextView itemCreateBy;
        private TextView itemName;
        private TextView itemPayment;
        private TextView itemQuantity;
        private TextView itemSubtotal;
        private ImageButton itemButton;
        private ImageButton itemDeleteButton;


        public PatientCashViewHolder(final View itemView) {
            super(itemView);

            itemCreateDate = itemView.findViewById(R.id.patient_detail_cash_createdate);
            itemCreateBy = itemView.findViewById(R.id.patient_detail_cash_createby);
            itemName = itemView.findViewById(R.id.patient_detail_cash_itemname);
            itemPayment = itemView.findViewById(R.id.patient_detail_cash_payment);
            itemQuantity = itemView.findViewById(R.id.patient_detail_cash_quantity);
            itemSubtotal = itemView.findViewById(R.id.patient_detail_cash_subtotal);
            itemButton = itemView.findViewById(R.id.patient_detail_cash_button);
            itemDeleteButton = itemView.findViewById(R.id.patient_detail_cash_delete);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(recordList.get(getAdapterPosition()).getPid().equals(2)) {
                       Toast.makeText(mContext, "院外人士姓名：" +
                               recordList.get(getAdapterPosition()).getPatientName(),Toast.LENGTH_LONG).show();
                    }
                }
            });

            itemDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MedicineRecordCardViewModel current = recordList.get(getAdapterPosition());
                    deleteItem(current.getRid(), new VolleyCallBack() {
                        @Override
                        public void onResult(VolleyStatus status) {
                            if(status == VolleyStatus.SUCCESS) {
                                if( mFragement instanceof PatientDetailCashFragment ){
                                    ((PatientDetailCashFragment) mFragement).refreshRecyclerView(getAdapterPosition());
                                } else if (mFragement instanceof PatientDetailMonthFragment) {
                                    ((PatientDetailMonthFragment) mFragement).refreshRecyclerView(getAdapterPosition());
                                }
                                Toast.makeText(mContext, "刪除成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mContext, "刪除失敗", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            });

            itemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MedicineRecordCardViewModel current = recordList.get(getAdapterPosition());
                    //if checkout view model has the item, remove it
                    if(mFragement instanceof PatientDetailCashFragment){
                        if(checkoutViewModel.getCashCheckoutLiveData().getValue().contains(current)) {
                            checkoutViewModel.getCashCheckoutLiveData().getValue().remove(current);
                            checkoutCounterFab.decrease();
                            itemButton.setImageResource(R.drawable.ic_add);
//                            current.setChecked(false);
                        } else {
                            checkoutViewModel.getCashCheckoutLiveData().getValue().add(current);
                            checkoutCounterFab.increase();
                            itemButton.setImageResource(R.drawable.ic_check);
//                            current.setChecked(true);
                        }
                        checkouTotal = 0;
                        for (MedicineRecordCardViewModel m : checkoutViewModel.getCashCheckoutLiveData().getValue()) {
                            checkouTotal += m.getSubtotal();
                        }
                        ((PatientDetailCashFragment) mFragement).setCheckoutTotal(checkouTotal);
                    } else if (mFragement instanceof  PatientDetailMonthFragment) {
                        if(checkoutViewModel.getMonthCheckoutLiveData().getValue().contains(current)) {
                            checkoutViewModel.getMonthCheckoutLiveData().getValue().remove(current);
                            checkoutCounterFab.decrease();
                            itemButton.setImageResource(R.drawable.ic_add);
//                            current.setChecked(false);
                        } else {
                            checkoutViewModel.getMonthCheckoutLiveData().getValue().add(current);
                            checkoutCounterFab.increase();
                            itemButton.setImageResource(R.drawable.ic_check);
//                            current.setChecked(true);
//                            Log.d("setting current to true", "Chloe " + current.isChecked() );
                        }
                        checkouTotal = 0;
                        for (MedicineRecordCardViewModel m : checkoutViewModel.getMonthCheckoutLiveData().getValue()) {
                            checkouTotal += m.getSubtotal();
                        }
                        ((PatientDetailMonthFragment) mFragement).setCheckoutTotal(checkouTotal);
                    }
                    //TODO: needs to create a dialog to select charge person
//                    String url = "http://192.168.0.4:8080/anho/record/update?rid=" + current.getRid() + "&chargeBy=1";
//                    chargeItem(url, new VolleyCallBack() {
//                        @Override
//                        public void onResult(VolleyStatus status) {
//                            if(status== VolleyStatus.SUCCESS) {
//                                itemCreateBy.setText("saved");
//                            }
//                        }
//                    });
//                    String url = "http://192.168.0.4:8080/anho/employee/all";
//                    parseEmployeeData(url, new VolleyCallBack() {
//                        @Override
//                        public void onResult(VolleyStatus status) {
//                            if(status == VolleyStatus.SUCCESS) {
//                                SignatureDialogFragment signatureDialogFragment =
//                                        SignatureDialogFragment.newInstance(employee, current.getRid(), getAdapterPosition());
//                                signatureDialogFragment.show( mFragement.getChildFragmentManager(),"signature");
//                            }
//                        }
//                    });

                }
            });

            checkoutCounterFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(checkoutCounterFab.getCount() ==0) {
                        final SweetAlertDialog nothingInCart = new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE);
                        nothingInCart.setTitleText("請選擇結帳項目");
                        nothingInCart.setConfirmText("ok");
                        nothingInCart.show();
                        nothingInCart.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                nothingInCart.dismiss();
                            }
                        });

                    } else {
//                        String result ="";
//                        for(MedicineRecordCardViewModel r :checkoutViewModel.getCashCheckoutLiveData().getValue()) {
//                            result += r.getMedicineName() + " ";
//                        }
//                        Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
                        SignatureDialogFragment signatureDialogFragment =
                                SignatureDialogFragment.newInstance(employee);
                        signatureDialogFragment.show( mFragement.getChildFragmentManager(),"signature");
                    }
                }
            });

        }


    }

    private void parseEmployeeData(String url, final VolleyCallBack volleyCallBack) {
        JsonArrayRequest jsonArrayRequest =
                new JsonArrayRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                for(int i = 0; i < response.length(); i++) {
                                    employeeList = new String[response.length()];
                                    JSONObject object = null;
                                    try {
                                        object = response.getJSONObject(i);
                                        String name = object.getString("name");
                                        Integer eid = object.getInt("eid");
                                        if( eid != 1 ) {
                                            employee.put(name,eid);
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
                                Log.d("VOLLEY", error.toString());
                                volleyCallBack.onResult(VolleyStatus.FAIL);
                            }
                        } ) {/**
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

    private void deleteItem(Integer rid, final VolleyCallBack volleyCallBack) {
        String url =  "http://" + ip + ":" + port + "/anho/record/delete?rid=" + rid;
        JsonArrayRequest jsonArrayRequest =
                new JsonArrayRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
//                                for(int i = 0; i < response.length(); i++) {
//                                    JSONObject object = null;
//                                    try {
//                                        object = response.getJSONObject(i);
//
//                                        String createAt = object.getString("createAt");
//                                        Integer rid = object.getInt("rid");
//                                        Integer pid = object.getInt("pid");
//                                        Integer mid = object.getInt("mid");
//                                        String name = object.getString("medicineName");
//                                        Integer quantity = object.getInt("quantity");
//                                        Integer subtotal = object.getInt("subtotal");
//                                        String createBy = object.getString("createBy");
//                                        String payment = object.getString("payment");
//

//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
                                volleyCallBack.onResult(VolleyStatus.SUCCESS);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("VOLLEY", error.toString());
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
