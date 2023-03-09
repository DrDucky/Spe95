package com.pomplarg.spe95.speoperations.data

import android.widget.AutoCompleteTextView
import androidx.annotation.Keep
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.gson.annotations.SerializedName
import com.pomplarg.spe95.agent.data.Agent
import com.pomplarg.spe95.agent.ui.AgentAutocompleteAdapter

@Keep
data class SpeOperation(

    @field:SerializedName("specialty")
    var specialty: String = "",

    @field:SerializedName("id")
    var id: Long = 0,

    @field:SerializedName("idIntervention")
    var idIntervention: Int? = null,

    @field:SerializedName("type")
    var type: String = "",

    @field:SerializedName("motif")
    var motif: String = "",

    @field:SerializedName("address")
    var address: GeoPoint? = null,

    @field:SerializedName("addressOffline")
    var addressOffline: String? = "",

    @field:SerializedName("observations")
    var observations: String? = "",

    @field:SerializedName("caserneId")
    var caserneId: Int? = null,

    @field:SerializedName("startDate")
    var startDate: Timestamp? = null,

    @field:SerializedName("unitChief")
    var unitChief: String? = "",

    @field:SerializedName("agentOnOperation")
    var agentOnOperation: List<AgentOnOperation>? = ArrayList(),

    @field:SerializedName("materialsCyno")
    var materialsCyno: List<MaterialCyno>? = ArrayList(),

    @field:SerializedName("decisionsCyno")
    var decisionsCyno: List<DecisionCyno>? = ArrayList(),

    @field:SerializedName("materialsSd")
    var materialsSd: List<MaterialSd>? = ArrayList(),

    @field:SerializedName("enginsSd")
    var enginsSd: List<EnginSd>? = ArrayList(),

    @field:SerializedName("materialsRa")
    var materialsRa: List<MaterialRa>? = ArrayList(),

    @field:SerializedName("photoRa")
    var photoRa: String? = ""
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