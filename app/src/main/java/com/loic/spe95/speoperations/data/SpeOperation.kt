package com.loic.spe95.speoperations.data

import android.widget.AutoCompleteTextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName
import com.loic.spe95.team.data.Agent
import com.loic.spe95.team.ui.AgentAutocompleteAdapter


data class SpeOperation(

    @get:PropertyName("specialty_id")
    @set:PropertyName("specialty_id")
    var specialtyId: Int = 0,

    @field:SerializedName("agents")
    var agents: List<Int> = ArrayList(),

    @field:SerializedName("id")
    var id: Int = 0,

    @field:SerializedName("type")
    var type: Int = 0,

    @field:SerializedName("motif")
    var motif: String = "",

    @field:SerializedName("address")
    var address: GeoPoint? = null,

    @get:PropertyName("caserne_id")
    @set:PropertyName("caserne_id")
    var caserneId: Int? = null,

    @get:PropertyName("start_date")
    @set:PropertyName("start_date")
    var startDate: Timestamp? = null,

    @get:PropertyName("unit_chief")
    @set:PropertyName("unit_chief")
    var unitChief: String? = ""

)

@BindingAdapter("entries")
fun AutoCompleteTextView.bindAdapter(entries: MutableLiveData<List<Agent>>?) {
    var listAgent: List<Agent> = arrayListOf<Agent>()
    if (entries?.value != null) {
        listAgent = entries.value!!
    }

    val adapter =
        AgentAutocompleteAdapter(this.context, android.R.layout.simple_list_item_1, listAgent)
    setAdapter(adapter)
}