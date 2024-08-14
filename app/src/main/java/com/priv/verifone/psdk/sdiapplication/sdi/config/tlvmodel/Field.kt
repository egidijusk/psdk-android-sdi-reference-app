package com.priv.verifone.psdk.sdiapplication.sdi.config.tlvmodel

data class Field(
    val name: String,
    val tag: String,
    val type: String,
    val value: String
)