package com.devlogs.rssfeed.screens.common.mvcview


/**
 * all factory_code is extension-method, they were splited into XXXMvcView File
 * */

class MvcViewFactory {
    val uiToolkit: UIToolkit

    constructor(uiToolkit: UIToolkit) {
        this.uiToolkit = uiToolkit
    }

}