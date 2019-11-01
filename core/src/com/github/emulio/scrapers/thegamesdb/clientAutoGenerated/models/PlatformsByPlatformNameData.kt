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
 * @param count 
 * @param platforms 
 */

data class PlatformsByPlatformNameData (
    @Json(name = "count")
    val count: kotlin.Int,
    @Json(name = "platforms")
    val platforms: kotlin.collections.List<PlatformsByPlatformNameDataPlatforms>
) {

}

