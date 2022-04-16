package com.snowremover.snowremoverandroid.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.snowremover.snowremoverandroid.CartAdapterRecyclerView;
import com.snowremover.snowremoverandroid.R;

import java.util.ArrayList;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>{
    private ArrayList<FeedbackModel> feedbacks;
    private ArrayList<FeedbackModel> copyData;
    FirebaseFirestore firestore;

    public FeedbackAdapter(ArrayList<FeedbackModel> feedbacks, ArrayList<FeedbackModel> copyData) {
        this.feedbacks = feedbacks;
        this.copyData = copyData;
    }

    public class FeedbackViewHolder extends RecyclerView.ViewHolder{
        private TextView name;
        private TextView email;
        private TextView feedback;
        private AppCompatButton isRead;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.admin_feedback_name);
            email = itemView.findViewById(R.id.admin_feedback_email);
            feedback = itemView.findViewById(R.id.admin_feedback_feedback);
            isRead = itemView.findViewById(R.id.admin_feedback_read);
        }
    }

    @NonNull
    @Override
    public FeedbackAdapter.FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_feedback_card, parent, false);
        return new FeedbackAdapter.FeedbackViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackAdapter.FeedbackViewHolder holder, int position) {
        holder.name.setText(feedbacks.get(position).getName());
        holder.email.setText(feedbacks.get(position).getEmail());
        holder.feedback.setText(feedbacks.get(position).getFeedback());

        holder.isRead.setOnClickListener(view -> {copyData.remove(position);
            readFeedback(feedbacks.get(position).getId());
        });
    }

    @Override
    public int getItemCount() {
        return feedbacks.size();
    }

    public void readFeedback(String id){

        firestore.collection("contactMessages").document(id).update("read", false).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                feedbacks.clear();
                feedbacks.addAll(copyData);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

}
