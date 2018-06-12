package com.young.binder

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.ArrayList

abstract class DataCenter {

    companion object {
        public var globalDebugMode: DebugMode = DebugMode.MODE_NONE
    }

    private val tag = javaClass.simpleName

    enum class DebugMode {
        MODE_NONE, MODE_NORMAL, MODE_DETAILED
    }

    private val binderMap = HashMap<String, ArrayList<Event<*>>>()
    private val adapterBinderMap = HashMap<String, HashMap<Any, Event<*>>>()
    private var mainHandler: Handler? = Handler(Looper.getMainLooper())

    var debugMode: DebugMode = DebugMode.MODE_NONE

    public fun clear() {
        mainHandler?.removeCallbacksAndMessages(null)
        binderMap.clear()
    }

    public fun destroy() {
        binderMap.clear()
        mainHandler?.removeCallbacksAndMessages(null)
        mainHandler = null
    }

    fun remove(any: Any, eventTag: String?) {
        removeNormalEvent(any, eventTag)
        removeAdapterEvent(any, eventTag)
    }

    private fun removeNormalEvent(any: Any, eventTag: String?) {
        if (eventTag == null || eventTag.isEmpty()) {
            binderMap.values.forEach {
                val iterator = it.iterator()
                while (iterator.hasNext()) {
                    val value = iterator.next()
                    if (any == value.getObserver()) {
                        if (globalDebugMode == DebugMode.MODE_DETAILED
                                || debugMode == DebugMode.MODE_DETAILED) {
                            Log.d(tag, "[$value] -> has removed from binderCloud:$this")
                        }
                        iterator.remove()
                    }
                }
            }
        } else {
            val eventList = binderMap[eventTag]
            eventList?.apply {
                val iterator = iterator()
                while (iterator.hasNext()) {
                    val value = iterator.next()
                    if (any == value.getObserver()) {
                        if (globalDebugMode == DebugMode.MODE_DETAILED
                                || debugMode == DebugMode.MODE_DETAILED) {
                            Log.d(tag, "[$value] -> has removed from binderCloud:$this")
                        }
                        iterator.remove()
                    }
                }
            }
        }
    }

    private fun removeAdapterEvent(any: Any, eventTag: String?) {
        if (eventTag == null || eventTag.isEmpty()) {
            val maps = adapterBinderMap.values
            maps.forEach { map ->
                val iterator = map.entries.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    if (entry.key == any) {
                        iterator.remove()
                    }
                }
            }
        } else {
            val map = adapterBinderMap[eventTag]
            map?.apply {
                val iterator = entries.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    if (entry.key == any) {
                        iterator.remove()
                    }
                }
            }
        }
    }

    protected fun getBinderMap(): HashMap<String, ArrayList<Event<*>>> {
        return binderMap
    }

    fun addEvent(eventTag: String, event: Event<*>) {
        var existList: ArrayList<Event<*>>? = binderMap[eventTag]
        if (existList == null) {
            existList = ArrayList()
            binderMap[eventTag] = existList
        }
        existList.add(event)
    }

    fun addAdapterEvent(t: Any, eventTag: String, event: Event<*>) {
        var existBinderMap: HashMap<Any, Event<*>>? = adapterBinderMap[eventTag]
        if (existBinderMap == null) {
            existBinderMap = HashMap()
            existBinderMap[t] = event
            adapterBinderMap[eventTag] = existBinderMap
        } else {
            existBinderMap[t] = event
        }
    }

    fun notifyDataChanged(eventTag: String) {
        if (globalDebugMode != DebugMode.MODE_NONE
                || debugMode != DebugMode.MODE_NONE) {
            Log.d(tag, "notifyDataChanged : $eventTag")
        }
        val existList: ArrayList<Event<*>>? = binderMap[eventTag]
        existList?.forEach {
            if (globalDebugMode == DebugMode.MODE_DETAILED
                    || debugMode == DebugMode.MODE_DETAILED) {
                Log.d(tag, "[$eventTag] -> detail notifyDataChanged : $it")
            }
            mainHandler?.post { it.changed() }
        }
        val eventsInAdapter: HashMap<Any, Event<*>>? = adapterBinderMap[eventTag]
        eventsInAdapter?.values?.forEach {
            if (globalDebugMode == DebugMode.MODE_DETAILED
                    || debugMode == DebugMode.MODE_DETAILED) {
                Log.d(tag, "detail notifyDataChanged in adapter : $it")
            }
            mainHandler?.post { it.changed() }
        }
    }
}