package com.loic.spe95.statistiques.data

data class Statistique(
    var motifs: HashMap<String?, Long?>? = HashMap(),
    var agentTypes: HashMap<String?, Long?>? = HashMap(),
    var agentTimes: HashMap<String?, Long?>? = HashMap()
)