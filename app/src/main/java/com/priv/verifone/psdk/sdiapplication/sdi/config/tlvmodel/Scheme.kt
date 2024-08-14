package com.priv.verifone.psdk.sdiapplication.sdi.config.tlvmodel

data class Scheme(
    val drl: Drl,
    val fields: List<Field>,
    val name: String
)