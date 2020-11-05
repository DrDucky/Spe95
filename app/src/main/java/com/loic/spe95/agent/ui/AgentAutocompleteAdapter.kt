package com.loic.spe95.agent.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.loic.spe95.agent.data.Agent


class AgentAutocompleteAdapter(
    context: Context, @LayoutRes private val layoutResource: Int,
    private val allAgents: List<Agent>
) : ArrayAdapter<Agent>(context, layoutResource, allAgents), Filterable {

    private var agents: List<Agent> = allAgents

    override fun getCount(): Int {
        return agents.size
    }

    override fun getItem(p0: Int): Agent? {
        return agents.get(p0)
    }

    override fun getItemId(p0: Int): Long {
        return agents.get(p0).id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: TextView = convertView as TextView? ?: LayoutInflater.from(context).inflate(
            layoutResource,
            parent,
            false
        ) as TextView
        view.text = "${agents[position].firstname} ${agents[position].lastname}"
        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(
                charSequence: CharSequence?,
                filterResults: Filter.FilterResults
            ) {
                agents = filterResults.values as List<Agent>
                notifyDataSetChanged()
            }

            override fun performFiltering(charSequence: CharSequence?): Filter.FilterResults {
                val queryString = charSequence?.toString()?.toLowerCase()

                val filterResults = Filter.FilterResults()
                filterResults.values = if (queryString == null || queryString.isEmpty())
                    allAgents
                else
                    allAgents.filter {
                        it.firstname.toLowerCase().contains(queryString) ||
                                it.lastname.toLowerCase().contains(queryString)
                    }
                return filterResults
            }
        }
    }
}