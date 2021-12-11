package com.devlogs.rssfeed.screens.read_feeds.controller

import com.devlogs.rssfeed.common.helper.LogTarget
import com.devlogs.rssfeed.common.helper.errorLog
import com.devlogs.rssfeed.common.helper.warningLog
import com.devlogs.rssfeed.common.shared_context.AppConfig
import com.devlogs.rssfeed.follow.FollowChannelUseCaseSync
import com.devlogs.rssfeed.follow.UnFollowChannelUseCaseSync
import com.devlogs.rssfeed.screens.common.presentation_state.PresentationStateManager
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationAction.*
import com.devlogs.rssfeed.screens.read_feeds.presentation_state.ReadFeedsScreenPresentationState.DisplayState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class ChannelFollowingController @Inject constructor(@Named(AppConfig.DaggerNamed.FRAGMENT_SCOPE) private val stateManager: PresentationStateManager,
                                                     private val followChannelUseCaseSync: FollowChannelUseCaseSync,
                                                     private val unFollowChannelUseCaseSync: UnFollowChannelUseCaseSync) : LogTarget {

    private val coroutine = CoroutineScope(Dispatchers.Main.immediate)

    fun follow () {
        coroutine.launch {
            if (stateManager.currentState is DisplayState) {
                val result = followChannelUseCaseSync.executes((stateManager.currentState as DisplayState).channelPresentableModel.id)

                if (result is FollowChannelUseCaseSync.Result.Success) {
                    stateManager.consumeAction(FollowProcessSuccessAction())
                }
                else if (result is FollowChannelUseCaseSync.Result.GeneralError) {
                    val errorMessage = if (result.message.isNullOrBlank()) "Internal error" else result.message
                    stateManager.consumeAction(FollowProcessFailedAction(errorMessage))
                    errorLog(errorMessage)
                }
                else if (result is FollowChannelUseCaseSync.Result.UnAuthorized) {
                    val errorMessage = "UnAuthorized"
                    stateManager.consumeAction(FollowProcessFailedAction(errorMessage))
                    errorLog(errorMessage)
                }
            } else {
                warningLog("Follow action only work in DisplayState not the ${stateManager.currentState.javaClass} ")
            }
        }
    }

    fun unFollow () {
        coroutine.launch {
            if (stateManager.currentState is DisplayState) {
                val result = unFollowChannelUseCaseSync.executes((stateManager.currentState as DisplayState).channelPresentableModel.id)

                if (result is UnFollowChannelUseCaseSync.Result.Success) {
                    stateManager.consumeAction(UnFollowProcessSuccessAction())
                }
                else if (result is UnFollowChannelUseCaseSync.Result.GeneralError) {
                    val errorMessage = if (result.message.isNullOrBlank()) "Internal error" else result.message
                    stateManager.consumeAction(
                        UnFollowProcessFailedAction(
                            errorMessage
                        )
                    )
                    errorLog(errorMessage)
                }
                else if (result is UnFollowChannelUseCaseSync.Result.UnAuthorized) {
                    val errorMessage = "UnAuthorized"
                    stateManager.consumeAction(UnFollowProcessFailedAction(errorMessage))
                    errorLog(errorMessage)
                }
            } else {
                warningLog("UnFollow action only work in DisplayState not the ${stateManager.currentState.javaClass} ")
            }
        }
    }

}