package com.github.matthews8.placeswishlist.database

import androidx.room.*
import com.google.android.gms.common.util.Hex
import com.google.android.gms.maps.model.LatLng

@Entity(
    tableName = "city_table",
    indices = [Index(value = ["name", "country"], //TODO test whether unique is the name+country set
        unique = true)], //  or each is unique -- in this case it-s a problem
)
data class City(
    @PrimaryKey(autoGenerate = true)
    var cityId: Long = 0L,

    @ColumnInfo(name = "latitude")
    var lat: Double,
    //TODO we can use the Geocoding API to get the LatLng each time
    @ColumnInfo(name = "longitude")
    var lng: Double,

    @ColumnInfo
    var name: String,

    @ColumnInfo
    var country: String,

    @ColumnInfo
    var visited: Boolean = false,

   /* @Ignore
    var imgeUrl: String? = null,*/

    //TODO se dovessi fare un altra tabella per le cose ricevute
    // tramite blietooth dovrei poi inserie in quella tabella il colore associato alla persona
    // e non qui. Inoltre received_from sarenne semplicemente una foreign key
    /*var receivedFrom: String? = null,
    var markerColor: String? = null*/
)