package com.procar.core.config

import com.procar.gateway.api.dto.ApiDoc
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverter
import io.swagger.v3.core.converter.ModelConverterContext
import io.swagger.v3.oas.models.media.Schema
import org.springframework.stereotype.Component

@Component
class ApiDocModelConverter : ModelConverter {
    override fun resolve(
        annotatedType: AnnotatedType,
        context: ModelConverterContext,
        chain: Iterator<ModelConverter>,
    ): Schema<*>? {
        val schema = if (chain.hasNext()) chain.next().resolve(annotatedType, context, chain) else return null
        annotatedType.ctxAnnotations
            ?.filterIsInstance<ApiDoc>()
            ?.firstOrNull()
            ?.let { doc ->
                if (doc.example.isNotEmpty()) schema?.example = doc.example
                if (doc.description.isNotEmpty()) schema?.description = doc.description
            }
        return schema
    }
}
