package com.priv.verifone.psdk.sdiapplication.sdi.config.tlvmodel

data class Drl(
    val name: String,
    val tag: String,
    val values: List<Value>
)