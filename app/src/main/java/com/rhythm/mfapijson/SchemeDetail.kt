package com.rhythm.mfapijson

data class SchemeDetail(
    val meta: Meta,
    val data: List<NAVData>
)

data class Meta(
    val fund_house: String,
    val scheme_type: String,
    val scheme_category: String,
    val scheme_code: Int,
    val scheme_name: String
)

data class NAVData(
    val date: String,
    val nav: String
)