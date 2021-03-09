package com.echsylon.demo.connection.model

interface Feature {
    val uuid: String
    val name: String?
    val label: String
        get() = name ?: uuid
}
