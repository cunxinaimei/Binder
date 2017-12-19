package com.young.binder

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.forEach

/**
 * Created by young on 2017/11/13.
 */
abstract class BinderCloud {

    private val tag = javaClass.simpleName

    enum class DebugMode {
        MODE_NONE, MODE_NORMAL, MODE_DETAILED
    }

    private val binderMap = HashMap<String, ArrayList<Event>>()
    private val adapterBinderMap = HashMap<String, HashMap<Any, Event>>()
    private var mainHandler: Handler? = Handler(Looper.getMainLooper())

    var debugMode: DebugMode = DebugMode.MODE_NONE

    @Deprecated("用不到，不会影响到GC")
    public fun clear() {
        mainHandler = null
        binderMap.clear()
    }

    protected fun getBinderMap():HashMap<String, ArrayList<Event>>{
        return binderMap
    }

    fun addEvent(eventTag: String, event: Event) {
        var existList: ArrayList<Event>? = binderMap[eventTag]
        if (existList == null) {
            existList = ArrayList()
            binderMap.put(eventTag, existList)
        }
        existList.add(event)
    }

    fun addAdapterEvent(t: Any, eventTag: String, event: Event) {
        var existBinderMap: HashMap<Any, Event>? = adapterBinderMap[eventTag]
        if (existBinderMap==null){
            existBinderMap = HashMap()
            existBinderMap.put(t, event)
            adapterBinderMap.put(eventTag, existBinderMap)
        } else {
            existBinderMap.put(t, event)
        }
    }

    fun notifyDataChanged(eventTag: String) {
        if (debugMode != DebugMode.MODE_NONE) {
            Log.d(tag, "notifyDataChanged : $eventTag")
        }
        val existList: ArrayList<Event>? = binderMap[eventTag]
        existList?.forEach {
            if (debugMode == DebugMode.MODE_DETAILED) {
                Log.d(tag, "detail notifyDataChanged : ${it}")
            }
            mainHandler?.post { it.changed() }
        }
        val eventsInAdapter: HashMap<Any, Event>? = adapterBinderMap[eventTag]
        eventsInAdapter?.values?.forEach {
            if (debugMode == DebugMode.MODE_DETAILED) {
                Log.d(tag, "detail notifyDataChanged in adapter : ${it}")
            }
            mainHandler?.post { it.changed() }
        }
    }
}