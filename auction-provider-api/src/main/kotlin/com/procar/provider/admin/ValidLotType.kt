package com.procar.provider.admin

import com.procar.provider.lot.LotType
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidLotTypeValidator::class])
annotation class ValidLotType(
    val message: String = "Invalid combination of lotType / auction / buyoutPrice",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class ValidLotTypeValidator : ConstraintValidator<ValidLotType, Any> {
    override fun isValid(value: Any?, ctx: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        val lotType = read(value, "lotType") as? LotType ?: return true
        val auction = read(value, "auction") as? AdminAuctionInfoRequest
        val buyoutPrice = read(value, "buyoutPrice") as? Double
        return when (lotType) {
            LotType.AUCTION -> auction != null && buyoutPrice == null
            LotType.BUYOUT  -> auction == null && buyoutPrice != null && buyoutPrice > 0
            LotType.HYBRID  -> auction != null && buyoutPrice != null && buyoutPrice > auction.startingBid
        }
    }
    private fun read(target: Any, name: String): Any? =
        target::class.members.firstOrNull { it.name == name }?.call(target)
}
