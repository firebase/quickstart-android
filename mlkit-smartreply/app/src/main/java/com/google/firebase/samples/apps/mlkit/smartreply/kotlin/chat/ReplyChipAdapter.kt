package com.google.firebase.samples.apps.mlkit.smartreply.kotlin.chat

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion
import com.google.firebase.samples.apps.mlkit.smartreply.R

import java.util.ArrayList

class ReplyChipAdapter(private val listener: ClickListener) : RecyclerView.Adapter<ReplyChipAdapter.ViewHolder>() {

    private val suggestions = ArrayList<SmartReplySuggestion>()

    interface ClickListener {

        fun onChipClick(chipText: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.smart_reply_chip, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val suggestion = suggestions[position]
        holder.bind(suggestion)
    }

    override fun getItemCount(): Int {
        return suggestions.size
    }

    fun setSuggestions(suggestions: List<SmartReplySuggestion>) {
        this.suggestions.clear()
        this.suggestions.addAll(suggestions)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val text: TextView

        init {
            this.text = itemView.findViewById(R.id.smartReplyText)
        }

        fun bind(suggestion: SmartReplySuggestion) {
            text.text = suggestion.text
            itemView.setOnClickListener { listener.onChipClick(suggestion.text) }
        }
    }
}
