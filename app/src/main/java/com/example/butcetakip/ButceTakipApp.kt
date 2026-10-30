package com.example.butcetakip

import android.app.Application

class ButceTakipApp : Application() {
    override fun onCreate() {
        super.onCreate()
        com.example.butcetakip.data.DataRepository.init(this)
    }
}
