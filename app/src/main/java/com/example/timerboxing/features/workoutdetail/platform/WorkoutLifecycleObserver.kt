package com.example.timerboxing.features.workoutdetail.platform

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

internal class WorkoutLifecycleObserver(
    private val isChangingConfigurations: () -> Boolean,
    private val onBackground: () -> Unit
) : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_STOP && !isChangingConfigurations()) onBackground()
    }
}
