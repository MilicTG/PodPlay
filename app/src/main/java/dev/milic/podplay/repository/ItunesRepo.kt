package dev.milic.podplay.repository

import dev.milic.podplay.service.ItunesService

class ItunesRepo(private val itunesService: ItunesService) {

    suspend fun searchTerm(term: String) = itunesService.searchPodcastByTerm(term)
}