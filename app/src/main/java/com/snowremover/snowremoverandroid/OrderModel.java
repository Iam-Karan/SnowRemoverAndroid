package com.snowremover.snowremoverandroid;

import java.io.Serializable;
import java.util.ArrayList;

public class OrderModel implements Serializable {
    public ArrayList<CartModel> prodocts;
    public String date;
    public String month;
    public String hour;
    public String minute;
    public String year;
    public double totalPrice;
    public boolean payment;

    public OrderModel(ArrayList<CartModel> prodocts, String date, String month, String hour, String minute, String year, double totalPrice, boolean payment) {
        this.prodocts = prodocts;
        this.date = date;
        this.month = month;
        this.hour = hour;
        this.minute = minute;
        this.year = year;
        this.totalPrice = totalPrice;
        this.payment = payment;
    }


    public ArrayList<CartModel> getProdocts() {
        return prodocts;
    }

    public void setProdocts(ArrayList<CartModel> prodocts) {
        this.prodocts = prodocts;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public boolean isPayment() {
        return payment;
    }

    public void setPayment(boolean payment) {
        this.payment = payment;
    }

}
