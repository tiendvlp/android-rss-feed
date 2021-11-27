package com.devlogs.rssfeed.screens.main

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.devlogs.chatty.screen.common.BackPressDispatcher
import com.devlogs.chatty.screen.common.BackPressListener
import com.devlogs.rssfeed.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), BackPressDispatcher {
    companion object {
        fun start(currentContext: Context) {
            currentContext.startActivity(Intent(currentContext, MainActivity::class.java))
        }
    }

    private var backPressListeners: HashSet<BackPressListener> = HashSet()

    private lateinit var grBottomNav : RadioGroup
    private lateinit var rbtnMenu : RadioButton
    private lateinit var rbtnFavoriteFeed : RadioButton
    private lateinit var rbtnReadFeeds : RadioButton
    private lateinit var rbtnAddChannel : RadioButton

    @Inject
    protected lateinit var mainScreenNavigator: MainScreenNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_main)
        mainScreenNavigator.init(savedInstanceState)
        addControls ()
        addEvents()
    }

    private fun addControls() {
        grBottomNav = findViewById(R.id.grBottomNav)
        rbtnMenu = findViewById(R.id.rbtnMenu)
        rbtnFavoriteFeed = findViewById(R.id.rbtnFavoriteFeed)
        rbtnReadFeeds = findViewById(R.id.rbtnReadFeeds)
        rbtnAddChannel = findViewById(R.id.rbtnAddChannel)
    }

    private fun addEvents() {
        grBottomNav.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                rbtnMenu.id -> Toast.makeText(this, "Show the menu", Toast.LENGTH_LONG).show()
                rbtnFavoriteFeed.id -> mainScreenNavigator.switchToSavedFeedsTab()
                rbtnReadFeeds.id -> mainScreenNavigator.switchToFeedsTab()
                rbtnAddChannel.id -> mainScreenNavigator.switchToAddChannelTab()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mainScreenNavigator.onSavedInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun register(backPressListener: BackPressListener) {
        backPressListeners.add(backPressListener)
    }

    override fun unregister(backPressListener: BackPressListener) {
        backPressListeners.remove(backPressListener)
    }

    override fun onBackPressed() {
        var isBackPressConsumed = false

        backPressListeners.forEach { listener ->
            if (listener.onBackPress()) {
                isBackPressConsumed = true
            }
        }

        if (isBackPressConsumed) {
            return
        }

        if (!mainScreenNavigator.navigateBack()) {
            super.onBackPressed()
        }
    }
}