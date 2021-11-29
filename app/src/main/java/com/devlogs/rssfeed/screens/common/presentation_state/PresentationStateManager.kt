package com.devlogs.rssfeed.screens.common.presentation_state

import android.os.Bundle
import android.util.Log
import com.devlogs.chatty.screen.common.presentationstate.CommonPresentationAction
import com.devlogs.chatty.screen.common.presentationstate.CommonPresentationAction.RestoreAction
import com.devlogs.rssfeed.common.base.BaseObservable

class PresentationStateManager : BaseObservable<PresentationStateChangedListener>() {
    lateinit var currentState: PresentationState private set
    private lateinit var currentAction : PresentationAction
    private var previousState : PresentationState? = null
    private lateinit var defaultState : PresentationState

    private val TAG = "PRESENTATION_STATE"

    fun init(savedInstanceState: Bundle?, defaultState: PresentationState) {
        this.defaultState = defaultState
        if (savedInstanceState != null && savedInstanceState.containsKey(TAG)) {
            currentAction = RestoreAction
            currentState = savedInstanceState.getSerializable(TAG) as PresentationState
        } else {
            currentAction = CommonPresentationAction.InitAction
            currentState = defaultState
        }

        consumeAction(currentAction)
//        getListener().forEach {
//            it.onStateChanged(previousState, currentState, currentAction)
//        }
    }

    fun consumeAction (action: PresentationAction) {
        previousState = currentState
        val causeAndEffect = currentState.consumeAction(previousState!!, action)
        currentState = causeAndEffect.state
        currentAction = causeAndEffect.action
        getListener().forEach {
            it.onStateChanged(previousState, currentState, currentAction)
        }
    }

    fun register (listener: PresentationStateChangedListener, getPreviousEvent: Boolean) {
        register(listener)
        if (getPreviousEvent && previousState != null) {
            listener.onStateChanged(previousState, currentState, RestoreAction)
        }
    }

    fun onSavedInstanceState (outState: Bundle) {
        Log.d("PresentationStateManager", "onSavedInstantState: ${currentState.javaClass.simpleName} amd allowSave: ${currentState.allowSave}")
        if (currentState.allowSave) {
            outState.putSerializable(TAG, currentState)
        } else {
            outState.remove(TAG)
            previousState = currentState
            currentState = defaultState
        }
    }
}