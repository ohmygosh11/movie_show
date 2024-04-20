package com.example.myapplication.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.myapplication.adapters.ImageSliderAdapter;
import com.example.myapplication.databinding.ActivityTvshowDetailsBinding;
import com.example.myapplication.models.TVShowDetails;
import com.example.myapplication.viewmodels.TVShowDetailsViewModel;

public class TVShowDetailsActivity extends AppCompatActivity {
    private ActivityTvshowDetailsBinding activityTvshowDetailsBinding;
    private TVShowDetailsViewModel tvShowDetailsViewModel;
    private TVShowDetails tvShowDetails;

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
        getTVShowDetails();
    }

    private void getTVShowDetails() {
        activityTvshowDetailsBinding.setIsLoading(true);
        String tvShowId = String.valueOf(getIntent().getIntExtra("id", -1));
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
        activityTvshowDetailsBinding.setTvShowName(getIntent().getStringExtra("name"));
        activityTvshowDetailsBinding.textName.setVisibility(View.VISIBLE);

        activityTvshowDetailsBinding.setNetworkCountry(
                getIntent().getStringExtra("network") + " (" + getIntent().getStringExtra("country") + ")"
        );
        activityTvshowDetailsBinding.textNetworkCountry.setVisibility(View.VISIBLE);

        activityTvshowDetailsBinding.setStatus(getIntent().getStringExtra("status"));
        activityTvshowDetailsBinding.textStatus.setVisibility(View.VISIBLE);

        activityTvshowDetailsBinding.setStartedDate(getIntent().getStringExtra("startDate"));
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
}