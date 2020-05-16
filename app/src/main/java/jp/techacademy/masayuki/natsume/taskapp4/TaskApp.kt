package jp.techacademy.masayuki.natsume.taskapp4

import android.app.Application
import io.realm.Realm

class TaskApp: Application() {   //親ｸﾗｽの継承
    override fun onCreate() {
        super.onCreate()
        Realm.init(this) //Realmの初期化
    }
}
