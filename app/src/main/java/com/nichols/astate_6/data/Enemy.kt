package com.nichols.astate_6.data

import androidx.annotation.DrawableRes

/**
 * An opponent
 */
data class Enemy(
    override val name: String,
    override var health: Int,
    override var maxAttack: Int,
    val warCry: String,
    override var isAlive: Boolean = true,
    @DrawableRes override val characterGif: Int
    ) : CharacterBase()