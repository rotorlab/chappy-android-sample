package com.rotor.core

import android.content.Context
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet

class RViewPager(context: Context, attributes: AttributeSet?): ViewPager(context, attributes) {

    lateinit var adapter: RPAdapter

    val fragments: ArrayList<RFragment> = ArrayList()

    constructor(context: Context) : this(context, null) {

    }

    fun init(activity: AppCompatActivity) {
        adapter = RPAdapter(this@RViewPager, activity.supportFragmentManager)
        setAdapter(adapter)
    }

    fun add(fragment: RFragment) : Boolean {
        var found = false
        fragments.forEach {
            if (it::class.java.simpleName.equals(fragment::class.java.simpleName)) {
                found = true
            }
        }
        if (found) {
            return false
        } else {
            fragment.onPauseFragment()
            fragments.add(fragment)
            adapter.notifyDataSetChanged()
            return true
        }
    }

    fun fragments() : ArrayList<RFragment> {
        return fragments
    }

    fun <T> setFragment(clazz: Class<T>) {
        adapter.setFragment(clazz)
    }

    fun adapter() : RPAdapter {
        return adapter
    }

}