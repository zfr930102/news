package com.fr.news.utils

import android.util.Log
import kotlinx.coroutines.runBlocking
import java.net.URLEncoder

class SearchParamsBuilder {
    private constructor()
    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            SearchParamsBuilder()
        }
    }

    private fun getClsSearchParams(moreParams: Map<String, Any> = emptyMap()): String {
        //合并基础参数跟额外参数
        val allParams = mergeParams(ApiConstants.CLS_BASE_PARAMS, moreParams)
        Log.d(TAG, "getClsSearchParams: mergeParams = $allParams")
        //对参数进行排序并进行构建查询字符串
        val queryString = buildSortedQueryString(allParams)
        Log.d(TAG, "getClsSearchParams: queryString = $queryString")
        //生成签名
        val signatureString = generateSignature(queryString, "SHA-1")
        Log.d(TAG, "getClsSearchParams: signatureString = $signatureString")
        //添加签名并返回最终结果
        val finalQueryString = buildFinalQueryString(allParams, signatureString)
        Log.d(TAG, "getClsSearchParams: finalQueryString = $finalQueryString")
        return finalQueryString
    }

    private fun buildFinalQueryString(
        allParams: MutableMap<String, String>,
        signatureString: String
    ): String {
        allParams["sign"] = signatureString
        return buildSortedQueryString(allParams)
    }

    private fun generateSignature(queryString: String, algorithm: String): String {
        val algorithmByteArray = CryptoUtils.myCrypto(queryString, algorithm)
        return CryptoUtils.md5Hex(algorithmByteArray)
    }

    private fun buildSortedQueryString(params: Map<String, String>): String {
        return params.entries.sortedBy { it.key }.joinToString("&") { (key, value) ->
            "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
        }
    }

    private fun mergeParams(
        baseParams: Map<String, Any>,
        moreParams: Map<String, Any>
    ): MutableMap<String, String> {
        val allParams = mutableMapOf<String, String>()
        baseParams.forEach { (key, value) ->
            allParams[key] = value.toString()
        }
        moreParams.forEach { (key, value) ->
            allParams[key] = value.toString()
        }
        return allParams
    }

    suspend fun getSearchParams(moreParams: Map<String, Any> = emptyMap(),searchParmsChannel:
    String): String{
        return when(searchParmsChannel){
            SearchParamsChannel.CLS_SEARCH_PARAMS_CHANNEL -> {
                getClsSearchParams(moreParams)
            }
            else -> {
                Log.d(TAG, "getSearchParams: searchParmsChannel is error")
                ""
            }
        }

    }

    fun getSearchParamsSync(moreParams: Map<String, Any> = emptyMap(),searchParmsChannel:
    String): String{
       return when(searchParmsChannel){
            SearchParamsChannel.CLS_SEARCH_PARAMS_CHANNEL -> {
                runBlocking { getClsSearchParams(moreParams) }
            }
            else -> {
                Log.d(TAG, "getSearchParamsSync: searchParmsChannel is error")
                ""
            }
        }
    }
}