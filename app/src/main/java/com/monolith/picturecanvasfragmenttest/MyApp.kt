package com.monolith.picturecanvasfragmenttest

import android.app.Application
import android.graphics.Bitmap


class MyApp: Application(){

    var ImageBuffer: Bitmap?=null

    var DIRECTORY:String?=null

    //このコメント下にグローバル変数等記述、必要分のみをコメント付きで記述すること

    //開始時処理
    override fun onCreate(){
        super.onCreate()
    }

    companion object{
        private var instance: MyApp?=null
        fun getInstance(): MyApp {
            if(instance ==null)
                instance = MyApp()
            return instance!!
        }
    }

}