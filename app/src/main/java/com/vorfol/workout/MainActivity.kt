package com.vorfol.workout

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {

    private val lastWorkoutKey = "lastWorkout"
    private val lastAmountKey = "lastAmount"

    private fun setControlVisibility(visibility: Int) {
        series.visibility = visibility
        currentAmount.visibility = visibility
        done.visibility = visibility
        buttonUp.visibility = visibility
        buttonDown.visibility = visibility
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getPreferences(Context.MODE_PRIVATE)
        val workouts = resources.getStringArray(R.array.workouts)

        workoutSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val edit = prefs.edit()
                edit.putInt(lastWorkoutKey, position)
                edit.apply()
                val arrId = resources.getIdentifier(workouts[position], "array", packageName)
                if (arrId != 0) {
                    weekSpinner.adapter = ArrayAdapter.createFromResource(baseContext, arrId, R.layout.support_simple_spinner_dropdown_item)
                    val key = lastAmountKey + workouts[position]
                    weekSpinner.setSelection(prefs.getInt(key, 0))
                }
            }
        }

        workoutSpinner.setSelection(prefs.getInt(lastWorkoutKey, 0))

        weekSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                setControlVisibility(INVISIBLE)
                val lastWorkout = prefs.getInt(lastWorkoutKey, 0)
                if (0 <= lastWorkout && lastWorkout < workouts.size) {
                    val key = lastAmountKey + workouts[lastWorkout]
                    val edit = prefs.edit()
                    edit.putInt(key, position)
                    edit.apply()
                    val arrId = resources.getIdentifier(workouts[lastWorkout], "array", packageName)
                    if (arrId != 0) {
                        val amounts = resources.getStringArray(arrId)
                        if (0 <= position && position < amounts.size) {
                            val str = amounts[position]
                            val strArr = str.split(" ")
                            if (strArr.size >= 5) {
                                currentAmount.text = strArr[0]
                                first.text = strArr[0]
                                second.text = strArr[1]
                                third.text = strArr[2]
                                fourth.text = strArr[3]
                                fifth.text = strArr[4]
                                setControlVisibility(VISIBLE)
                            }
                        }
                    }
                }
            }
        }

        done.setOnClickListener {
            // 1. hide spinner
            workoutSpinner.visibility = INVISIBLE
            weekSpinner.visibility = INVISIBLE
            if (currentAmount.text.isNotEmpty()) {
                // 2. save done value
                var appendStatistics = currentAmount.text.toString() + " "
                // 3. fix done and select next amount
                var doCountDown = true
                seriesDone.visibility = VISIBLE
                when {
                    firstDone.text == "" -> {
                        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
                        appendStatistics = "\n" + sdf.format(Date()) + " " + appendStatistics
                        firstDone.text = currentAmount.text
                        currentAmount.text = second.text
                    }
                    secondDone.text == "" -> {
                        secondDone.text = currentAmount.text
                        currentAmount.text = third.text
                    }
                    thirdDone.text == "" -> {
                        thirdDone.text = currentAmount.text
                        currentAmount.text = fourth.text
                    }
                    fourthDone.text == "" -> {
                        fourthDone.text = currentAmount.text
                        currentAmount.text = fifth.text
                    }
                    fifthDone.text == "" -> {
                        fifthDone.text = currentAmount.text
                        currentAmount.text = ""
                        doCountDown = false
                        setControlVisibility(INVISIBLE)
                        congrat.visibility = VISIBLE
                    }
                }
                if (appendStatistics.isNotEmpty()) {
                    val lastWorkout = prefs.getInt(lastWorkoutKey, 0)
                    val statistics = File(getExternalFilesDir(null), workouts[lastWorkout])
                    statistics.appendText(appendStatistics)
                }
                // 4. start countdown
                done.visibility = INVISIBLE
                if (doCountDown) {
                    timerText.visibility = VISIBLE
                    val countDown = object : CountDownTimer(120000, 1000) {
                        @SuppressLint("SetTextI18n")
                        override fun onTick(millisUntilFinished: Long) {
                            timerText.text =
                                "${twoDigits(millisUntilFinished / 60000)}:${twoDigits((millisUntilFinished % 60000) / 1000)}"
                        }
                        override fun onFinish() {
                            done.visibility = VISIBLE
                            timerText.text = ""
                            timerText.visibility = INVISIBLE
                        }
                    }
                    countDown.start()
                }
            }
        }

        buttonUp.setOnClickListener {
            try {
                val amount = currentAmount.text.toString().toInt()
                currentAmount.text = (amount + 1).toString()
            } catch (ex: Exception) {

            }
        }

        buttonDown.setOnClickListener {
            try {
                val amount = currentAmount.text.toString().toInt()
                if (amount > 0) {
                    currentAmount.text = (amount - 1).toString()
                }
            } catch (ex: Exception) {

            }
        }
    }

    fun twoDigits(num: Long): String {
        return when (num < 10) {
            true -> "0$num"
            else -> "$num"
        }
    }
}
