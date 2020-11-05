package com.loic.spe95.agent.data

import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName

data class Agent(

    @field:SerializedName("id")
    var id: String = "",

    @get:PropertyName("specialties_member")
    @set:PropertyName("specialties_member")
    var specialtiesMember: Map<String, Boolean>? = HashMap(),

    @field:SerializedName("avatar")
    val avatar: String = "",

    @field:SerializedName("firstname")
    var firstname: String = "",

    @field:SerializedName("lastname")
    var lastname: String = "",

    @field:SerializedName("caserne_id")
    val caserneId: Int? = null
)