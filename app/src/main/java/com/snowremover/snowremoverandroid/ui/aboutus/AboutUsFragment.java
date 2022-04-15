package com.snowremover.snowremoverandroid.ui.aboutus;

import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.snowremover.snowremoverandroid.R;
import com.snowremover.snowremoverandroid.SignInScreen;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AboutUsFragment extends Fragment {

    private AboutUsViewModel mViewModel;
    private TextInputEditText name, email, feedback;
    String nameValue, emailValue, feedbackValue;
    private AppCompatButton sendFeedBack;
    private FirebaseFirestore firestore;

    public static AboutUsFragment newInstance() {
        return new AboutUsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.about_us_fragment, container, false);
        firestore = FirebaseFirestore.getInstance();

        name = view.findViewById(R.id.about_us_name);
        email = view.findViewById(R.id.about_us_email);
        feedback = view.findViewById(R.id.about_us_comment);
        sendFeedBack = view.findViewById(R.id.about_us_send);

        sendFeedBack.setOnClickListener(view1 -> {
            if(validateFeedback()){
                setSendFeedback(view);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AboutUsViewModel.class);
        // TODO: Use the ViewModel
    }

    private boolean validateFeedback(){
        boolean validate = true;
        nameValue = name.getText().toString();
        emailValue = email.getText().toString();
        feedbackValue = feedback.getText().toString();

        if(nameValue.isEmpty()){
            name.setError("Name must not be empty!");
            validate = false;
        }
        if(emailValue.isEmpty()){
            email.setError("Email id is not valid!");
            validate = false;
        }
        if(feedbackValue.isEmpty()){
            feedback.setError("Feedback must not be empty!");
            validate = false;
        }

        return validate;
    }

    private void setSendFeedback(View view){

        Map<String, Object> feedbackMap = new HashMap<>();
        feedbackMap.put("name", nameValue);
        feedbackMap.put("email", emailValue);
        feedbackMap.put("message", feedbackValue);
        feedbackMap.put("read", false);

        firestore.collection("feedback").document().set(feedbackMap).addOnSuccessListener(unused -> {
            Toast.makeText(view.getContext(), "Feedback added!", Toast.LENGTH_LONG).show();
            name.setText("");
            name.setHint("Name");
            email.setText("");
            email.setHint("Email");
            feedback.setText("");
            feedback.setHint("Comment");
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
        });
    }

}