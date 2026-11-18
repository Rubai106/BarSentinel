package com.barsentinel.data.local

import androidx.room.Entity
import androidx.room.Index

/**
 * Core entities (spec §13). Room on device is the source of truth for the
 * advocate's own data. Status and type fields are stored as strings; the
 * enum mapping lives in the repository layer (Phase 3), keeping the schema
 * stable across app upgrades — a migration that breaks chain verification
 * is a release blocker.
 *
 * Data rule: no client names, case numbers or matter details in any entity.
 */

/** The advocate using this device. No professional credential is stored as "verified". */
@Entity(tableName = "users")
data class UserEntity(
    @androidx.room.PrimaryKey val id: String,
    val displayName: String,
    val phone: String,
    val language: String,
    val createdAtEpochMillis: Long
)

/** A buddy candidate. Entered explicitly in-app or from contacts with permission; never uploaded wholesale. */
@Entity(tableName = "contacts")
data class ContactEntity(
    @androidx.room.PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val relationshipLabel: String
)

/**
 * The single unit of protection. Composite identity (id, version): the
 * version increments on every material edit and old versions are retained,
 * so buddy acceptance always refers to an exact version.
 */
@Entity(
    tableName = "envelopes",
    primaryKeys = ["id", "version"],
    indices = [Index(value = ["ownerId"]), Index(value = ["buddyId"])]
)
data class EnvelopeEntity(
    val id: String,
    val ownerId: String,
    val version: Int,
    /** DRAFT, PROPOSED, LIVE, ACTIVE, ESCALATING, CLOSED_SAFE, CLOSED_SOS, EXPIRED */
    val status: String,
    val type: String,
    val venueCategory: String,
    /** Area label only — never a client address. */
    val areaLabel: String,
    val startEpochMillis: Long,
    val expectedEndEpochMillis: Long,
    val intervalMinutes: Int,
    val graceMinutes: Int,
    val tier: String,
    val rulesetVersion: String,
    val buddyId: String?,
    val secondContactId: String?,
    val createdAtEpochMillis: Long
)
