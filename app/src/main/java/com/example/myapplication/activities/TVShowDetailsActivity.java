package com.example.myapplication.activities;


import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.adapters.EpisodesAdapter;
import com.example.myapplication.adapters.ImageSliderAdapter;
import com.example.myapplication.databinding.ActivityTvshowDetailsBinding;
import com.example.myapplication.databinding.LayoutEpisodesBottomSheetBinding;
import com.example.myapplication.databinding.LayoutPaymentBottomSheetBinding;
import com.example.myapplication.listeners.EpisodesListener;
import com.example.myapplication.models.Episode;
import com.example.myapplication.models.TVShow;
import com.example.myapplication.models.TVShowDetails;
import com.example.myapplication.viewmodels.TVShowDetailsViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class TVShowDetailsActivity extends AppCompatActivity implements EpisodesListener {
    private ActivityTvshowDetailsBinding activityTvshowDetailsBinding;
    private LayoutEpisodesBottomSheetBinding layoutEpisodesBottomSheetBinding;
    private LayoutPaymentBottomSheetBinding layoutPaymentBottomSheetBinding;
    private TVShowDetailsViewModel tvShowDetailsViewModel;
    private TVShowDetails tvShowDetails;
    private TVShow tvShow;
    private BottomSheetDialog episodeBottomSheet;
    private BottomSheetDialog paymentBottomSheet;
    private boolean isAvailableInWatchlist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        activityTvshowDetailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_tvshow_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        doInitialization();
    }

    private void doInitialization() {
        tvShowDetailsViewModel = new ViewModelProvider(this).get(TVShowDetailsViewModel.class);
        activityTvshowDetailsBinding.imageBack.setOnClickListener(v -> onBackPressed());
        tvShow = (TVShow) getIntent().getSerializableExtra("tvShow");
        checkAvailableInWatchlist();
        getTVShowDetails();
        handleClickReadMore();
        handleClickVoteButton();
        handleClickEpisodesButton();
        handleClickWatchlistButton();
        handleClickPremiumButton();
    }

    private void checkAvailableInWatchlist() {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(tvShowDetailsViewModel.getTVShowFromWatchlist(String.valueOf(tvShow.getId()))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( tvShow -> {
                    if (tvShow != null) {
                        isAvailableInWatchlist = true;
                        activityTvshowDetailsBinding.imageWatchlist.setImageResource(R.drawable.ic_added);
                        compositeDisposable.dispose();
                    }
                })
        );
    }

    private void getTVShowDetails() {
        activityTvshowDetailsBinding.setIsLoading(true);
        String tvShowId = String.valueOf(tvShow.getId());
        tvShowDetailsViewModel.getTVShowDetails(tvShowId).observe(this, tvShowDetailsResponse -> {
            activityTvshowDetailsBinding.setIsLoading(false);
            if (tvShowDetailsResponse.getTvShowDetails() != null) {
                if (tvShowDetailsResponse.getTvShowDetails().getPictures() != null) {
                    loadImageSliders(tvShowDetailsResponse.getTvShowDetails().getPictures());
                }
                tvShowDetails = tvShowDetailsResponse.getTvShowDetails();
                loadInfoTVShowDetails(tvShowDetails);
            }
        });
    }

    private void loadImageSliders(String[] sliderImages) {
        activityTvshowDetailsBinding.sliderViewPager.setOffscreenPageLimit(1);
        activityTvshowDetailsBinding.sliderViewPager.setAdapter(new ImageSliderAdapter(sliderImages));
        activityTvshowDetailsBinding.sliderViewPager.setVisibility(View.VISIBLE);
        activityTvshowDetailsBinding.viewFadingEdge.setVisibility(View.VISIBLE);
        setupSliderIndicators(sliderImages.length);
        activityTvshowDetailsBinding.sliderViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentSliderIndicator(position);
            }
        });
    }

    private void setupSliderIndicators(int count) {
        ImageView[] indicators = new ImageView[count];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(8, 0, 8, 0);
        for (int i = 0; i < count; i++) {
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.background_slider_indicator_inactive
            ));
            indicators[i].setLayoutParams(layoutParams);
            activityTvshowDetailsBinding.layoutSliderIndicators.addView(indicators[i]);
        }
        activityTvshowDetailsBinding.layoutSliderIndicators.setVisibility(View.VISIBLE);
        setCurrentSliderIndicator(0);
    }

    private void setCurrentSliderIndicator(int position) {
        int childCount = activityTvshowDetailsBinding.layoutSliderIndicators.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView sliderIndicator = (ImageView) activityTvshowDetailsBinding.layoutSliderIndicators.getChildAt(i);
            if (i == position) {
                sliderIndicator.setImageDrawable(ContextCompat.getDrawable(
                        getApplicationContext(),
                        R.drawable.background_slider_indicator_active
                ));
            } else {
                sliderIndicator.setImageDrawable(ContextCompat.getDrawable(
                        getApplicationContext(),
                        R.drawable.background_slider_indicator_inactive
                ));
            }
        }
    }

    private void loadInfoTVShowDetails(TVShowDetails tvShowDetails) {
//      set tv_show details
        activityTvshowDetailsBinding.setTvShowName(tvShow.getName());
        activityTvshowDetailsBinding.textName.setVisibility(View.VISIBLE);

        activityTvshowDetailsBinding.setNetworkCountry(
                tvShow.getNetwork() + " (" + tvShow.getCountry() + ")"
        );
        activityTvshowDetailsBinding.textNetworkCountry.setVisibility(View.VISIBLE);

        activityTvshowDetailsBinding.setStatus(tvShow.getStatus());
        activityTvshowDetailsBinding.textStatus.setVisibility(View.VISIBLE);

        activityTvshowDetailsBinding.setStartedDate(tvShow.getStartDate());
        activityTvshowDetailsBinding.textStarted.setVisibility(View.VISIBLE);

        activityTvshowDetailsBinding.setTvShowImageUrl(
                tvShowDetails.getImagePath()
        );
        activityTvshowDetailsBinding.imageTVShow.setVisibility(View.VISIBLE);

        activityTvshowDetailsBinding.setDescription(
                String.valueOf(
                        HtmlCompat.fromHtml(tvShowDetails.getDescription(), HtmlCompat.FROM_HTML_MODE_LEGACY)
                )
        );
        activityTvshowDetailsBinding.textDescription.setVisibility(View.VISIBLE);

        activityTvshowDetailsBinding.textReadMore.setVisibility(View.VISIBLE);

//
        activityTvshowDetailsBinding.setRating(
                String.format(Locale.getDefault(), "%.2f", Double.parseDouble(tvShowDetails.getRating()))
        );

        if (tvShowDetails.getGenres() != null) {
            activityTvshowDetailsBinding.setGenre(tvShowDetails.getGenres()[0]);
        } else {
            activityTvshowDetailsBinding.setGenre("N/A");
        }

        activityTvshowDetailsBinding.setRuntime(tvShowDetails.getRuntime() + " Min");

        activityTvshowDetailsBinding.viewDivider1.setVisibility(View.VISIBLE);
        activityTvshowDetailsBinding.viewDivider2.setVisibility(View.VISIBLE);
        activityTvshowDetailsBinding.layoutMisc.setVisibility(View.VISIBLE);
        if (tvShow.getStatus().equals("Running")) {
            activityTvshowDetailsBinding.buttonPremium.setVisibility(View.VISIBLE);
        } else {
            activityTvshowDetailsBinding.buttonEpisodes.setVisibility(View.VISIBLE);
        }
        activityTvshowDetailsBinding.buttonVote.setVisibility(View.VISIBLE);
        activityTvshowDetailsBinding.imageWatchlist.setVisibility(View.VISIBLE);
    }

    private void handleClickReadMore() {
        activityTvshowDetailsBinding.textReadMore.setOnClickListener(v -> {
            if (activityTvshowDetailsBinding.textReadMore.getText().toString().equals("Read More")) {
                activityTvshowDetailsBinding.textDescription.setMaxLines(Integer.MAX_VALUE);
                activityTvshowDetailsBinding.textDescription.setEllipsize(null);
                activityTvshowDetailsBinding.textReadMore.setText(R.string.read_less);
            } else {
                activityTvshowDetailsBinding.textDescription.setMaxLines(4);
                activityTvshowDetailsBinding.textDescription.setEllipsize(TextUtils.TruncateAt.END);
                activityTvshowDetailsBinding.textReadMore.setText(R.string.read_more);
            }
        });
    }

    private void handleClickVoteButton() {
        activityTvshowDetailsBinding.buttonVote.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tvShowDetails.getUrl()));
            startActivity(intent);
        });
    }

    private void handleClickEpisodesButton() {
        activityTvshowDetailsBinding.buttonEpisodes.setOnClickListener(v -> {
            if (episodeBottomSheet == null) {
                episodeBottomSheet = new BottomSheetDialog(TVShowDetailsActivity.this);
                layoutEpisodesBottomSheetBinding = DataBindingUtil.inflate(
                        LayoutInflater.from(TVShowDetailsActivity.this),
                        R.layout.layout_episodes_bottom_sheet,
                        findViewById(R.id.episodesContainer),
                        false
                );
                episodeBottomSheet.setContentView(layoutEpisodesBottomSheetBinding.getRoot());
                layoutEpisodesBottomSheetBinding.episodesRecyclerView.setAdapter(
                        new EpisodesAdapter(tvShowDetails.getEpisodes(), this)
                );
                layoutEpisodesBottomSheetBinding.textHeader.setText(
                        String.format("Episode | %s", tvShow.getName())
                );
                layoutEpisodesBottomSheetBinding.imageClose.setOnClickListener(v1 -> episodeBottomSheet.dismiss());
            }

            FrameLayout frameLayout = episodeBottomSheet.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet
            );
            if (frameLayout != null) {
                BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(frameLayout);
                bottomSheetBehavior.setPeekHeight((int) (Resources.getSystem().getDisplayMetrics().heightPixels * 0.85));
            }

            episodeBottomSheet.show();
        });
    }

    private void handleClickPremiumButton() {
//        handle click premium button
        activityTvshowDetailsBinding.buttonPremium.setOnClickListener(v -> {
            if (paymentBottomSheet == null) {
                paymentBottomSheet = new BottomSheetDialog(TVShowDetailsActivity.this);
                layoutPaymentBottomSheetBinding = DataBindingUtil.inflate(
                        LayoutInflater.from(TVShowDetailsActivity.this),
                        R.layout.layout_payment_bottom_sheet,
                        findViewById(R.id.layoutPaymentContainer),
                        false
                );
                paymentBottomSheet.setContentView(layoutPaymentBottomSheetBinding.getRoot());
                layoutPaymentBottomSheetBinding.imageClose.setOnClickListener(v1 -> paymentBottomSheet.dismiss());
            }
            paymentBottomSheet.show();
            //        check input enough or nor
            layoutPaymentBottomSheetBinding.inputZipCode.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (layoutPaymentBottomSheetBinding.inputZipCode.getText().length() == 6 &&
                            layoutPaymentBottomSheetBinding.inputCardNum.getText().length() == 16 &&
                            layoutPaymentBottomSheetBinding.inputDate.getText().length() == 4 &&
                            layoutPaymentBottomSheetBinding.inputCVC.getText().length() == 3
                    ) {
                        layoutPaymentBottomSheetBinding.buttonPay.setAlpha(1);
                        layoutPaymentBottomSheetBinding.buttonPay.setEnabled(true);
                    } else {
                        layoutPaymentBottomSheetBinding.buttonPay.setAlpha(0.3f);
                        layoutPaymentBottomSheetBinding.buttonPay.setEnabled(false);
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
            //        handle pay button
            layoutPaymentBottomSheetBinding.buttonPay.setOnClickListener(view -> {
                paymentBottomSheet.dismiss();
                activityTvshowDetailsBinding.buttonPremium.setVisibility(View.GONE);
                activityTvshowDetailsBinding.buttonEpisodes.setVisibility(View.VISIBLE);

                Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setDuration(2500);
                activityTvshowDetailsBinding.buttonEpisodes.startAnimation(fadeIn);
            });
        });
    }

    private void handleClickWatchlistButton() {
        activityTvshowDetailsBinding.imageWatchlist.setOnClickListener(v -> {
            CompositeDisposable compositeDisposable = new CompositeDisposable();
            if (isAvailableInWatchlist) {
                isAvailableInWatchlist = false;
                compositeDisposable.add(tvShowDetailsViewModel.removeTVShowFromWatchlist(tvShow)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            activityTvshowDetailsBinding.imageWatchlist.setImageResource(R.drawable.ic_watchlist);
                            Toast.makeText(this, "Removed from the watchlist", Toast.LENGTH_SHORT).show();
                        })
                );
            } else {
                isAvailableInWatchlist = true;
                compositeDisposable.add(tvShowDetailsViewModel.addToWatchlist(tvShow)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            activityTvshowDetailsBinding.imageWatchlist.setImageResource(R.drawable.ic_added);
                            Toast.makeText(getApplicationContext(), "Added to watchlist", Toast.LENGTH_SHORT).show();
                        })
                );
            }
        });
    }

    @Override
    public void onEpisodeClicked(Episode episode) {
        Intent intent = new Intent(getApplicationContext(), VideoPlayerActivity.class);
        intent.putExtra("episodeUrl", "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4");
        startActivity(intent);
    }

}
