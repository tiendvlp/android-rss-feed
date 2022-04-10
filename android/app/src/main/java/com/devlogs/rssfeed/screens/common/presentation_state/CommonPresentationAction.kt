package com.devlogs.chatty.screen.common.presentationstate

import com.devlogs.rssfeed.screens.common.presentation_state.PresentationAction

sealed class CommonPresentationAction : PresentationAction {
    object InitAction : CommonPresentationAction()
    object RestoreAction : CommonPresentationAction ()
}