package com.nichols.astate_6.data

import androidx.annotation.DrawableRes

/**
 * Users character
 */
data class Player(
    override val name: String,
    override var health: Int,
    override var maxAttack: Int,
    val weaponName: String,
    val level: Int,
    override var isAlive: Boolean = true,
    @DrawableRes override val characterGif: Int
    ) : CharacterBase()
