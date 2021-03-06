package com.clinic.anhe.medicinetracker.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.clinic.anhe.medicinetracker.R;
import com.clinic.anhe.medicinetracker.ViewModel.CheckoutViewModel;
import com.clinic.anhe.medicinetracker.model.PatientsCardViewModel;
import com.clinic.anhe.medicinetracker.utils.ArgumentVariables;

public class PatientDetailFragment extends Fragment {

    BottomNavigationView mBottomNavigationView;
    private String selectedPatientName;
    private String selectedPatientIC;
    private Integer selectedPatientPID;
    private CheckoutViewModel checkoutViewModel;

    public static PatientDetailFragment newInstance(PatientsCardViewModel selectedPatient) {
        PatientDetailFragment fragment = new PatientDetailFragment();
        Bundle args = new Bundle();
        args.putString(ArgumentVariables.ARG_SELECTED_PATIENT_NAME, selectedPatient.getPatientName());
        args.putString(ArgumentVariables.ARG_SELECTED_PATIENT_ID, selectedPatient.getPatientIC());
        args.putInt(ArgumentVariables.ARG_SELECTED_PATIENT_PID, selectedPatient.getPID());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // What i have added is this
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.getItem(0).setVisible(false);
        //Log.d("here is the option menu", "Chloe");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ArgumentVariables.ARG_SELECTED_PATIENT_NAME, selectedPatientName);
        outState.putString(ArgumentVariables.ARG_SELECTED_PATIENT_ID, selectedPatientIC);
        outState.putInt(ArgumentVariables.ARG_SELECTED_PATIENT_PID, selectedPatientPID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_detail, container, false);

        if(savedInstanceState != null ) {
            selectedPatientName = savedInstanceState.getString(ArgumentVariables.ARG_SELECTED_PATIENT_NAME);
            selectedPatientIC = savedInstanceState.getString(ArgumentVariables.ARG_SELECTED_PATIENT_ID);
            selectedPatientPID = savedInstanceState.getInt(ArgumentVariables.ARG_SELECTED_PATIENT_PID);
        }
        if(selectedPatientName == null) {
            selectedPatientName = getArguments().getString(ArgumentVariables.ARG_SELECTED_PATIENT_NAME);
            selectedPatientIC = getArguments().getString(ArgumentVariables.ARG_SELECTED_PATIENT_ID);
            selectedPatientPID = getArguments().getInt(ArgumentVariables.ARG_SELECTED_PATIENT_PID);
        }

        checkoutViewModel = ViewModelProviders.of(this).get(CheckoutViewModel.class);

      //  Log.d("tring to see if we can get pid", "" + getArguments().getInt(ArgumentVariables.ARG_SELECTED_PATIENT_PID));
        mBottomNavigationView = view.findViewById(R.id.patient_detail_bottom_navigation);


        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                switch(item.getItemId()) {
                    case R.id.patient_detail_cash:
                        PatientDetailCashFragment patientDetailCashFragment = PatientDetailCashFragment.newInstance(
                                selectedPatientName, selectedPatientIC, selectedPatientPID);
                        transaction.replace(R.id.patient_detail_container, patientDetailCashFragment, "patientDetailCash")
                                   .commit();
                        break;
                    case R.id.patient_detail_month:
                        PatientDetailMonthFragment patientDetailMonthFragment = PatientDetailMonthFragment.newInstance(
                                selectedPatientName, selectedPatientIC, selectedPatientPID);
                        transaction.replace(R.id.patient_detail_container, patientDetailMonthFragment)
                                   .commit();
                        break;
                    case R.id.patient_detail_search:
                        PatientDetailSearchFragment patientDetailSearchFragment = PatientDetailSearchFragment.newInstance(
                                selectedPatientName, selectedPatientIC, selectedPatientPID);
                        transaction.replace(R.id.patient_detail_container, patientDetailSearchFragment)
                                   .commit();
                        break;
                }
                return false;
            }
        });

        //TODO: check detail cash as default when start up. the following does not work
        //check detail cash when start up
        if(savedInstanceState == null) {
            mBottomNavigationView.getMenu().performIdentifierAction(R.id.patient_detail_cash, 0);
        }
        setRetainInstance(true);
        return view;
    }
}
