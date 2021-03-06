package com.clinic.anhe.medicinetracker.utils;

public interface ArgumentVariables {

    public static final String ARG_PATIENT_SHIFT = "patient shift";
    public static final String ARG_CARTLIST = "cartlist";
    public static final String ARG_MEDICINE_TYPE = "medicine type";
    public static final String ARG_DAY_TYPE = "day type";
    public static final String ARG_SELECTED_PATIENT_NAME = "selected patient name";
    public static final String ARG_SELECTED_PATIENT_ID = "selected patient id";
    public static final String ARG_SELECTED_PATIENT_PID = "selected patient pid";
    public static final String ARG_EMPLOYEE_LIST = "employee list";
    public static final String ARG_NURSE_NAME = "dashboard nurse name";
    public static final String ARG_PATIENT_DETAIL_SEARCH_STARTDATE = "startDate";
    public static final String ARG_PATIENT_DETAIL_SEARCH_ENDDATE= "endDate";
    public static final String ARG_SELECTED_MEDICINE_NAME ="selected medicine name";
    public static final String ARG_CART_SELECTED_PATIENT_NAME="cart selected patient name";
    public static final String ARG_CART_SELECTED_EID = "cart selected eid";
    public static final String ARG_INVENTORY_MEDICINE_NAME = "inventory medicine name";
    public static final String ARG_INVENTORY_MID = "inventory mid";
    public static final String ARG_INVENTORY_STOCK = "invnetory stock";

    public static final String ARG_DIFFERENCEBUTTON = "difference button";
    public static final String ARG_ACTUAL_CASH ="actual cash";
    public static final String ARG_CASHFLOW_MONTH_TOTAL="cashflow month total";
    public static final String ARG_CASHFLOW_LASTMONTH_TOTAL="cashflow last month total";
    public static final String ARG_CASHFLOW_SEARCH_REVENUE= "cashflow search revenu";
    public static final String ARG_CASH_CHECKOUT_TOTAL= "cash checkout total";
    public static final String ARG_MONTH_CHECKOUT_TOTAL="month checkout total";


    //for pager to recognize what kind of fragment to new
    public static final String KIND_PATIENTS = "patients";
    public static final String KIND_PATIENTLIST = "patientlist";
    public static final String KIND_DASHBOARD_PATIENTS = "dashboardpatients";


    public static final String TAG_MEDICINE_CATEGORY_FRAGMENT = "medicinecategory";
    public static final String TAG_SELECT_PATIENT_FRAGMENT = "selectpatient";
    public static final String TAG_DASHBOARD_SETTING_FRAGMENT = "dashboard setting";
    public static final String TAG_MORNING_FRAGMENT = "morning";
    public static final String TAG_AFTERNOON_FRAGMENT = "afternoon";
    public static final String TAG_NIGHT_FRAGMENT = "night";
    public static final String TAG_MEDICINE_MANAGE_FRAGMENT= "medicinemanage";
    public static final String TAG_MEDICINE_DETAIL_FRAGMENT ="medicinedetail";
    public static final String TAG_DASHBOARD_TODAY_FRAGMENT = "dashboard today";
}
