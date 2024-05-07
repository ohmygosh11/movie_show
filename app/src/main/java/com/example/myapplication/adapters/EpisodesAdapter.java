package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ItemContainerEpisodeBinding;
import com.example.myapplication.listeners.EpisodesListener;
import com.example.myapplication.models.Episode;

import java.util.List;

public class EpisodesAdapter extends RecyclerView.Adapter<EpisodesAdapter.EpisodeViewHolder> {
    private List<Episode> episodes;
    private EpisodesListener episodesListener;

    public EpisodesAdapter(List<Episode> episodes, EpisodesListener episodesListener) {
        this.episodes = episodes;
        this.episodesListener = episodesListener;
    }

    @NonNull
    @Override
    public EpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemContainerEpisodeBinding itemContainerEpisodeBinding = DataBindingUtil.inflate(
                layoutInflater, R.layout.item_container_episode, parent, false
        );
        return new EpisodeViewHolder(itemContainerEpisodeBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeViewHolder holder, int position) {
        holder.bind(episodes.get(position));
    }

    @Override
    public int getItemCount() {
        return episodes.size();
    }

    public class EpisodeViewHolder extends RecyclerView.ViewHolder {
        private ItemContainerEpisodeBinding itemContainerEpisodeBinding;
        public EpisodeViewHolder(ItemContainerEpisodeBinding itemContainerEpisodeBinding) {
            super(itemContainerEpisodeBinding.getRoot());
            this.itemContainerEpisodeBinding = itemContainerEpisodeBinding;
        }
        public void bind(Episode episode) {
            String title = "SEASON ";
            String seasonNum = episode.getSeason();
            if (seasonNum.length() == 1) {
                seasonNum = "0".concat(seasonNum);
            }
            String episodeNum = episode.getEpisode();
            if (episodeNum.length() == 1) {
                episodeNum = "0".concat(episodeNum);
            }
            title = title.concat(seasonNum).concat(" - EPISODE ").concat(episodeNum);
            itemContainerEpisodeBinding.setTitle(title);
            itemContainerEpisodeBinding.setName(episode.getName());
            itemContainerEpisodeBinding.setAirDate(episode.getAirDate());
            itemContainerEpisodeBinding.getRoot().setOnClickListener(v -> {
                episodesListener.onEpisodeClicked(episode);
            });
        }
    }
}
