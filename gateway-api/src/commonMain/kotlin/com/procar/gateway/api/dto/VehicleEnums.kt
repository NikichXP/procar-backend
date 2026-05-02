package com.procar.gateway.api.dto

import kotlinx.serialization.Serializable

@Serializable
enum class BodyType(val displayName: String) {
    SEDAN("Sedan"),
    SUV("SUV"),
    TRUCK("Truck"),
    COUPE("Coupe"),
    CONVERTIBLE("Convertible"),
    HATCHBACK("Hatchback"),
    WAGON("Wagon"),
    VAN("Van"),
    MINIVAN("Minivan"),
    OTHER("Other");

    override fun toString(): String = name
}

@Serializable
enum class Transmission(val displayName: String) {
    AUTOMATIC("Automatic"),
    MANUAL("Manual"),
    CVT("CVT"),
    DCT("DCT");

    override fun toString(): String = name
}

@Serializable
enum class Drivetrain(val displayName: String) {
    FWD("FWD"),
    RWD("RWD"),
    AWD("AWD"),
    FOUR_WD("4WD");

    override fun toString(): String = when (this) {
        FOUR_WD -> "4WD"
        else -> name
    }
}

@Serializable
enum class FuelType(val displayName: String) {
    GASOLINE("Gasoline"),
    DIESEL("Diesel"),
    ELECTRIC("Electric"),
    HYBRID("Hybrid"),
    PLUG_IN_HYBRID("Plug-in Hybrid");

    override fun toString(): String = when (this) {
        PLUG_IN_HYBRID -> "PLUG_IN_HYBRID"
        else -> name
    }
}

@Serializable
enum class VehicleCondition(val displayName: String) {
    EXCELLENT("Excellent"),
    GOOD("Good"),
    FAIR("Fair"),
    POOR("Poor"),
    DAMAGED("Damaged"),
    SALVAGE("Salvage");

    override fun toString(): String = name
}
