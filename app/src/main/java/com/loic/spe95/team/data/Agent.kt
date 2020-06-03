package com.loic.spe95.team.data

import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName

data class Agent(

    @field:SerializedName("id")
    val id: Int = 0,

    @get:PropertyName("specialties_member")
    @set:PropertyName("specialties_member")
    var specialtiesMember: Map<String, Boolean>? = HashMap(),

    @field:SerializedName("avatar")
    val avatar: String = "",

    @field:SerializedName("firstname")
    val firstname: String = "",

    @field:SerializedName("lastname")
    val lastname: String = "",

    @field:SerializedName("caserne_id")
    val caserneId: Int? = null
)