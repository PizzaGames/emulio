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
 * @param code 
 * @param status 
 * @param &#x60;data&#x60; 
 * @param remainingMonthlyAllowance 
 * @param extraAllowance 
 */

data class Platforms (
    @Json(name = "code")
    val code: kotlin.Int,
    @Json(name = "status")
    val status: kotlin.String,
    @Json(name = "data")
    val `data`: PlatformsData,
    @Json(name = "remaining_monthly_allowance")
    val remainingMonthlyAllowance: kotlin.Int,
    @Json(name = "extra_allowance")
    val extraAllowance: kotlin.Int
) {

}

