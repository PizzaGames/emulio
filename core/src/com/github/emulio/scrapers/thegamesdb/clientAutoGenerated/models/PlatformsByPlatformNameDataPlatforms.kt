/**
* TheGamesDB API
* API Documentations
*
* The version of the OpenAPI document: 1.0.0
* 
*
* NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
* https://openapi-generator.tech
* Do not edit the class manually.
*/
package com.github.emulio.scrapers.thegamesdb.clientAutoGenerated.models


import com.squareup.moshi.Json



/**
 * 
 * @param console 
 * @param controller 
 * @param name 
 * @param icon 
 * @param alias 
 * @param developer 
 * @param id 
 */

data class PlatformsByPlatformNameDataPlatforms (
    @Json(name = "console")
    val console: kotlin.String? = null,
    @Json(name = "controller")
    val controller: kotlin.String? = null,
    @Json(name = "name")
    val name: kotlin.String? = null,
    @Json(name = "icon")
    val icon: kotlin.String? = null,
    @Json(name = "alias")
    val alias: kotlin.String? = null,
    @Json(name = "developer")
    val developer: kotlin.String? = null,
    @Json(name = "id")
    val id: kotlin.Int? = null
) {

}

