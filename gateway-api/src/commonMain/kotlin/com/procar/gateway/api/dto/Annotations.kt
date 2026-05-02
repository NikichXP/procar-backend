package com.procar.gateway.api.dto

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiDoc(val example: String = "", val description: String = "")
