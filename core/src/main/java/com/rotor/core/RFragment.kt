package com.rotor.core

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.rotor.core.interfaces.RScreen
import java.util.HashMap

abstract class RFragment: Fragment(), RScreen {

    private var active = false
    private var viewed = false
    private lateinit var map: HashMap<String, Any>

    abstract fun onResumeView()

    abstract fun onPauseView()

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewed = true
        map = java.util.HashMap()
        Rotor.screens().add(this)
    }

    fun onResumeFragment() {
        active = true
        Rotor.onResume()
        onResumeView()
    }

    fun onPauseFragment() {
        Rotor.onPause()
        active = false
        onPauseView()
    }

    override fun onDestroyView() {
        Rotor.screens().remove(this)
        viewed = false
        super.onDestroyView()
    }


    override fun isActive(): Boolean {
        return active
    }

    fun viewed(): Boolean {
        return viewed
    }

    override fun addPath(path: String, obj: Any): Boolean {
        if (!map.contains(path)) {
            map.put(path, obj)
            return true;
        } else {
            return false
        }
    }

    override fun removePath(path: String): Boolean {
        if (map.contains(path)) {
            map.remove(path)
            return true;
        } else {
            return false
        }
    }

    override fun hasPath(path: String): Boolean {
        return map.contains(path)
    }

    override fun holders(): HashMap<String, Any> {
        return map
    }

}