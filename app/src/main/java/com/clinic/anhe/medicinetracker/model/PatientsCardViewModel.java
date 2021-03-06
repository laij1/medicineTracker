package com.clinic.anhe.medicinetracker.model;

import com.clinic.anhe.medicinetracker.utils.DayType;

public class PatientsCardViewModel {

    private String patientName;
    private String patientIC;
    private String patientShift;
    private DayType patientDay;
    private Integer pid;
    private Boolean deleted;

    public PatientsCardViewModel(Integer pid, String patientName, String patientIC, String patientShift, String patientDay, Boolean deleted) {
        this.pid = pid;
        this.patientName = patientName;
        this.patientIC = patientIC;
        this.patientShift = patientShift;
        this.patientDay = patientDay.equals("一三五")?DayType.oddDay: DayType.evenDay;
        this.deleted = deleted;
    }

    public Integer getPID(){ return pid; }

    public String getPatientName(){
        return patientName;
    }

    public String getPatientIC(){
        return patientIC;
    }

    public String getPatientShift() { return patientShift; }

    public DayType getPatientDay() { return patientDay; }

    public boolean getDeleted() { return deleted; }

    public void setPatientName(String name) { patientName = name; }

    public void setPatientIC(String ic) { patientIC = ic; }

    public void setPatientShift(String shift) { patientShift = shift; }

    public void setPatientDay(DayType day) { patientDay = day; }

    public void setPid(Integer pid){ this.pid = pid; }

    public void setDeleted(Boolean deleted) { this.deleted = deleted; }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof PatientsCardViewModel)) {
            return false;
        }

        PatientsCardViewModel p = (PatientsCardViewModel) obj;

        return p.getPatientIC().equalsIgnoreCase(patientIC) && p.getPatientName().equalsIgnoreCase(patientName);
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
