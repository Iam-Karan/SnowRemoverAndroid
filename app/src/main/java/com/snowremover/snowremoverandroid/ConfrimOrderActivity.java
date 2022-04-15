package com.snowremover.snowremoverandroid;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ConfrimOrderActivity extends AppCompatActivity {

    Toolbar toolbar;

    private final String secretKey = "sk_test_51KnTwUEeWLLzf6XRkIcLGeU3HdIc8NB2VdRbFbzT2TZvOylvdeMjgFasmnlyDlPPt26cduvAf9zqmsZXwufJkrZn00Pk94nVbP";
    private final String publishKey = "pk_test_51KnTwUEeWLLzf6XRPYGwXPOkSE98yeUkbjchJLFOQy8i9Gi6cnqW2AG0QkV2IfSXhxgY1cb0gFUl2X40vG4VStbA00p865OQIv";
    PaymentSheet paymentSheet;
    String customerId;
    String emphericalKey;
    String clientSecret;

    ImageButton backImageButton;
    double subTotalValue = 0;
    private double taxValue = 15.0;
    TextInputEditText addressLine, city, zipcode, state, country;
    TextView subtotal, discount, tax, total;
    String  adddressLineString, cityString, zipcodeString, stateString, countryString;
    double totoalValue;
    ArrayList<CartModel> cartData = new ArrayList<>();
    FirebaseFirestore firestore;
    String uId;
    AppCompatButton buy;
    String dateString, typeString, totalString, idString, hoursString, quantityString;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confrim_order);
        ConfrimOrderActivity.this.setTitle("");
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        typeString = intent.getExtras().getString("type");
        dateString = intent.getExtras().getString("date");
        totalString = intent.getExtras().getString("total");
        idString = intent.getExtras().getString("id");
        hoursString = intent.getExtras().getString("hours");
        quantityString = intent.getExtras().getString("quantity");

        Log.d("order Intent", typeString+dateString+totalString+idString);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        assert mFirebaseUser != null;
        uId = mFirebaseUser.getUid();
        findId();
        getData();

        buy.setOnClickListener(view -> {
            if(fatchAndValidate()){
                paymentFlow();
            }
        });
        backImageButton.setOnClickListener(view -> {
            Intent homeIntent = new Intent(getApplicationContext(), HomeScreen.class);
            startActivity(homeIntent);
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
        if(typeString.equals("cart")){
            firestore.collection("users").document(uId).collection("cart").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {

                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot d : list) {

                                String id = d.getId();
                                String quantity = d.getData().get("quantity").toString();
                                String type = d.getData().get("type").toString();
                                String name = d.getData().get("name").toString();
                                String imageUrl = d.getData().get("image").toString();
                                String hours = d.getData().get("hours").toString();
                                double price = Double.parseDouble(d.getData().get("price").toString());
                                if(type.equals("person")){
                                    subTotalValue = subTotalValue + (price * Integer.parseInt(hours));
                                }else {
                                    subTotalValue = subTotalValue + (price * Integer.parseInt(quantity));
                                }

                                CartModel data = new CartModel(id, type, quantity, name, imageUrl, hours, price);
                                cartData.add(data);
                            }
                        } else {

                            Toast.makeText(getApplicationContext(), "No data found in Database", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Fail to get the data.", Toast.LENGTH_SHORT).show())
                    .addOnCompleteListener(task -> setData());
        }
        if(typeString.equals("person")){
            firestore.collection("person").document(idString).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            assert document != null;
                            if (document.exists()) {

                                String id = document.getId();
                                String quantity = quantityString;
                                String type = "person";
                                String name = document.getData().get("name").toString();
                                String imageUrl = "personimages/"+document.getData().get("imageurl").toString();
                                String hours = hoursString;
                                double price = Double.parseDouble(document.getData().get("Price").toString());
                                subTotalValue = subTotalValue + price * Integer.parseInt(hours);
                                CartModel data = new CartModel(id, type, quantity, name, imageUrl, hours, price);
                                cartData.add(data);
                                setData();
                            } else {
                                Log.d("document Not Found", "No such document");
                            }
                        } else {
                            Log.d("error", "get failed with ", task.getException());
                        }
                    }).addOnFailureListener(e -> Log
                    .d("error", e.toString()));
        }
        if(typeString.equals("products")){
            firestore.collection("products").document(idString).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            assert document != null;
                            if (document.exists()) {

                                String id = document.getId();
                                String quantity = quantityString;
                                String type = "products";
                                String name = document.getData().get("name").toString();
                                String imageUrl  = "products/"+document.getData().get("main_image").toString();
                                String hours = hoursString;
                                double price = Double.parseDouble(document.getData().get("price_numerical").toString());
                                subTotalValue = subTotalValue + price * Integer.parseInt(quantity);
                                CartModel data = new CartModel(id, type, quantity, name, imageUrl, hours, price);
                                cartData.add(data);
                                setData();
                            } else {
                                Log.d("document Not Found", "No such document");
                            }
                        } else {
                            Log.d("error", "get failed with ", task.getException());
                        }
                    }).addOnFailureListener(e -> Log.d("error", e.toString()));
        }
        paymentConfigiration();
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
        buy.setText("Pay "+totoalValue);
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        Date parsedDate = null;
        Date currentTime = Calendar.getInstance().getTime();
        Timestamp curruntTimestamp = new Timestamp(currentTime);
        Timestamp timestamp;
        try {
            parsedDate = dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(parsedDate == null){
            timestamp = new Timestamp(currentTime);
        }else {
             timestamp = new Timestamp(parsedDate);
        }

        DocumentReference documentReference = firestore.collection("users").document(uId).collection("order").document();
        Map<String, Object> data = new HashMap<>();
        data.put("total", totoalValue);
        data.put("order_date", curruntTimestamp);
        data.put("reservation_datetime", timestamp);
        data.put("payment", true);
        data.put("items", cartData);

        documentReference.set(data).addOnSuccessListener(unused -> {
            if(typeString.equals("cart")){
                for(int i = 0; i < cartData.size(); i++){
                    firestore.collection("users").document(uId).collection("cart").document(cartData.get(i).getId())
                            .delete()
                            .addOnSuccessListener(unused1 -> {
                            });
                }
            }
            Toast.makeText(getApplicationContext(), "Order placed successfully!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
            startActivity(intent);
        }).addOnFailureListener(e -> Log.d("error", e.toString()));
    }

    public void paymentConfigiration(){
        PaymentConfiguration.init(this, publishKey);

        paymentSheet = new PaymentSheet(this, paymentSheetResult -> {
            onPaymentResult(paymentSheetResult);
        });

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/customers",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            customerId = object.getString("id");
                            Log.d("customer Id", customerId);
                            getEmphericalKey(customerId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer "+secretKey);
                return header;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(ConfrimOrderActivity.this);
        requestQueue.add(stringRequest);
    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if(paymentSheetResult instanceof  PaymentSheetResult.Completed){
            setOrder();
        }
    }

    public void getEmphericalKey(String customerId){
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/ephemeral_keys",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            emphericalKey = object.getString("id");
                            Log.d("Empherical Key", emphericalKey);
                            getClientSecret(customerId, emphericalKey);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer "+secretKey);
                header.put("Stripe-Version", "2020-08-27");
                return header;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerId);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(ConfrimOrderActivity.this);
        requestQueue.add(stringRequest);
    }

    private void getClientSecret(String customerId, String emphericalKey) {
        double intTotal = totoalValue * 100;
        int totalInt = (int) intTotal;
        String finalTotal = String.valueOf(totalInt);
        Toast.makeText(getApplicationContext(), finalTotal, Toast.LENGTH_LONG).show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/payment_intents",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            clientSecret = object.getString("client_secret");
                            Log.d("Client secret key Id", clientSecret);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer "+secretKey);
                return header;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer", customerId);
                params.put("amount", finalTotal);
                params.put("currency", "cad");
                params.put("automatic_payment_methods[enabled]", "true");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(ConfrimOrderActivity.this);
        requestQueue.add(stringRequest);
    }

    private void paymentFlow() {
        paymentSheet.presentWithPaymentIntent(
                clientSecret, new PaymentSheet.Configuration("Snow remover",
                        new PaymentSheet.CustomerConfiguration(
                                customerId,
                                emphericalKey
                        ))
        );
    }


}