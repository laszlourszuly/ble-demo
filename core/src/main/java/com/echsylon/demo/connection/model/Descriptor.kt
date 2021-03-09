package com.echsylon.demo.connection.model

data class Descriptor(
    override val uuid: String,
    override val name: String? = null,
    var value: String? = null
) : Feature
