package com.doan_hung.visual_audio_player

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.doan_hung.visual_audio_player.databinding.ActivityPlayBinding
import com.doan_hung.visual_audio_player.player.AudioPlayer
import com.imn.ivisusample.utils.formatAsTime
import com.imn.ivisusample.utils.getDrawableCompat

class PlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayBinding
    private lateinit var player: AudioPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        listenOnPlayerStates()
        initUI()
    }

    override fun onStop() {
        player.release()
        super.onStop()
    }

    private fun initUI() = with(binding) {
        playButton.setOnClickListener { player.togglePlay() }
        seekForwardButton.setOnClickListener {
            visualizer.seekOver(SEEK_OVER_AMOUNT) { time ->
                player.seekTo(time)
                player.player.play()
            }
        }
        seekBackwardButton.setOnClickListener {
            visualizer.seekOver(-SEEK_OVER_AMOUNT) { time ->
                player.seekTo(time)
                player.player.play()
            }
        }
        lifecycleScope.launchWhenCreated {
            val amps = player.loadAmps()
            visualizer.setData(amps)
        }
    }

    private fun listenOnPlayerStates() = with(binding) {
        player = AudioPlayer.getInstance(applicationContext).init().apply {
            onStart = {
                visualizer.setTotalTime(player.duration)
                playButton.icon = getDrawableCompat(R.drawable.ic_pause_24)
            }
            onStop = { playButton.icon = getDrawableCompat(R.drawable.ic_play_arrow_24) }
            onPause = { playButton.icon = getDrawableCompat(R.drawable.ic_play_arrow_24) }
            onResume = { playButton.icon = getDrawableCompat(R.drawable.ic_pause_24) }
            onProgress = { time, _ ->
                updateTime(time)
            }
        }
    }

    private fun updateTime(time: Long) = with(binding) {
        timelineTextView.text = time.formatAsTime()
        visualizer.updateTime(time)
    }

    companion object {
        const val SEEK_OVER_AMOUNT = 5000L
    }
}