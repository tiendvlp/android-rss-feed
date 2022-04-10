package com.devlogs.rssfeed.screens.common.presentation_state

import java.lang.Exception

class InvalidActionException(stateName: String, actionName: String) : Exception("$stateName state can not consume $actionName action")