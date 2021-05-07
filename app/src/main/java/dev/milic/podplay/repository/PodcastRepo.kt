package dev.milic.podplay.repository

import dev.milic.podplay.model.Podcast

class PodcastRepo {

    fun getPodcast(feedUrl: String): Podcast? {
        return Podcast(feedUrl, "No Name", "No description", "No image")
    }
}