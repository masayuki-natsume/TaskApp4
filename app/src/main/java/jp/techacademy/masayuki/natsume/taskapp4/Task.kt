package jp.techacademy.masayuki.natsume.taskapp4

import java.io.Serializable
import java.util.Date
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class Task: RealmObject(), Serializable{ //親ｸﾗｽとｲﾝﾀｰﾌｪｲｽを継承
    var title: String = ""       // タイトル
    var category: String = ""   // ｶﾃｺﾞﾘ
    var contents: String = ""   // 内容
    var date: Date = Date()      // 日時

    // id をプライマリーキーとして設定
    @PrimaryKey
    var id: Int = 0

}