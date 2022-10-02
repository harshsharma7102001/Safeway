package com.world4tech.safeway.retrofit;

import android.content.Context;
import android.widget.Toast;
import com.world4tech.safeway.R;
import com.world4tech.safeway.models.NewApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class RequestManager {
    Context context;
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://newsapi.org/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public void getNewsHeadlines(OnFetchDataListener listener, String category, String query) {
        CallNewsApi callNewsApi = retrofit.create(CallNewsApi.class);
        Call<NewApiResponse> call = callNewsApi.callHeadlines( query, context.getString(R.string.new_api_key));

        try {
            call.enqueue(new Callback<NewApiResponse>() {
                @Override
                public void onResponse(Call<NewApiResponse> call, Response<NewApiResponse> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(context, "Error in response", Toast.LENGTH_SHORT).show();
                    } else {
                        listener.onFetchData(response.body().getArticles(), response.message());
                    }
                }

                @Override
                public void onFailure(Call<NewApiResponse> call, Throwable t) {
                    listener.onError("Request Failed!");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RequestManager(Context context) {
        this.context = context;
    }

    public interface CallNewsApi {
        @GET("everything")
        Call<NewApiResponse> callHeadlines(
                //@Query("country") String country,
                // @Query("category") String category,
                @Query("q") String query,
                @Query("apiKey") String api_key
        );

    }
}
