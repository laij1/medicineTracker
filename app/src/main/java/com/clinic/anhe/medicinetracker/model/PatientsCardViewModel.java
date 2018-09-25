package com.clinic.anhe.medicinetracker.model;

import com.clinic.anhe.medicinetracker.utils.DayType;

public class PatientsCardViewModel {

    private String patientName;
    private String patientIC;
    private String patientShift;
    private DayType patientDay;
    private Integer pid;

    public PatientsCardViewModel(Integer pid, String patientName, String patientIC, String patientShift, String patientDay) {
        this.pid = pid;
        this.patientName = patientName;
        this.patientIC = patientIC;
        this.patientShift = patientShift;
        this.patientDay = patientDay.equals("一三五")?DayType.oddDay: DayType.evenDay;
    }

    public Integer getPID(){ return pid; }

    public String getPatientName(){
        return patientName;
    }

    public String getPatientIC(){
        return patientIC;
    }

    public String getPatientShift() { return patientShift; }

    public DayType getPatientDay() {return patientDay; }

    public void setPatientName(String name) { patientName = name; }

    public void setPatientIC(String ic) { patientIC = ic; }

    public void setPatientShift(String shift) { patientShift = shift; }

    public void setPatientDay(DayType day) { patientDay = day; }

    public void setPid(Integer pid){ this.pid = pid; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof PatientsCardViewModel)) {
            return false;
        }

        PatientsCardViewModel p = (PatientsCardViewModel) obj;

        return p.getPatientIC()== patientIC;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + patientName.hashCode();
        result = 31 * result + patientIC.hashCode();
        result = 31 * result + pid;
        return result;
    }
}
