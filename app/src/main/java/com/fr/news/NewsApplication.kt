package com.fr.news

import android.app.Application

class NewsApplication: Application() {

    companion object {
        lateinit var instance: NewsApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}