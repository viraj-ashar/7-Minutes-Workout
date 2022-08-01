package com.example.a7minuteworkout

import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a7minuteworkout.databinding.ActivityExcerciseBinding
import com.example.a7minuteworkout.databinding.ActivityMainBinding
import java.util.*
import kotlin.collections.ArrayList

class ExcerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var binding: ActivityExcerciseBinding? = null

    private var restTimer: CountDownTimer? = null
    private var restProgress = 0

    private var exerciseTimer: CountDownTimer? = null
    private var exerciseProgress = 0

    private var exerciseList: ArrayList<ExerciseModel>? = null
    private var currentExercisePosition = -1

    private var tts: TextToSpeech? = null
    private var player: MediaPlayer? = null

    private var exerciseAdapter: ExerciseStatusAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExcerciseBinding.inflate(layoutInflater)

        setSupportActionBar(binding?.tbExcersise)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        setContentView(binding?.root)

        tts = TextToSpeech(this, this)

        exerciseList = Constants.defaultexerciseList()

        binding?.tbExcersise?.setNavigationOnClickListener {
            onBackPressed()
        }

        setupRestView()
        setupExerciseStatusRecyclerView()
    }

    private fun setupExerciseStatusRecyclerView() {
        binding?.rvExerciseStatus?.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        exerciseAdapter = ExerciseStatusAdapter(exerciseList!!)
        binding?.rvExerciseStatus?.adapter = exerciseAdapter
    }

    private fun setupRestView() {
        if (currentExercisePosition + 1 > 0) {
            try {
                val soundUri =
                    Uri.parse("android.resource://com.example.a7minuteworkout/" + R.raw.workout_completed)
                player = MediaPlayer.create(applicationContext, soundUri)
                player?.isLooping = false
                player?.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding?.flRestView?.visibility = View.VISIBLE
        binding?.tvTitle?.visibility = View.VISIBLE
        binding?.tvExerciseName?.visibility = View.INVISIBLE
        binding?.flExerciseView?.visibility = View.INVISIBLE
        binding?.ivImage?.visibility = View.INVISIBLE
        binding?.tvUpcominglabel?.visibility = View.VISIBLE
        binding?.tvUpcomingExerciseName?.visibility = View.VISIBLE

        if (restTimer != null) {
            restTimer?.cancel()
            restProgress = 0
        }

        binding?.tvUpcomingExerciseName?.text =
            exerciseList!![currentExercisePosition + 1].getName()

        setRestProgressBar()
    }

    private fun setRestProgressBar() {
        binding?.pbProgressBar?.progress = restProgress
        restTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(p0: Long) {
                restProgress++
                binding?.pbProgressBar?.progress = 10 - restProgress
                binding?.tvTimer?.text = (10 - restProgress).toString()
            }

            override fun onFinish() {
                currentExercisePosition++
                setupExerciseView()
            }
        }.start()
    }

    private fun setupExerciseView() {
        binding?.flRestView?.visibility = View.INVISIBLE
        binding?.tvTitle?.visibility = View.INVISIBLE
        binding?.tvExerciseName?.visibility = View.VISIBLE
        binding?.flExerciseView?.visibility = View.VISIBLE
        binding?.ivImage?.visibility = View.VISIBLE
        binding?.tvUpcominglabel?.visibility = View.INVISIBLE
        binding?.tvUpcomingExerciseName?.visibility = View.INVISIBLE

        if (exerciseTimer != null) {
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }

        speakOut(exerciseList!![currentExercisePosition].getName())

        binding?.ivImage?.setImageResource(exerciseList!![currentExercisePosition].getImage())
        binding?.tvExerciseName?.text = exerciseList!![currentExercisePosition].getName()

        setExerciseProgressBar()
    }

    private fun setExerciseProgressBar() {
        binding?.pbExercise?.progress = exerciseProgress

        exerciseTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(p0: Long) {
                exerciseProgress++
                binding?.pbExercise?.progress = 30 - exerciseProgress
                binding?.tvTimerExercise?.text = (30 - exerciseProgress).toString()
            }

            override fun onFinish() {
                if (currentExercisePosition + 1 < exerciseList!!.size) {
                    setupRestView()
                } else {
                    Toast.makeText(
                        this@ExcerciseActivity,
                        "Congratulations you have completed 7 minutes workout.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }

        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (restTimer != null) {
            restTimer?.cancel()
            restProgress = 0
        }

        if (exerciseTimer != null) {
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }

        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }

        if (player != null) {
            player!!.stop()
        }

        binding = null
    }

    private fun speakOut(text: String) {
        tts?.setPitch(0.75f)
        tts?.setSpeechRate(0.8f)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.CANADA)

            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                Toast.makeText(this, "Language Specified Is Not Supported.", Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            Toast.makeText(this, "Initialization Failed", Toast.LENGTH_LONG).show()
        }
    }
}