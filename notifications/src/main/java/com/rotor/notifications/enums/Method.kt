package com.rotor.notifications.enums

/**
 * Created by efraespada on 18/03/2018.
 */
enum class Method(methodName: String) {
    ADD("add"),
    REMOVE("remove");

    val methodName: String

    init {
        this.methodName = methodName
    }

    fun getMethod() : String {
        return methodName
    }
}