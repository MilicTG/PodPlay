package dev.milic.podplay.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dev.milic.podplay.model.Episode
import dev.milic.podplay.model.Podcast
import dev.milic.podplay.repository.PodcastRepo
import dev.milic.podplay.viewmodel.SearchViewModel.PodcastSummaryViewData
import java.util.*

class PodcastViewModel(application: Application) : AndroidViewModel(application) {

    var podcastRepo: PodcastRepo? = null
    var activePodcastViewData: PodcastViewData? = null


    private fun episodesToEpisodesView(episodes: List<Episode>): List<EpisodeViewData> {
        return episodes.map {
            EpisodeViewData(
                it.guid,
                it.title,
                it.description,
                it.mediaUrl,
                it.releaseDate,
                it.duration
            )
        }
    }

    private fun podcastToPodcastView(podcast: Podcast): PodcastViewData {
        return PodcastViewData(
            false,
            podcast.feedTitle,
            podcast.feedUrl,
            podcast.feedDesc,
            podcast.imageUrl,
            episodesToEpisodesView(podcast.episodes)
        )
    }

    fun getPodcast(podcastSummaryViewData: PodcastSummaryViewData): PodcastViewData? {
        val repo = podcastRepo ?: return null
        val feedUrl = podcastSummaryViewData.feedUrl ?: return null
        val podcast = repo.getPodcast(feedUrl)

        podcast?.let {
            it.feedTitle = podcastSummaryViewData.name ?: ""
            it.imageUrl = podcastSummaryViewData.imageUrl ?: ""
            activePodcastViewData = podcastToPodcastView(it)
            return activePodcastViewData
        }

        return null
    }

    data class PodcastViewData(
        var subscribed: Boolean = false,
        var feedTitle: String? = "",
        var feedUrl: String? = "",
        var feedDesc: String? = "",
        var imageUrl: String? = "",
        var episodes: List<EpisodeViewData>
    )

    data class EpisodeViewData(
        var guid: String? = "",
        var title: String? = "",
        var description: String? = "",
        var mediaUrl: String? = "",
        var releaseDate: Date? = null,
        var duration: String? = ""
    )
}