package com.snowremover.snowremoverandroid.ui.home;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.snowremover.snowremoverandroid.HomePageRecyclerView;
import com.snowremover.snowremoverandroid.ProductData;
import com.snowremover.snowremoverandroid.R;
import com.snowremover.snowremoverandroid.databinding.FragmentHomeBinding;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ArrayList<ProductData> productItemData = new ArrayList<>();
    private ArrayList<ProductData> copyItemData = new ArrayList<>();
    private RecyclerView homeRecyclerView;
    FirebaseFirestore firestore;
    HomePageRecyclerView adapter;
    private SearchView searchView;
    private ImageButton filterBtn;
    private Spinner dropdownMenu;
    private TextView tempreture, place;
    private ImageView weatherImage;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        firestore = FirebaseFirestore.getInstance();

        homeRecyclerView = root.findViewById(R.id.home_recyclerview);
        searchView = root.findViewById(R.id.home_screen_searchView);
        filterBtn = root.findViewById(R.id.home_screen_filter);
        dropdownMenu = root.findViewById(R.id.home_screen_spinner);
        tempreture = root.findViewById(R.id.home_temp);
        place = root.findViewById(R.id.home_location);
        weatherImage = root.findViewById(R.id.home_weather_image);

        new getWeather().execute();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                adapter.search(s, copyItemData);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.search(s, copyItemData);
                return true;
            }
        });

        searchView.setOnCloseListener(() -> {
            adapter.search("", copyItemData);
            return false;
        });

        filterBtn.setOnClickListener(view -> {
            if(dropdownMenu.getVisibility() == View.VISIBLE){
                dropdownMenu.setVisibility(View.GONE);

            }else {
                dropdownMenu.setVisibility(View.VISIBLE);
            }
        });

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.dropdown_item, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdownMenu.setAdapter(spinnerAdapter);
        dropdownMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 2){
                    adapter.available(copyItemData);
                }
                if(i == 3){
                    adapter.lowToHigh(copyItemData);
                }
                if (i == 4){
                    adapter.highToLow(copyItemData);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        productItemData = new ArrayList<>();
        setProductsInfo();

        return root;
    }

    private void setAdapter() {
        adapter = new HomePageRecyclerView(productItemData);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        homeRecyclerView.setLayoutManager(layoutManager);
        homeRecyclerView.setItemAnimator(new DefaultItemAnimator());
        homeRecyclerView.setAdapter(adapter);
    }


    private void setProductsInfo() {
        firestore.collection("products").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot d : list) {
                            String id = d.getId().toString();
                            String name = Objects.requireNonNull(d.getData().get("name")).toString();
                            String price = Objects.requireNonNull(d.getData().get("price_numerical")).toString();
                            float pricefloat = Float.parseFloat(price);

                            String imageUrl = Objects.requireNonNull(d.getData().get("main_image")).toString();
                            int numOfUnit = Integer.parseInt(d.getData().get("stock_unit").toString());
                            String type = d.getData().get("type").toString();
                            boolean archive = (boolean) d.getData().get("archive");
                            if(!archive){
                                ProductData data = new ProductData(id, name, imageUrl, pricefloat, numOfUnit, type);
                                productItemData.add(data);
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "No data found in Database", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(getContext(), "Fail to get the data.", Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    copyItemData.addAll(productItemData);
                    setAdapter();
                });
    }


    protected class getWeather extends AsyncTask<Void, Void, JSONObject>
    {
        String str="https://api.openweathermap.org/data/2.5/weather?q=montreal&appid=96d2ff9494ffe7aa56ac22dc7570cfa1";
        String IMG_URL = "https://openweathermap.org/img/w/";
        @Override
        protected JSONObject doInBackground(Void... params)
        {
            URLConnection urlConn = null;
            BufferedReader bufferedReader = null;
            try
            {
                URL url = new URL(str);
                urlConn = url.openConnection();
                bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

                StringBuffer stringBuffer = new StringBuffer();
                String line;
                while ((line = bufferedReader.readLine()) != null)
                {
                    stringBuffer.append(line);
                }

                return new JSONObject(stringBuffer.toString());
            }
            catch(Exception ex)
            {
                Log.e("App", "yourDataTask", ex);
                return null;
            }
            finally
            {
                if(bufferedReader != null)
                {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(JSONObject response)
        {
            if(response != null)
            {
                JSONObject object = response;
                try {
                    JSONObject tempObject = object.getJSONObject("main");
                    double temp = Double.parseDouble(tempObject.getString("temp")) - 273.15;
                    int tempInt = (int) temp;
                    tempreture.setText(Html.fromHtml(tempInt+"<sup>o</sup>"));

                    String location = object.getString("name");
                    JSONObject country = object.getJSONObject("sys");
                    location = location +", "+country.getString("country");

                    place.setText(location);
                    JSONArray jsonArray = object.getJSONArray("weather");
                    JSONObject iconObject = jsonArray.getJSONObject(0);
                    String iconUrl = IMG_URL+""+iconObject.getString("icon")+".png";

                    Picasso.get()
                            .load(iconUrl)
                            .into(weatherImage);
                    Log.d("response", iconUrl);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}