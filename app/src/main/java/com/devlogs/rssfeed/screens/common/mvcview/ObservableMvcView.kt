package com.devlogs.rssfeed.screens.common.mvcview

interface ObservableMvcView<LISTENER> : MvcView {
    fun register (listener: LISTENER)
    fun unRegister (listener: LISTENER)
}