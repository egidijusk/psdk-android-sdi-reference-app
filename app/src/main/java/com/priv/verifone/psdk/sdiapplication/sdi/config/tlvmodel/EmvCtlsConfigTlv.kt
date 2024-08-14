package com.priv.verifone.psdk.sdiapplication.sdi.config.tlvmodel

data class EmvCtlsConfigTlv(
    val ApplicationData: List<ApplicationData>,
    val terminal: Terminal
)