package com.world4tech.safeway;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.world4tech.safeway.models.NewApiResponse;
import com.world4tech.safeway.models.NewsHeadlines;
import com.world4tech.safeway.retrofit.CustomAdapter;
import com.world4tech.safeway.retrofit.OnFetchDataListener;
import com.world4tech.safeway.retrofit.RequestManager;
import com.world4tech.safeway.retrofit.SelectListener;

import java.util.List;

public class NewnewsActivity extends AppCompatActivity implements SelectListener, View.OnClickListener {
    RecyclerView recyclerView;
    CustomAdapter adapter;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newnews);
        Intent intent = getIntent();
        String value = intent.getStringExtra("area");
        TextView t = findViewById(R.id.city_name);
        t.setText(value);

//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                dialog.setTitle("Fetching news articles of " + query);
//                dialog.show();
//                RequestManager manager = new RequestManager(MainActivity.this);
//                manager.getNewsHeadlines(listener, "general", query);
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                Toast.makeText(MainActivity.this, "Error while searching", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        });


        dialog = new ProgressDialog(this);
        dialog.setTitle("Fetching news articles.");
        dialog.show();


        RequestManager manager = new RequestManager(this);
        manager.getNewsHeadlines(listener, "general", value);
    }

    private final OnFetchDataListener<NewApiResponse> listener = new OnFetchDataListener<NewApiResponse>() {
        @Override
        public void onFetchData(List<NewsHeadlines> list, String message) {
            if (list.isEmpty()) {
                Toast.makeText(NewnewsActivity.this, "Facing network issue", Toast.LENGTH_SHORT).show();
            } else {
                showNews(list);
                dialog.dismiss();
            }
        }

        @Override
        public void onError(String message) {

        }
    };

    private void showNews(List<NewsHeadlines> list) {
        recyclerView = findViewById(R.id.news_view);
        recyclerView.hasFixedSize();
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        adapter = new CustomAdapter(this, list, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onNewsClicked(NewsHeadlines headlines) {

    }

    @Override
    public void onClick(View view) {
//        Button button = (Button) view;
//        String category = button.getText().toString();
//
//        dialog.setTitle("Fetching news articles of " + category);
//        dialog.show();
//
//        RequestManager manager = new RequestManager(this);
//        manager.getNewsHeadlines(listener, category, null);
    }
}
