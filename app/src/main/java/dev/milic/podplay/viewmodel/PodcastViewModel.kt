package dev.milic.podplay.viewmodel

import android.app.Application
import androidx.lifecycle.*
import dev.milic.podplay.db.PodPlayDatabase
import dev.milic.podplay.db.PodcastDao
import dev.milic.podplay.model.Episode
import dev.milic.podplay.model.Podcast
import dev.milic.podplay.repository.PodcastRepo
import dev.milic.podplay.util.DateUtils
import dev.milic.podplay.viewmodel.SearchViewModel.PodcastSummaryViewData
import kotlinx.coroutines.launch
import java.util.*

class PodcastViewModel(application: Application) : AndroidViewModel(application) {

    private var activePodcast: Podcast? = null

    var podcastRepo: PodcastRepo? = null
    val podcastDao: PodcastDao =
        PodPlayDatabase.getInstance(application, viewModelScope).podcastDao()

    var livePodcastSummaryViewData: LiveData<List<PodcastSummaryViewData>>? = null

    private val _podcastLiveData = MutableLiveData<PodcastViewData?>()
    val podcastLiveData: LiveData<PodcastViewData?> = _podcastLiveData

    fun getPodcast(podcastSummaryViewData: PodcastSummaryViewData) {
        podcastSummaryViewData.feedUrl?.let { url ->
            viewModelScope.launch {
                podcastRepo?.getPodcast(url)?.let {
                    it.feedTitle = podcastSummaryViewData.name ?: ""
                    it.imageUrl = podcastSummaryViewData.imageUrl ?: ""
                    _podcastLiveData.value = podcastToPodcastView(it)
                    activePodcast = it
                } ?: run {
                    _podcastLiveData.value = null
                }
            }
        } ?: run {
            _podcastLiveData.value = null
        }
    }

    fun getPodcast(): LiveData<List<PodcastSummaryViewData>>? {
        val repo = podcastRepo ?: return null

        if (livePodcastSummaryViewData == null) {
            val liveData = repo.getAll()

            livePodcastSummaryViewData = Transformations.map(liveData) { podcastList ->
                podcastList.map { podcast ->
                    podcastToSummaryView(podcast)
                }
            }
        }
        return livePodcastSummaryViewData
    }

    fun saveActivePodcast() {
        val repo = podcastRepo ?: return
        activePodcast?.let {
            repo.save(it)
        }
    }

    private fun podcastToPodcastView(podcast: Podcast): PodcastViewData {
        return PodcastViewData(
            false, podcast.feedTitle, podcast.feedUrl, podcast.feedDesc,
            podcast.imageUrl, episodesToEpisodesView(podcast.episodes)
        )
    }

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

    private fun podcastToSummaryView(podcast: Podcast): PodcastSummaryViewData {
        return PodcastSummaryViewData(
            podcast.feedTitle,
            DateUtils.dateToShortDate(podcast.lastUpdated),
            podcast.imageUrl,
            podcast.feedUrl
        )
    }

    data class PodcastViewData(
        var subscribed: Boolean = false, var feedTitle: String? = "",
        var feedUrl: String? = "", var feedDesc: String? = "",
        var imageUrl: String? = "", var episodes: List<EpisodeViewData>
    )

    data class EpisodeViewData(
        var guid: String? = "", var title: String? = "",
        var description: String? = "", var mediaUrl: String? = "",
        var releaseDate: Date? = null, var duration: String? = ""
    )
}
