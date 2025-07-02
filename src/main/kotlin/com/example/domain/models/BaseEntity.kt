package com.example.domain.models

import java.util.UUID

/**
 * Base interface for domain entities
 */
interface BaseEntity {
    val id: UUID
}
