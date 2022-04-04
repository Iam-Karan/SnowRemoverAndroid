package com.snowremover.snowremoverandroid.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.snowremover.snowremoverandroid.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AdminPersonDetailActivity extends AppCompatActivity {
    private TextInputEditText name, description, price, age;
    private ImageView image;
    private ImageButton backButton, editBtn;
    private String nameValue, descriptionValue, ageValue, priceValue;
    private AppCompatButton addProduct, archive;
    private boolean archiveVlaue = false;
    String prductId = "";
    FirebaseFirestore firestore;
    int RESULT_LOAD_IMG = 120;
    Uri imageUri;
    File file;
    String imageName = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_person_detail);
        AdminPersonDetailActivity.this.setTitle("");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        prductId = intent.getExtras().getString("PersonID");
        firestore = FirebaseFirestore.getInstance();
        findUI();

        if(!prductId.isEmpty()){
            editBtn.setVisibility(View.VISIBLE);
            archive.setVisibility(View.VISIBLE);
            addProduct.setText("Update");
            setData();

            archive.setOnClickListener(view -> {
                firestore.collection("person").document(prductId).update("archive", !archiveVlaue);
                Toast.makeText(getApplicationContext(), "Product is archived!", Toast.LENGTH_SHORT).show();
                archiveVlaue = !archiveVlaue;
                setArchive();
            });
        }

        backButton.setOnClickListener(view -> {
            Intent intent1 = new Intent(getBaseContext(), AdminMainActivity.class);
            startActivity(intent1);
        });

        image.setOnClickListener(view -> getImgage());

        editBtn.setOnClickListener(view -> getImgage());

        addProduct.setOnClickListener(view -> getData());

    }

    private void findUI(){
        backButton = findViewById(R.id.back_button);
        addProduct = findViewById(R.id.person_add);
        editBtn = findViewById(R.id.edit_btn);
        archive = findViewById(R.id.person_archive);
        name = findViewById(R.id.person_name);
        description = findViewById(R.id.person_description);
        age = findViewById(R.id.person_age);
        price = findViewById(R.id.person_price);
        image = findViewById(R.id.person_image);
    }

    private void setData(){
        DocumentReference docRef = firestore.collection("person").document(prductId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                assert document != null;
                if (document.exists()) {

                    StorageReference storageReference =  FirebaseStorage.getInstance().getReference("personimages/"+document.getData().get("imageurl").toString());

                    storageReference.getDownloadUrl().addOnSuccessListener(uri ->
                            Glide.with(this)
                                    .load(uri)
                                    .into(image));
                    imageName = document.getData().get("imageurl").toString();
                    nameValue = document.getData().get("name").toString();
                    descriptionValue = document.getData().get("description").toString();
                    ageValue = document.getData().get("age").toString();
                    priceValue = document.getData().get("Price").toString();
                    archiveVlaue = (boolean) document.getData().get("archive");

                    name.setText(nameValue);
                    description.setText(descriptionValue);
                    age.setText(ageValue);
                    price.setText(priceValue);
                    setArchive();
                } else {
                    Log.d("document Not Found", "No such document");
                }
            } else {
                Log.d("error", "get failed with ", task.getException());
            }
        }).addOnFailureListener(e -> Log.d("error", e.toString()));
    }

    private void setArchive(){
        if(archiveVlaue){
            archive.setText("Unarchive");
            archive.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.green_round_btn));
        }else {
            archive.setText("Archive");
            archive.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.pink_round_btn));
        }
    }

    private void getImgage(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            try {
                imageUri = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                Cursor cursor = getContentResolver().query(imageUri,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);

                file = new File(picturePath);
                imageName = file.getName();


                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                image.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(AdminPersonDetailActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(AdminPersonDetailActivity.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    private void getData(){
        if(!checkValueFuntion(name) && !checkValueFuntion(description) && !checkValueFuntion(age) && !checkValueFuntion(price) && !imageName.isEmpty()){
            nameValue = name.getText().toString();
            descriptionValue = description.getText().toString();
            ageValue = age.getText().toString();
            priceValue = price.getText().toString();
            if(prductId.isEmpty()){
                addPerson();
            }else {
                updateData();
            }
        }
    }

    private void addPerson(){
        Map<String, Object> productData = new HashMap<>();
        productData.put("name", nameValue);
        productData.put("description", descriptionValue);
        productData.put("archive", archiveVlaue);
        productData.put("imageurl", imageName);
        productData.put("age", Integer.parseInt(ageValue));
        productData.put("Price", priceValue);
        productData.put("personId", 1);
        productData.put("id", 1);
        productData.put("completed_order", 0);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("personimages");

        storageRef.child(imageName).putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    firestore.collection("person").document().set(productData).addOnSuccessListener(unused -> Toast.makeText(AdminPersonDetailActivity.this, "Product Added Successfully!", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(AdminPersonDetailActivity.this, e.toString(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(AdminPersonDetailActivity.this, e.toString(), Toast.LENGTH_SHORT).show());
    }

    private void updateData(){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("personimages");


        firestore.collection("products").document(prductId).update(
                "imageurl" , imageName,
                "name" , nameValue,
                "archive", archiveVlaue,
                "age", Integer.parseInt(ageValue),
                "description", descriptionValue,
                "Price" , priceValue
                ).addOnSuccessListener(unused -> {

            if(imageUri != null){
                storageRef.child(imageName).putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> {

                        })
                        .addOnFailureListener(e -> Toast.makeText(AdminPersonDetailActivity.this, e.toString(), Toast.LENGTH_SHORT).show());
            }
            Toast.makeText(AdminPersonDetailActivity.this, "Product updated successfully!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Toast.makeText(AdminPersonDetailActivity.this, e.toString(), Toast.LENGTH_SHORT).show());

    }

    private boolean checkValueFuntion(TextInputEditText inputEditText){
        boolean result = inputEditText.getText().toString().isEmpty();
        if(result){
            inputEditText.setError("Field must not be empty!");
        }
        return result;
    }

}