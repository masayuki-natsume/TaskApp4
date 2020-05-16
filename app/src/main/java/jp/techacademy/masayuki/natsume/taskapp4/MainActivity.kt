package jp.techacademy.masayuki.natsume.taskapp4

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import io.realm.RealmChangeListener
import io.realm.Sort
import java.util.*
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.app.AlarmManager
import android.app.PendingIntent
import kotlinx.android.synthetic.main.row_task_list.*

const val EXTRA_TASK = "jp.techacademy.masayuki.natsume.taskapp4.TASK" //他ｱﾌﾟﾘのEXTRAと間違えない為に定義

class MainActivity : AppCompatActivity() {

    private lateinit var mRealm: Realm //最初の起動 mRealm変数定義
    private val mRealmListener = object : RealmChangeListener<Realm> { //追加･削除に呼ばれるListener
        override fun onChange(element: Realm) {      //ﾒｯｿﾄﾞをｵｰﾊﾞｰﾗｲﾄﾞ してreloadを呼び出す
            reloadListView()              //ListViewの更新ﾒｯｿｯﾄ
        }
    }

    private lateinit var mTaskAdapter: TaskAdapter //Adapter定義

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener { view ->
            //fab
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            startActivity(intent)

        }

        // Realmの設定
        mRealm = Realm.getDefaultInstance()        //ｵﾌﾞｼﾞｪｸﾄを取得
        mRealm.addChangeListener(mRealmListener) //設定

        // ListViewの設定
        mTaskAdapter = TaskAdapter(this@MainActivity) //内容を変数に

        // ListViewをタップしたときの処理
        listView1.setOnItemClickListener { parent, view, position, id ->
            // 入力・編集する画面に遷移させる
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)

        }

        // ListViewを長押ししたときの処理
        listView1.setOnItemLongClickListener { parent, view, position, id ->
            // タスクを削除する
            val task = parent.adapter.getItem(position) as Task

            // ダイアログを表示する
            val builder = AlertDialog.Builder(this@MainActivity)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK") { _, _ ->
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction()  //変更処理の時のﾒｯｿﾄﾞ
                results.deleteAllFromRealm()
                mRealm.commitTransaction() //処理の後のﾒｯｿﾄﾞimport android.app.AlarmManager

                val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this@MainActivity,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)

                reloadListView()
            }

            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }

        reloadListView()

        search_button.setOnClickListener {
            val str = kubun_edit_text.text.toString()
            //
            val taskRealmResults = mRealm.where(Task::class.java).equalTo("category",str).findAll().sort("date", Sort.DESCENDING)


            // 上記の結果を、TaskList としてセットする（Realmから取得したﾃﾞｰﾀをｺﾋﾟｰしてAdapterに渡す)
            mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)

            // TaskのListView用のアダプタに渡す
            listView1.adapter = mTaskAdapter

            // 表示を更新するために、アダプターにデータが変更されたことを知らせるﾒｯｿﾄﾞ
            mTaskAdapter.notifyDataSetChanged()
        }
    }

    private fun reloadListView() { //Realmに変更があった時呼び出されAdapterにﾃﾞｰﾀ設定ﾒｯｿﾄﾞ
        // Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得
        val taskRealmResults = mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

        // 上記の結果を、TaskList としてセットする（Realmから取得したﾃﾞｰﾀをｺﾋﾟｰしてAdapterに渡す)
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)

        // TaskのListView用のアダプタに渡す
        listView1.adapter = mTaskAdapter

        // 表示を更新するために、アダプターにデータが変更されたことを知らせるﾒｯｿﾄﾞ
        mTaskAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {    //Activityの破棄の時呼び出されRealmｸﾗｽのcloseﾒｯｿﾄﾞを呼ぶ
        super.onDestroy()

        mRealm.close()             //Realmｸﾗｽのｵﾌﾞｼﾞｪｸﾄを破棄
    }
}

