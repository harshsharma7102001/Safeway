package com.world4tech.safeway.retrofit;

import com.world4tech.safeway.models.NewsHeadlines;

import java.util.List;

public interface OnFetchDataListener<NewApiResponse> {
    void onFetchData(List<NewsHeadlines> list, String message);
    void onError(String message);
}
