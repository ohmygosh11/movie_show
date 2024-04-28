package com.example.myapplication.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.myapplication.database.TVShowDatabase;
import com.example.myapplication.models.TVShow;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;

public class WatchlistViewModel extends AndroidViewModel {
    private TVShowDatabase tvShowDatabase;

    public WatchlistViewModel(@NonNull Application application) {
        super(application);
        this.tvShowDatabase = TVShowDatabase.getTvShowDatabase(application);
    }

    public Flowable<List<TVShow>> loadWatchlist() {
        return tvShowDatabase.tvShowDao().getWatchlist();
    }
}
