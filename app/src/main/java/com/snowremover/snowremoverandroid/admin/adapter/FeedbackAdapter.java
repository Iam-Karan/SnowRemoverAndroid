package com.snowremover.snowremoverandroid.admin.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.snowremover.snowremoverandroid.R;
import com.snowremover.snowremoverandroid.admin.model.FeedbackModel;

import java.util.ArrayList;
import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>{
    private ArrayList<FeedbackModel> feedbacks;
    private ArrayList<FeedbackModel> copyData;
    private Context context;
    FirebaseFirestore firestore;

    public FeedbackAdapter(ArrayList<FeedbackModel> feedbacks, ArrayList<FeedbackModel> copyData, Context context) {
        this.feedbacks = feedbacks;
        this.copyData = copyData;
        this.context = context;
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

        if(feedbacks.get(position).isRead() == true){
            holder.isRead.setText("Unread");
            holder.isRead.setBackground(ContextCompat.getDrawable(context, R.drawable.green_round_btn));
        }else {
            holder.isRead.setText("Read");
            holder.isRead.setBackground(ContextCompat.getDrawable(context, R.drawable.pink_round_btn));
        }

        holder.isRead.setOnClickListener(view -> {
            boolean isnotVlaue = !feedbacks.get(position).isRead();
            copyData.get(position).setRead(isnotVlaue);
            readFeedback(feedbacks.get(position).getId(), isnotVlaue);
        });
    }

    @Override
    public int getItemCount() {
        return feedbacks.size();
    }

    public void readFeedback(String id, boolean value){
        firestore = FirebaseFirestore.getInstance();

        firestore.collection("contactMessages").document(id).update("read", value).addOnCompleteListener(task -> {
            setData(!value);
        });

    }

    private void setData(boolean radioIsRead){
        feedbacks.clear();
        copyData.clear();
        firestore.collection("contactMessages").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot d : list) {

                            String id = d.getId();
                            String name = d.getData().get("name").toString();
                            String email = d.getData().get("email").toString();
                            String feedback = d.getData().get("message").toString();
                            boolean isRead = (boolean) d.getData().get("read");

                            FeedbackModel model = new FeedbackModel(id, name, email, feedback, isRead);
                            if(radioIsRead == isRead){
                                feedbacks.add(model);
                            }
                            copyData.add(model);
                        }
                    }
                }).addOnCompleteListener(task -> {
                notifyDataSetChanged();
                });
    }

}
