package com.priv.verifone.psdk.sdiapplication.sdi.config.tlvmodel

data class EmvContactConfigTlv(
    val applications: List<Application>,
    val terminal: Terminal
)