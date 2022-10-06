package com.nichols.astate_6.data

/**
 * Base class for all characters
 */
abstract class CharacterBase {
    abstract val name: String
    abstract var health: Int
    abstract var maxAttack: Int
    abstract val isAlive: Boolean
    abstract val characterGif: Int
}