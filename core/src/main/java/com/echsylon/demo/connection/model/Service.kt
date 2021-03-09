package com.echsylon.demo.connection.model

data class Service(
    override val uuid: String,
    override val name: String? = null
) : Feature
