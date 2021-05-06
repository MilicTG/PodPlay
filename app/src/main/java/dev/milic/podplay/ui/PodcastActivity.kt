package dev.milic.podplay.ui

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dev.milic.podplay.R
import dev.milic.podplay.adapter.PodcastListAdapter
import dev.milic.podplay.databinding.ActivityPodcastBinding
import dev.milic.podplay.repository.ItunesRepo
import dev.milic.podplay.service.ItunesService
import dev.milic.podplay.viewmodel.SearchViewModel


class PodcastActivity : AppCompatActivity(), PodcastListAdapter.PodcastListAdapterListener {

    private lateinit var binding: ActivityPodcastBinding
    private val searchViewModel by viewModels<SearchViewModel>()
    private lateinit var podcastListAdapter: PodcastListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPodcastBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewModels()
        updateControls()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search, menu)

        val searchMenuItem = menu.findItem(R.id.search_item)
        val searchView = searchMenuItem?.actionView as SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        return true
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onShowDetails(podcastSummaryViewData: SearchViewModel.PodcastSummaryViewData) {
        TODO("Not yet implemented")
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query =
                intent.getStringExtra(SearchManager.QUERY) ?: return
        }
    }

    private fun setupViewModels() {
        val service = ItunesService.instance
        searchViewModel.iTunesRepo = ItunesRepo(service)
    }

    private fun updateControls() {
        val layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration =
            DividerItemDecoration(binding.podcastRecyclerView.context, layoutManager.orientation)
        podcastListAdapter = PodcastListAdapter(null, this, this)

        binding.podcastRecyclerView.setHasFixedSize(true)
        binding.podcastRecyclerView.layoutManager = layoutManager
        binding.podcastRecyclerView.addItemDecoration(dividerItemDecoration)
        binding.podcastRecyclerView.adapter = podcastListAdapter
    }
}