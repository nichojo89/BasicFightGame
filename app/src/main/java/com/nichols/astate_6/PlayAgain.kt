package com.nichols.astate_6

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_play_again.*


const val PLAYER_WINS = "PLAYER_WINS"
const val LEVEL = "LEVEL"

//Stretch Goal #1: Add multiple activities and layouts to your project
class PlayAgain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_again)
        val playerWIns = intent.getBooleanExtra(PLAYER_WINS, false)

        var goBackIntent = Intent()
        var level = 1
        if(playerWIns){
            //Stretch goal #3: Progress through the game world by defeating different obstacles (such as defeating enemies, and progressing to the next area)
            tv_outcome.text = "You Win!"
            level = intent.getIntExtra(LEVEL,1)
            level++
            btn_play_again.text = "Level $level"

        } else {
            tv_outcome.text = "You Lose"
            btn_play_again.text = "Restart"
        }

        goBackIntent.putExtra(LEVEL, level)
        btn_play_again.setOnClickListener {
            setResult(RESULT_OK, goBackIntent)
            finish()
        }
    }
}

