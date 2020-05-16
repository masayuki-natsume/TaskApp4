package jp.techacademy.masayuki.natsume.taskapp4

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import io.realm.Realm
import kotlinx.android.synthetic.main.content_input.*
import java.util.*
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent

//入力によるﾃﾞｰﾀﾍﾞｰｽ変更保存ｸﾗｽ
class InputActivity : AppCompatActivity() {

    //ﾌﾟﾛﾊﾟﾃｲの定義
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0
    private var mTask: Task? = null //Taskｸﾗｽのｵﾌﾞｼﾞｪｸﾄ

    //日付設定のﾘｽﾅｰButtonの記述
    private val mOnDateClickListener = View.OnClickListener {
        val datePickerDialog = DatePickerDialog(this, //日付入力のﾒｯｿﾄﾞ
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                mYear = year
                mMonth = month
                mDay = dayOfMonth
                val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
                date_button.text = dateString //Buttonの日付更新
            }, mYear, mMonth, mDay)
        datePickerDialog.show()
    }

    //時間設定のﾘｽﾅｰButtonの記述
    private val mOnTimeClickListener = View.OnClickListener {
        val timePickerDialog = TimePickerDialog(this, //時間入力のﾒｯｿﾄﾞ
            TimePickerDialog.OnTimeSetListener { _, hour, minute -> //時間更新のﾒｯｿﾄﾞ
                mHour = hour
                mMinute = minute
                val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
                times_button.text = timeString
            }, mHour, mMinute, false)
        timePickerDialog.show()
    }

    //決定のﾘｽﾅｰButtonの記述
    private val mOnDoneClickListener = View.OnClickListener {
        addTask()                             //Realmに保存/更新
        finish()                              //Activityを閉じてMain画面に戻る
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        // ActionBarを設定する
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)           //toolbarをActionBarとして使えるように設定
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true) //ActionBarに戻るﾎﾞﾀﾝを表示
        }

        // UI部品の設定
        date_button.setOnClickListener(mOnDateClickListener)
        times_button.setOnClickListener(mOnTimeClickListener)
        done_button.setOnClickListener(mOnDoneClickListener)

        // EXTRA_TASK から Task の id を取得して、 id から Task のインスタンスを取得する
        val intent = intent            //ｸﾗｽを超えてﾃﾞｰﾀの渡し
        val taskId = intent.getIntExtra(EXTRA_TASK, -1) //taskのidを受け取る
        val realm = Realm.getDefaultInstance() //ｵﾌﾞｼﾞｪｸﾄを取得
        mTask = realm.where(Task::class.java).equalTo("id", taskId).findFirst()
        realm.close()

        if (mTask == null) {                   //task idが-1の時(addTaskで指定idは必ず0以上)
            // 新規作成の場合
            val calendar = Calendar.getInstance()
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
        } else {
            // 更新の場合
            title_edit_text.setText(mTask!!.title)
            category_edit_text.setText(mTask!!.category) //追加
            content_edit_text.setText(mTask!!.contents)

            val calendar = Calendar.getInstance()
            calendar.time = mTask!!.date
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)

            val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
            val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)

            date_button.text = dateString
            times_button.text = timeString
        }
    }

    //ﾃﾞｰﾀﾍﾞｰｽに保存
    private fun addTask() {
        val realm = Realm.getDefaultInstance() //Realmｵﾌﾞｼﾞｪｸﾄを取得

        realm.beginTransaction() //Realmの変更の処理に必要ﾒｯｿﾄﾞ

        if (mTask == null) {
            // 新規作成の場合
            mTask = Task()

            val taskRealmResults = realm.where(Task::class.java).findAll()

            val identifier: Int =
                if (taskRealmResults.max("id") != null) {
                    taskRealmResults.max("id")!!.toInt() + 1
                } else {
                    0
                }
            mTask!!.id = identifier
        }

        val title = title_edit_text.text.toString()
        val category = category_edit_text.text.toString() //追加
        val content = content_edit_text.text.toString()

        mTask!!.title = title
        mTask!!.category = category //追加
        mTask!!.contents = content
        val calendar = GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute)
        val date = calendar.time
        mTask!!.date = date

        realm.copyToRealmOrUpdate(mTask!!) //ｵﾌﾞｼﾞｪｸﾄがあれば更新 無ければ追加
        realm.commitTransaction() //処理の後に必要ﾒｯｿﾄﾞ

        realm.close()

        val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java) //ﾌﾞﾛｰﾄﾞｷｬｽﾄを受け取る
        resultIntent.putExtra(EXTRA_TASK, mTask!!.id) //Task情報を
        val resultPendingIntent = PendingIntent.getBroadcast(
            this,                    //引1
            mTask!!.id,                      //引2
            resultIntent,                     //引3
            PendingIntent.FLAG_UPDATE_CURRENT //引4 既存のPｲﾝﾃﾝﾄがあればextraのﾃﾞｰﾀだけ置き換え指定
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager //ｻｰﾋﾞｽ取得して設定
        //引1 UTC時間指定ｽﾘｰﾌﾟでもｱﾗｰﾑ発行 引2 ﾀｽｸ時間をUTCで指定
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, resultPendingIntent)
    }
}
