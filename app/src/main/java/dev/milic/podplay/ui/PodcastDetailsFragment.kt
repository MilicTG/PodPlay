package dev.milic.podplay.ui

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.method.ScrollingMovementMethod
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import dev.milic.podplay.R
import dev.milic.podplay.adapter.EpisodeListAdapter
import dev.milic.podplay.databinding.FragmentPodcastDetailBinding
import dev.milic.podplay.service.PodplayMediaService
import dev.milic.podplay.viewmodel.PodcastViewModel
import java.lang.RuntimeException

class PodcastDetailsFragment : Fragment() {

    private lateinit var binding: FragmentPodcastDetailBinding
    private lateinit var episodeListAdapter: EpisodeListAdapter
    private lateinit var mediaBrowserCompat: MediaBrowserCompat
    private var mediaControllerCallback: MediaControllerCallback? = null
    private val podcastViewModel: PodcastViewModel by activityViewModels()
    private var listener: OnPodcastDetailsListener? = null

    companion object {
        fun newInstance(): PodcastDetailsFragment {
            return PodcastDetailsFragment()
        }
    }

    override fun onStart() {
        super.onStart()
        if (mediaBrowserCompat.isConnected) {
            val fragmentActivity = activity as FragmentActivity
            if (MediaControllerCompat.getMediaController(fragmentActivity) == null) {
                registerMediaController(mediaBrowserCompat.sessionToken)
            }
        } else {
            mediaBrowserCompat.connect()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        initMediaBrowser()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPodcastDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        podcastViewModel.podcastLiveData.observe(viewLifecycleOwner, { viewData ->
            if (viewData != null) {
                binding.feedTitleTextView.text = viewData.feedTitle
                binding.feedDescTextView.text = viewData.feedDesc
                activity?.let { activity ->
                    Glide.with(activity).load(viewData.imageUrl).into(binding.feedImageView)
                }
                binding.feedDescTextView.movementMethod = ScrollingMovementMethod()
                binding.episodeRecyclerView.setHasFixedSize(true)

                val layoutManager = LinearLayoutManager(activity)
                binding.episodeRecyclerView.layoutManager = layoutManager

                val dividerItemDecoration = DividerItemDecoration(
                    binding.episodeRecyclerView.context,
                    layoutManager.orientation
                )
                binding.episodeRecyclerView.addItemDecoration(dividerItemDecoration)

                episodeListAdapter = EpisodeListAdapter(viewData.episodes)
                binding.episodeRecyclerView.adapter = episodeListAdapter

                activity?.invalidateOptionsMenu()
            }
        })

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnPodcastDetailsListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnPodcastDetailsListener")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_details, menu)
    }

    override fun onStop() {
        super.onStop()
        val fragmentActivity = activity as FragmentActivity
        if (MediaControllerCompat.getMediaController(fragmentActivity) != null) {
            mediaControllerCallback?.let {
                MediaControllerCompat.getMediaController(fragmentActivity).unregisterCallback(it)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_feed_action -> {
                podcastViewModel.podcastLiveData.value?.feedUrl?.let {
                    listener?.onSubscribe()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        podcastViewModel.podcastLiveData.observe(viewLifecycleOwner, { podcast ->
            if (podcast != null) {
                menu.findItem(R.id.menu_feed_action).title =
                    if (podcast.subscribed) getString(R.string.unsubscribe) else getString(R.string.subscribe)
            }
        })
        super.onPrepareOptionsMenu(menu)
    }

    private fun registerMediaController(token: MediaSessionCompat.Token) {
        //activity can go null use this to prevent that
        val fragmentActivity = activity as FragmentActivity
        val mediaController = MediaControllerCompat(fragmentActivity, token)
        mediaControllerCallback = MediaControllerCallback()
        mediaController.registerCallback(mediaControllerCallback!!)
    }

    private fun initMediaBrowser() {
        val fragmentActivity = activity as FragmentActivity

        mediaBrowserCompat = MediaBrowserCompat(
            fragmentActivity, ComponentName(
                fragmentActivity, PodplayMediaService::class.java
            ), MediaBrowserCallbacks(), null
        )
    }

    interface OnPodcastDetailsListener {
        fun onSubscribe()
        fun onUnsubscribe()
    }

    inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
        }
    }

    inner class MediaBrowserCallbacks : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            registerMediaController(mediaBrowserCompat.sessionToken)
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            //TODO disable
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            //TODO handle
        }
    }
}