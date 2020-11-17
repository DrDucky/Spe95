package com.pomplarg.spe95.statistiques.data

import androidx.annotation.Keep

@Keep
data class Statistique(
    var motifs: HashMap<String?, Long?>? = HashMap(),
    var agentTypes: HashMap<String?, Long?>? = HashMap(),
    var agentTimes: HashMap<String?, Long?>? = HashMap(),
    var ipso: HashMap<String?, Long?>? = HashMap(),
    var nano: HashMap<String?, Long?>? = HashMap(),
    var nerone: HashMap<String?, Long?>? = HashMap(),
    var priaxe: HashMap<String?, Long?>? = HashMap()
)