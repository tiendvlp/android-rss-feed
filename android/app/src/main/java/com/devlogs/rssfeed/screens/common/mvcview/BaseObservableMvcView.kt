package com.devlogs.rssfeed.screens.common.mvcview

import com.devlogs.rssfeed.common.base.BaseObservable


abstract class BaseObservableMvcView <LISTENER> : BaseObservable<LISTENER>(),
    ObservableMvcView<LISTENER> {

}
