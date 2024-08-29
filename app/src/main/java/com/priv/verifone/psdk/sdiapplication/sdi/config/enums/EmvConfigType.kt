package com.priv.verifone.psdk.sdiapplication.sdi.config.enums

enum class EmvConfigType {

    INTEGER,
    STRING,
    ENCODED_HEX;

    companion object {
        fun parseConfigType(configType: String): EmvConfigType {
            return when (configType) {
                "transactionExponentType", "int" -> INTEGER

                "terminalType", "labelType", "merchantIdType",
                "merchantNameType", "emptyString", "string" -> STRING

                else -> ENCODED_HEX // "amount12", "amount12alt", "versionNumberType", allHex type
            }
        }
    }
}