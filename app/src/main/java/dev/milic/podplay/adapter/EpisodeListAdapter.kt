package dev.milic.podplay.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.milic.podplay.databinding.EpisodeItemBinding
import dev.milic.podplay.util.DateUtils
import dev.milic.podplay.util.HtmlUtils
import dev.milic.podplay.viewmodel.PodcastViewModel.*

class EpisodeListAdapter(
    private var episodeViewList: List<EpisodeViewData>?,
    private val episodeListAdapterListener: EpisodeListAdapterListener
) : RecyclerView.Adapter<EpisodeListAdapter.ViewHolder>() {

    interface EpisodeListAdapterListener {
        fun onSelectedEpisode(episodeViewData: EpisodeViewData)
    }

    inner class ViewHolder(
        binding: EpisodeItemBinding,
        val episodeListAdapterListener: EpisodeListAdapterListener
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                episodeViewData?.let {
                    episodeListAdapterListener.onSelectedEpisode(it)
                }
            }
        }

        var episodeViewData: EpisodeViewData? = null
        val titleTextView: TextView = binding.titleView
        val descTextView: TextView = binding.descView
        val durationTextView: TextView = binding.durationView
        val releaseDateTextView: TextView = binding.releaseDateView
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EpisodeListAdapter.ViewHolder {
        return ViewHolder(
            EpisodeItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), episodeListAdapterListener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val episodeViewList = episodeViewList ?: return
        val episodeView = episodeViewList[position]

        holder.episodeViewData = episodeView
        holder.titleTextView.text = episodeView.title
        holder.descTextView.text = HtmlUtils.htmlToSpannable(episodeView.description ?: "")
        holder.durationTextView.text = episodeView.duration
        holder.releaseDateTextView.text = episodeView.releaseDate?.let {
            DateUtils.dateToShortDate(it)
        }
    }

    override fun getItemCount(): Int {
        return episodeViewList?.size ?: 0
    }
}