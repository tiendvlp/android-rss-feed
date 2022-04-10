package com.devlogs.rssfeed.screens.common.presentation_state

import com.devlogs.rssfeed.screens.common.presentation_state.PresentationAction
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationState

interface PresentationStateChangedListener {
    fun onStateChanged (previousState: PresentationState?, currentState: PresentationState, action: PresentationAction)
}