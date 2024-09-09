package com.example.licenseplaterec

import com.google.gson.annotations.SerializedName

data class CarInfo(

    @SerializedName("kenteken"                                     ) var kenteken                                 : String? = null,
    @SerializedName("brandstof_volgnummer"                         ) var brandstofVolgnummer                      : String? = null,
    @SerializedName("brandstof_omschrijving"                       ) var brandstofOmschrijving                    : String? = null,
    @SerializedName("co2_uitstoot_gewogen"                         ) var co2UitstootGewogen                       : String? = null,
    @SerializedName("geluidsniveau_rijdend"                        ) var geluidsniveauRijdend                     : String? = null,
    @SerializedName("geluidsniveau_stationair"                     ) var geluidsniveauStationair                  : String? = null,
    @SerializedName("emissiecode_omschrijving"                     ) var emissiecodeOmschrijving                  : String? = null,
    @SerializedName("milieuklasse_eg_goedkeuring_licht"            ) var milieuklasseEgGoedkeuringLicht           : String? = null,
    @SerializedName("nettomaximumvermogen"                         ) var nettomaximumvermogen                     : String? = null,
    @SerializedName("toerental_geluidsniveau"                      ) var toerentalGeluidsniveau                   : String? = null,
    @SerializedName("emis_deeltjes_type1_wltp"                     ) var emisDeeltjesType1Wltp                    : String? = null,
    @SerializedName("emis_co2_gewogen_gecombineerd_wltp"           ) var emisCo2GewogenGecombineerdWltp           : String? = null,
    @SerializedName("brandstof_verbruik_gewogen_gecombineerd_wltp" ) var brandstofVerbruikGewogenGecombineerdWltp : String? = null,
    @SerializedName("klasse_hybride_elektrisch_voertuig"           ) var klasseHybrideElektrischVoertuig          : String? = null,
    @SerializedName("uitlaatemissieniveau"                         ) var uitlaatemissieniveau                     : String? = null

)
