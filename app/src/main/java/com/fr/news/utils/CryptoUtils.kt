package com.fr.news.utils

import java.security.MessageDigest

object CryptoUtils {
    /**
     * 计算字符串的 SHA-1 哈希
     */
    fun sha1(input: String): ByteArray{
        val messageDigest = MessageDigest.getInstance("SHA-1")
        return messageDigest.digest(input.toByteArray())
    }

    /**
     * 计算字节数组的 MD5 哈希（十六进制字符串）
     */
    fun md5Hex(data: ByteArray):String {
        val messageDigest = MessageDigest.getInstance("MD5")
        val hashBytes = messageDigest.digest(data)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * 模拟 TypeScript 中的 myCrypto 函数
     * 先进行 SHA-1 哈希，然后进行 MD5 哈希
     */
    fun myCrypto(data:String,algorithm:String): ByteArray{
        return when(algorithm){
            "SHA-1" -> sha1(data)
            else -> throw IllegalArgumentException("Invalid algorithm: $algorithm")
        }
    }
}