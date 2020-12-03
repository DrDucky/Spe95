package com.pomplarg.spe95.speoperations.data

import androidx.annotation.Keep

@Keep
data class MaterialSd(var name: String? = "", var quantity: Int = 0)

data class AlertStock(var name: String? = "", var threshold: Int = 0)