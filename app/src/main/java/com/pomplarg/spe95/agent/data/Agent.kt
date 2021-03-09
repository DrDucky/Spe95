package com.pomplarg.spe95.agent.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Agent(

    @field:SerializedName("id")
    var id: String = "",

    @field:SerializedName("specialtiesMember")
    var specialtiesMember: Map<String, Boolean>? = HashMap(),

    @field:SerializedName("avatar")
    var avatar: String = "",

    @field:SerializedName("firstname")
    var firstname: String = "",

    @field:SerializedName("lastname")
    var lastname: String = "",

    @field:SerializedName("caserne_id")
    val caserneId: Int? = null
)