package dev.milic.podplay.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import dev.milic.podplay.R
import dev.milic.podplay.databinding.FragmentPodcastDetailBinding
import dev.milic.podplay.viewmodel.PodcastViewModel

class PodcastDetailsFragment : Fragment() {

    private lateinit var binding: FragmentPodcastDetailBinding
    private val podcastViewModel: PodcastViewModel by activityViewModels()

    companion object {
        fun newInstance(): PodcastDetailsFragment {
            return PodcastDetailsFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
        updateControls()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_details, menu)
    }

    private fun updateControls() {
        val viewData = podcastViewModel.activePodcastViewData ?: return
        binding.feedTitleTextView.text = viewData.feedTitle
        binding.feedDescTextView.text = viewData.feedDesc
        activity?.let { activity ->
            Glide.with(activity).load(viewData.imageUrl).into(binding.feedImageView)
        }
    }
}