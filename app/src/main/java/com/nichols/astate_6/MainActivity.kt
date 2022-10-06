package com.nichols.astate_6

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.nichols.astate_6.data.Enemy
import com.nichols.astate_6.data.Player
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private var _level = 1
    private var _isDefending = false
    private lateinit var _enemy: Enemy
    private lateinit var _player: Player

    /**
     * Stretch goal #4: Understand the narrative/theme of your game through the language and aesthetic of your app.
     * The theme is mid-century fighting
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initGame()
    }

    /**
     * Sets up player, enemy, resets scores, levels, and descriptive UI
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun initGame() {
        val lvlMsg = "Level $_level"
        tv_level.text = lvlMsg

        //Goblin appears every odd level otherwise wizard
        val isGoblin = _level % 2 == 1
        _enemy = if (isGoblin)
            Enemy("Goblin", 10 + _level, 1 + _level, "Dang Dingily!", true, R.drawable.ic_enemy)
        else
            Enemy("Wizard", 10 + _level, 1 + _level, "Curses!", true, R.drawable.ic_wizard)

        //reset player stats
        _player =
            Player("Red Wolfenstein", 12, maxAttack = 4, "Gnash", 1, true, R.drawable.ic_warrior)

        //player desc UI
        tv_player_name.text = _player.name
        tv_player_health.text = getHealthMessage(_player.health, true)

        //enemy desc UI
        tv_enemy_name.text = _enemy.name
        tv_enemy_health.text = getHealthMessage(_enemy.health, false)
        val warcryMsg = "${_enemy.name} yells ${_enemy.warCry}"
        tv_warcry.text = warcryMsg

        //insult the player
        tv_player_actions.text = getString(R.string.insult)

        loadCharacterAnims()

        //let animations complete before allowing user to fight
        btn_attack.isEnabled = false
        btn_defend.isEnabled = false
        GlobalScope.launch(Dispatchers.Main) {

            delay(4200)
            btn_attack.isEnabled = true
        }
    }

    /**
     * Loads animated character gifs
     */
    private fun loadCharacterAnims() {
        Glide.with(this)
            .asGif()
            .load(R.drawable.ic_warrior) //Your gif resource
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
            .skipMemoryCache(true)
            .listener(gifDrawableListener)
            .into(iv_warrior)

        Glide.with(this)
            .asGif()
            .load(_enemy.characterGif) //Your gif resource
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
            .skipMemoryCache(true)
            .listener(gifDrawableListener)
            .into(iv_enemy)
    }

    /**
     * Returns formatted health status message
     */
    private fun getHealthMessage(health: Int, LTR: Boolean): String =
        if (LTR) "❤️ $health" else "$health ❤️"

    /**
     * Sets animation properties on the drawables
     */
    private val gifDrawableListener = object : RequestListener<GifDrawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<GifDrawable>?,
            isFirstResource: Boolean
        ): Boolean = false

        override fun onResourceReady(
            resource: GifDrawable,
            model: Any,
            target: Target<GifDrawable>,
            dataSource: DataSource,
            isFirstResource: Boolean
        ): Boolean {
            resource.setLoopCount(1)
            return false
        }
    }

    /**
     * Offensive attack against the enemy
     */
    fun attackButtonTapped(view: View) {
        val playerAttackPoints = Random.nextInt(_player.maxAttack)
        val enemyAttackPoints = Random.nextInt(_enemy.maxAttack)
        battle(playerAttackPoints, enemyAttackPoints)
    }

    /**
     * Blocks against incoming enemy attack
     * Stretch goal #1: Be able to select between multiple strategies when I encounter an enemy (such as attack, defend, heal)
     */
    fun defendButtonTapped(view: View) {
        btn_defend.isEnabled = false
        _isDefending = true
    }

    /**
     * Renders the UI/UX for a battle between player and enemy
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun battle(playerAttackPoints: Int, enemyAttackPoints: Int) {
        //Hide war cry and insult once fighting begins
        tv_warcry.text = ""
        tv_player_actions.text = ""

        //no 2x clicks
        btn_attack.isEnabled = false

        //Hoisted the animation property to pass to both attack functions
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        playerAttacks(playerAttackPoints, fadeOut)

        //Need to close player attack thread before opening enemy attack for better UI
        GlobalScope.launch(Dispatchers.Main) {
            delay(1000)
            enemyAttacks(enemyAttackPoints, playerAttackPoints, fadeOut)
        }
    }

    /**
     * enemy attacks player
     */
    private suspend fun enemyAttacks(
        enemyAttackPoints: Int,
        playerAttackPoints: Int,
        fadeOut: Animation
    ) {
        val enemyDrawable = iv_enemy.drawable as GifDrawable
        enemyDrawable.start()

        //Give player a quick chance to select defend button
        delay(2500)
        btn_defend.isEnabled = true
        delay(300)

        //Change UI when club strikes player
        btn_defend.isEnabled = false
        tv_player_attacked.visibility = View.VISIBLE
        tv_player_attacked.startAnimation(fadeOut)
        tv_player_attacked.visibility = View.INVISIBLE
        btn_attack.isEnabled = true

        if (_isDefending) {
            //enemies attack was blocked by player
            tv_player_attacked.text = getString(R.string.blocked)
            _isDefending = false
            return
        }

        //reduce players health
        _player.health -= enemyAttackPoints
        tv_player_health.text = getHealthMessage(_player.health, true)
        tv_player_attacked.text = if (enemyAttackPoints > 0) "- $enemyAttackPoints" else "miss"

        //Did the player die? ☠️
        if (_player.health <= 0) {
            _player.isAlive = false
            endRound(false)
            return
        }
        //Call random health
        heal(playerAttackPoints)

        //Set outcome description
        setActionDescription(playerAttackPoints, enemyAttackPoints)
    }

    /**
     * Player attacks enemy
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun playerAttacks(playerAttackPoints: Int, fadeOut: Animation) {
        val warriorDrawable = iv_warrior.drawable as GifDrawable
        warriorDrawable.start()

        GlobalScope.launch(Dispatchers.Main) {
            //once sword strikes enemy, show damage and other UI
            delay(1000)
            _enemy.health -= playerAttackPoints
            tv_enemy_health.text = getHealthMessage(_enemy.health, false)
            tv_enemy_attacked.text = if (playerAttackPoints > 0) "- $playerAttackPoints" else "miss"
            tv_enemy_attacked.visibility = View.VISIBLE
            tv_enemy_attacked.startAnimation(fadeOut)
            tv_enemy_attacked.visibility = View.INVISIBLE

            //Did the enemy die? ☠️
            if (_enemy.health <= 0) {
                _enemy.isAlive = false
                endRound(true)
            }
        }
    }

    /**
     * 1 in 100 chance of healing the player
     * Stretch goal #1: Be able to select between multiple strategies when I encounter an enemy (such as attack, defend, heal)
     */
    private fun heal(playerAttackPoints: Int) {
        val j = Random.nextInt(100)
        if (j == 69) {
            _player.health = 12
            tv_player_actions.text = if (playerAttackPoints == 0)
                "You missed ${_enemy.name}\nbut you recovered to full health"
            else "You hit ${_enemy.name} with $playerAttackPoints and recovered to full health"
        }
    }

    /**
     * Sets description of the outcome
     */
    private fun setActionDescription(playerPoints: Int, enemyPoints: Int) {
        var msg =
            if (playerPoints == 0) "You missed ${_enemy.name}\n" else "You inflicted $playerPoints damage to ${_enemy.name}\n"
        msg += if (enemyPoints == 0) "${_enemy.name} missed you\n" else "${_enemy.name} hit you with $enemyPoints"
        tv_player_actions.text = msg
    }

    /**
     * Navigate to game menu screen
     * Stretch goal #2: Be able to win or lose the game based on various factors (such as player health)
     */
    private fun endRound(playerWins: Boolean) {
        btn_attack.isEnabled = true
        btn_defend.isEnabled = true

        //navigate to play again screen
        val playAgain = Intent(this, PlayAgain::class.java)
        playAgain.putExtra(PLAYER_WINS, playerWins)
        playAgain.putExtra(LEVEL, _level)

        getResult.launch(playAgain)
    }

    /**
     * Register for activity callback to catch intent extras
     */
    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                _level = it.data?.getIntExtra(LEVEL, 1)!!
                initGame()
            }
        }
}