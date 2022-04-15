package com.snowremover.snowremoverandroid.admin.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.snowremover.snowremoverandroid.CartModel;
import com.snowremover.snowremoverandroid.R;
import com.snowremover.snowremoverandroid.admin.FeedbackAdapter;
import com.snowremover.snowremoverandroid.admin.FeedbackModel;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminFeedbacksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminFeedbacksFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ArrayList<FeedbackModel> feedbacks;
    private ArrayList<FeedbackModel> copyFeedbacks;
    private RecyclerView feedBackRecyclerView;
    private FeedbackAdapter adapter;
    FirebaseFirestore firestore;
    public AdminFeedbacksFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdminFeedbacksFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminFeedbacksFragment newInstance(String param1, String param2) {
        AdminFeedbacksFragment fragment = new AdminFeedbacksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_feedbacks, container, false);
        feedbacks = new ArrayList<>();
        copyFeedbacks = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
        feedBackRecyclerView = view.findViewById(R.id.admin_feedback_recyclerview);
        setData();

        return view;
    }

    private void setAdapter(){
        adapter = new FeedbackAdapter(feedbacks, copyFeedbacks);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        feedBackRecyclerView.setLayoutManager(layoutManager);
        feedBackRecyclerView.setItemAnimator(new DefaultItemAnimator());
        feedBackRecyclerView.setAdapter(adapter);
    }

    private void setData(){
        firestore.collection("feedback").get()
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
                            if(!isRead){
                                feedbacks.add(model);
                            }
                        }
                        copyFeedbacks.addAll(feedbacks);
                    } else {
                        Toast.makeText(getContext(), "No data found in Database", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(getContext(), "Fail to get the data.", Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> setAdapter());
    }
}