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
 * @param small 
 * @param original 
 * @param large 
 * @param thumb 
 * @param croppedCenterThumb 
 * @param medium 
 */

data class GamesByGameIDIncludeBoxartBaseUrl (
    @Json(name = "small")
    val small: kotlin.String,
    @Json(name = "original")
    val original: kotlin.String,
    @Json(name = "large")
    val large: kotlin.String,
    @Json(name = "thumb")
    val thumb: kotlin.String,
    @Json(name = "cropped_center_thumb")
    val croppedCenterThumb: kotlin.String,
    @Json(name = "medium")
    val medium: kotlin.String
) {

}

