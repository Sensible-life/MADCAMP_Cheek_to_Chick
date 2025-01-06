package com.mpackage.network.utils

import org.json.JSONArray
import org.json.JSONObject

fun JSONObject.toMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    val keys = this.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        val value = this[key]
        map[key] = when (value) {
            is JSONObject -> value.toMap() // JSONObject 재귀 변환
            is JSONArray -> value.toList() // JSONArray 변환
            else -> value
        }
    }
    return map
}

fun JSONArray.toList(): List<Any> {
    val list = mutableListOf<Any>()
    for (i in 0 until this.length()) {
        val value = this[i]
        list.add(
            when (value) {
                is JSONObject -> value.toMap()
                is JSONArray -> value.toList()
                else -> value
            }
        )
    }
    return list
}


//fun JSONObject.toMap(): Map<String, Any> {
//    val map = mutableMapOf<String, Any>()
//    val keys = this.keys()
//    while (keys.hasNext()) {
//        val key = keys.next()
//        val value = this.get(key)
//        map[key] = this[key]
//        map[key] = if (value is JSONObject) {
//            value.toMap()
//        } else if (value is org.json.JSONArray) {
//            val list = mutableListOf<Any>()
//            for (i in 0 until value.length()) {
//                val item = value.get(i)
//                list.add(if (item is JSONObject) item.toMap() else item)
//            }
//            list
//        } else {
//            value
//        }
//    }
//    return map
//}
//
//fun JSONArray.toList(): List<Any> {
//    val list = mutableListOf<Any>()
//    for (i in 0 until this.length()) {
//        val value = this[i]
//        list.add(
//            when (value) {
//                is JSONObject -> value.toMap() // JSONObject일 경우 재귀적으로 호출
//                is JSONArray -> value.toList() // JSONArray를 List로 변환
//                else -> value
//            }
//        )
//    }
//    return list
//}