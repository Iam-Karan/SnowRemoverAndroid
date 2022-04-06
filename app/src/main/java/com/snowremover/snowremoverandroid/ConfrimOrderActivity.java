package com.snowremover.snowremoverandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class ConfrimOrderActivity extends AppCompatActivity {

    Toolbar toolbar;
    ImageButton backImageButton;
    double subTotalValue = 0;
    private double taxValue = 15.0;
    TextInputEditText addressLine, city, zipcode, state, country;
    TextView subtotal, discount, tax, total;
    String  adddressLineString, cityString, zipcodeString, stateString, countryString;
    double totoalValue;
    ArrayList<CartModel> cartData = new ArrayList<>();
    ArrayList<OrderModel> orderData = new ArrayList<>();
    FirebaseFirestore firestore;
    String uId;
    AppCompatButton buy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confrim_order);
        ConfrimOrderActivity.this.setTitle("");
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        assert mFirebaseUser != null;
        uId = mFirebaseUser.getUid();
        findId();
        getData();

        buy.setOnClickListener(view -> {
            if(fatchAndValidate()){
                setOrder();
            }
        });
        backImageButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
            startActivity(intent);
        });
    }

    public void findId(){
        addressLine = findViewById(R.id.shiping_address_line);
        city = findViewById(R.id.shiping_city);
        zipcode = findViewById(R.id.shiping_zipcode);
        state = findViewById(R.id.shiping_state);
        country = findViewById(R.id.shiping_country);
        subtotal = findViewById(R.id.shiping_subtotal);
        discount = findViewById(R.id.shiping_discount);
        tax = findViewById(R.id.shiping_tax);
        total = findViewById(R.id.shiping_total);
        buy = findViewById(R.id.shiping_buy);
        backImageButton = findViewById(R.id.back_button);
    }

    public void getData(){
        firestore.collection("users").document(uId).collection("cart").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot d : list) {

                            String id = d.getId();
                            String quntity = d.getData().get("quantity").toString();
                            String type = d.getData().get("type").toString();
                            String name = d.getData().get("name").toString();
                            String image = d.getData().get("image").toString();
                            String hour = d.getData().get("hours").toString();
                            double personPrice = Double.parseDouble(d.getData().get("price").toString());
                            subTotalValue = subTotalValue + personPrice;
                            CartModel data = new CartModel(id, type, quntity, name, image, hour, personPrice);
                            cartData.add(data);
                        }
                    } else {

                        Toast.makeText(getApplicationContext(), "No data found in Database", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Fail to get the data.", Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> setData());
    }

    public void setData(){
        subTotalValue = round(subTotalValue, 2);
        Random r = new Random();
        int random = r.nextInt(30);
        double discountValue = round(((random * 1.0) * subTotalValue) / 100, 2);
        taxValue = round((subTotalValue * taxValue) / 100, 2);
        totoalValue = round(subTotalValue + taxValue - discountValue, 2);

        tax.setText("$"+taxValue);
        discount.setText("$"+discountValue);
        subtotal.setText("$"+subTotalValue);
        total.setText("$"+totoalValue);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public Boolean fatchAndValidate(){

        boolean validate = true;

        adddressLineString = addressLine.getText().toString();
        cityString = city.getText().toString();
        zipcodeString = zipcode.getText().toString();
        stateString = state.getText().toString();
        countryString = country.getText().toString();

        if(adddressLineString.isEmpty()){
            addressLine.setError("Line should not empty");
            validate = false;
        }
        if(cityString.isEmpty()){
            city.setError("City require");
            validate = false;
        }
        if(stateString.isEmpty()){
            state.setError("State name required");
            validate = false;
        }
        if(zipcodeString.isEmpty()){
            zipcode.setError("Zipcode require");
            validate = false;
        }
        if(countryString.isEmpty()){
            country.setError("Country require");
            validate = false;
        }

        return validate;
    }

    public void setOrder(){

        Date currentTime = Calendar.getInstance().getTime();
        String date = String.valueOf(currentTime.getDate());
        String month = String.valueOf(currentTime.getMonth());
        String year = String.valueOf(currentTime.getYear());
        String hour = String.valueOf(currentTime.getHours());
        String minute = String.valueOf(currentTime.getMinutes());
        OrderModel orderModel = new OrderModel(cartData, date, month, hour, minute, year, totoalValue, true);
        orderData.add(orderModel);
        DocumentReference documentReference = firestore.collection("users").document(uId).collection("order").document();
        Map<String, Object> data = new HashMap<>();
        data.put("orderid", orderData);

        documentReference.set(data).addOnSuccessListener(unused -> {
            for(int i = 0; i < orderData.get(0).getProdocts().size(); i++){
                firestore.collection("users").document(uId).collection("cart").document(orderData.get(0)
                        .getProdocts().get(i).getId())
                        .delete()
                        .addOnSuccessListener(unused1 -> {
                            Toast.makeText(getApplicationContext(), "Order placed successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
                            startActivity(intent);
                        });
            }
        }).addOnFailureListener(e -> Log.d("error", e.toString()));
    }

}