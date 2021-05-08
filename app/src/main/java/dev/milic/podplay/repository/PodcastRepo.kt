package dev.milic.podplay.repository

import dev.milic.podplay.model.Podcast
import dev.milic.podplay.service.RssFeedService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PodcastRepo {

    fun getPodcast(feedUrl: String): Podcast? {
        val rssFeedService = RssFeedService.instance
        GlobalScope.launch {
            rssFeedService.getFeed(feedUrl)
        }

        return Podcast(feedUrl, "No Name", "No description", "No image")
    }
}