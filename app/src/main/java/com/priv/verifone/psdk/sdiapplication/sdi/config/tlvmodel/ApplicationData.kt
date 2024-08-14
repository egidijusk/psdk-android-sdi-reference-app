package com.priv.verifone.psdk.sdiapplication.sdi.config.tlvmodel

data class ApplicationData(
    val AID: String,
    val KernelID: String,
    val common: Common,
    val scheme: Scheme
)