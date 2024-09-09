package com.example.licenseplaterec

import com.google.gson.annotations.SerializedName

data class HistoryInfo(
    @SerializedName("kenteken"                                     ) var kenteken                                 : String? = null,
    @SerializedName("brandstof_omschrijving"                       ) var brandstofOmschrijving                    : String? = null,
    @SerializedName("canpark"                                      ) var canpark                                  : Boolean? = null
)
