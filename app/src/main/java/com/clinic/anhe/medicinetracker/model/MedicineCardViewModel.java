package com.clinic.anhe.medicinetracker.model;

import android.util.Log;

import com.clinic.anhe.medicinetracker.utils.PaymentType;

public class MedicineCardViewModel {

    private String medicineName;
    private String medicinePrice;
    private String medicineDose;
    private Integer medicineId;
    private Integer medicineStock;
    private String medicineCategory;
    private PaymentType cashPayment = PaymentType.UNSELECT;
    private boolean isAddToCart = false;
    private int quantity = 0;
    private float sliderPosition = 0f;
    private int subtotal = 0;
    private boolean deleted;

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public MedicineCardViewModel(
            Integer medicineId, String medicineName, String medicinePrice, String medicineDose, Integer medicineStock, String medicineCategory, boolean deleted) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.medicinePrice = medicinePrice;
        this.medicineDose = medicineDose;
        this.medicineStock = medicineStock;
        this.medicineCategory = medicineCategory;
        this.deleted = deleted;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof MedicineCardViewModel)) {
            return false;
        }

        MedicineCardViewModel med = (MedicineCardViewModel) obj;

        return med.getMedicineId() == medicineId;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + medicineName.hashCode();
        result = 31 * result + medicineId;
        return result;
    }


    public String getMedicinName(){
        return medicineName;
    }

    public String getMedicinePrice(){
        return medicinePrice;
    }

    public String getMedicineDose() { return medicineDose; }

    public int getQuantity() { return quantity; }

    public void setQuantity(int i) {
        quantity = i;

    }

    public void setCashPayment(boolean cash) {
        if(cash) {
            cashPayment = PaymentType.CASH;
        } else {
            cashPayment = PaymentType.MONTH;
        }
    }

    public float getSliderPosition() { return sliderPosition; }

    public void setSliderPosition(float f) {
        sliderPosition = f;
    }

    public PaymentType isCashPayment() {

        return cashPayment;
    }

    public void addToCart() {
        isAddToCart = true;
    }

    public void removeFromCart(){
        isAddToCart = false;
    }

    public boolean getIsAddToCart() {
        return isAddToCart;
    }

    public Integer getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Integer medicineId) {
        this.medicineId = medicineId;
    }

    public Integer getMedicineStock() {
        return medicineStock;
    }

    public void setMedicineStock(Integer medicineStock) {
        this.medicineStock = medicineStock;
    }

    public void setMedicineCategory(String medicineCategory) { this.medicineCategory = medicineCategory; }

    public String getMedicineCategory() { return medicineCategory; }

    public int getSubtotal() {
        if(subtotal == 0) {
            calculateSubtotal();
        }
        return subtotal;
    }

    public void calculateSubtotal() {
        //TODO: here we need to calculate 買ㄧ送二
            subtotal = Integer.parseInt(medicinePrice) * quantity;
    }
}
