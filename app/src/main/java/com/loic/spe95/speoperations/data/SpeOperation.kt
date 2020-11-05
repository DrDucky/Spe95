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

    @get:PropertyName("specialty")
    @set:PropertyName("specialty")
    var specialty: String = "",

    @field:SerializedName("id")
    var id: Int = 0,

    @field:SerializedName("type")
    var type: Int = 0,

    @field:SerializedName("motif")
    var motif: String = "",

    @field:SerializedName("address")
    var address: GeoPoint? = null,

    @field:SerializedName("address_offline")
    var addressOffline: String? = "",

    @get:PropertyName("caserne_id")
    @set:PropertyName("caserne_id")
    var caserneId: Int? = null,

    @get:PropertyName("start_date")
    @set:PropertyName("start_date")
    var startDate: Timestamp? = null,

    @get:PropertyName("unit_chief")
    @set:PropertyName("unit_chief")
    var unitChief: String? = "",

    @get:PropertyName("agentsOperation")
    @set:PropertyName("agentsOperation")
    var agentOnOperation: List<AgentOnOperation>? = ArrayList(),

    @get:PropertyName("materialsCyno")
    @set:PropertyName("materialsCyno")
    var materialsCyno: List<MaterialCyno>? = ArrayList(),

    @get:PropertyName("materialsSd")
    @set:PropertyName("materialsSd")
    var materialsSd: List<MaterialSd>? = ArrayList()
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