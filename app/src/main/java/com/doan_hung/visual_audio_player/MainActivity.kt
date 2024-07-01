package com.doan_hung.visual_audio_player

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.doan_hung.visual_audio_player.databinding.ActivityMainBinding
import com.doan_hung.visual_audio_player.recorder.Recorder
import com.imn.ivisusample.utils.checkAudioPermission
import com.imn.ivisusample.utils.formatAsTime
import com.imn.ivisusample.utils.getDrawableCompat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recorder: Recorder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        checkAudioPermission(AUDIO_PERMISSION_REQUEST_CODE)

        initUI()
    }

    override fun onStart() {
        super.onStart()
        listenOnRecorderStates()
    }

    override fun onStop() {
        recorder.release()
        super.onStop()
    }

    private fun initUI() = with(binding) {
        recordButton.setOnClickListener { recorder.toggleRecording() }
    }

    private fun listenOnRecorderStates() = with(binding) {
        recorder = Recorder.getInstance(applicationContext).init().apply {
            onStart = { recordButton.icon = getDrawableCompat(R.drawable.ic_stop_24) }
            onStop = {
                visualizer.clearData()
                timelineTextView.text = 0L.formatAsTime()
                recordButton.icon = getDrawableCompat(R.drawable.ic_record_24)
                startActivity(Intent(this@MainActivity, PlayActivity::class.java))
            }
            onAmpListener = {
                runOnUiThread {
                    if (recorder.isRecording) {
                        timelineTextView.text = recorder.getCurrentTime().formatAsTime()
                        Log.i("TAG", "listenOnRecorderStates: $it")
                        visualizer.addData(it)
                    }
                }
            }
        }
    }

    companion object {
        private const val AUDIO_PERMISSION_REQUEST_CODE = 1
    }
}