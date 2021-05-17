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
        if (feedResponse != null) {
            podcast = rssResponseToPodcast(feedUrl, "", feedResponse)
        }
        return podcast
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
}