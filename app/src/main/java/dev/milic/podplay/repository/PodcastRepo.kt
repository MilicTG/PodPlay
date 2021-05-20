package dev.milic.podplay.repository

import androidx.lifecycle.LiveData
import dev.milic.podplay.db.PodcastDao
import dev.milic.podplay.model.Episode
import dev.milic.podplay.model.Podcast
import dev.milic.podplay.service.RssFeedResponse
import dev.milic.podplay.service.RssFeedService
import dev.milic.podplay.util.DateUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class PodcastRepo(private var feedService: RssFeedService, private var podcastDao: PodcastDao) {

    fun save(podcast: Podcast) {
        GlobalScope.launch {
            val podcastId = podcastDao.insertPodcast(podcast)
            for (episode in podcast.episodes) {
                episode.podcastId = podcastId
                podcastDao.insertEpisode(episode)
            }
        }
    }

    fun getAll(): LiveData<List<Podcast>> {
        return podcastDao.loadPodcasts()
    }

    suspend fun getPodcast(feedUrl: String): Podcast? {
        var podcast: Podcast? = null
        val feedResponse = feedService.getFeed(feedUrl)
        val podcastLocal = podcastDao.loadPodcast(feedUrl)

        if (podcastLocal != null) {
            podcastLocal.id?.let {
                podcastLocal.episodes = podcastDao.loadEpisodes(it)
                return podcastLocal
            }
        }

        if (feedResponse != null) {
            podcast = rssResponseToPodcast(feedUrl, "", feedResponse)
        }
        return podcast
    }

    fun delete(podcast: Podcast) {
        GlobalScope.launch {
            podcastDao.deletePodcast(podcast)
        }
    }

    private fun rssItemsToEpisodes(episodeResponses: List<RssFeedResponse.EpisodeResponse>): List<Episode> {
        return episodeResponses.map {
            Episode(
                it.guid ?: "",
                null,
                it.title ?: "",
                it.description ?: "",
                it.url ?: "",
                it.type ?: "",
                DateUtils.xmlDateToDate(it.pubDate),
                it.duration ?: ""
            )
        }
    }

    private fun rssResponseToPodcast(
        feedUrl: String, imageUrl: String, rssResponse: RssFeedResponse
    ): Podcast? {
        val items = rssResponse.episodes ?: return null
        val description = if (rssResponse.description == "")
            rssResponse.summary else rssResponse.description

        return Podcast(
            null, feedUrl, rssResponse.title, description, imageUrl,
            rssResponse.lastUpdated, episodes = rssItemsToEpisodes(items)
        )
    }

    private suspend fun getNewEpisodes(localPodcast: Podcast): List<Episode> {
        val response = feedService.getFeed(localPodcast.feedUrl)

        if (response != null) {
            val remotePodcast =
                rssResponseToPodcast(localPodcast.feedUrl, localPodcast.imageUrl, response)
            remotePodcast?.let {
                val localEpisodes = podcastDao.loadEpisodes(localPodcast.id!!)
                return remotePodcast.episodes.filter { episode ->
                    localEpisodes.find { episode.guid == it.guid } == null
                }
            }
        }

        return listOf()
    }

    private fun saveNewEpisodes(podcastId: Long, episodes: List<Episode>) {
        GlobalScope.launch {
            for (episode in episodes) {
                episode.podcastId = podcastId
                podcastDao.insertEpisode(episode)
            }
        }
    }

    suspend fun updatePodcastEpisodes(): MutableList<PodcastUpdateInfo> {
        val updatedPodcast: MutableList<PodcastUpdateInfo> = mutableListOf()
        val podcasts = podcastDao.loadPodcastStatic()

        for (podcast in podcasts) {
            val newEpisode = getNewEpisodes(podcast)
            if (newEpisode.count() > 0) {
                podcast.id?.let {
                    saveNewEpisodes(it, newEpisode)
                    updatedPodcast.add(
                        PodcastUpdateInfo(
                            podcast.feedUrl, podcast.feedTitle, newEpisode.count()
                        )
                    )
                }
            }
        }

        return updatedPodcast
    }

    inner class PodcastUpdateInfo(
        val feedUrl: String,
        val name: String,
        val newCount: Int
    )
}