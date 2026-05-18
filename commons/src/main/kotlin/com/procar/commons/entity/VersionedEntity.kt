package com.procar.commons.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import java.time.LocalDateTime

abstract class VersionedEntity(
    @Version
    open var version: Int? = null,
    
    @CreatedDate
    open var createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    open var updatedAt: LocalDateTime = LocalDateTime.now()
)
