package jp.techacademy.masayuki.natsume.taskapp4

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class TaskAdapter(context: Context): BaseAdapter() {
    private val mLayoutInflater: LayoutInflater //xmlのViewを取り扱う　ﾌﾟﾛﾊﾟﾃｲ
    var taskList = mutableListOf<Task>() //taskListﾌﾟﾛﾊﾟﾃｲ(ｱｲﾃﾑを保持するﾘｽﾄを定義)

    init {                                                  //Realmの初期化
        this.mLayoutInflater = LayoutInflater.from(context)
    }

    override fun getCount(): Int {                 //mTaskListのｻｲｽﾞ(数)
        return taskList.size
    }

    override fun getItem(position: Int): Any {     //mTaskListの要素(ﾃﾞｰﾀ)
        return taskList[position]
    }

    override fun getItemId(position: Int): Long {  //mTaskListのﾃﾞｰﾀID
        return 0
    }

    //BaseAdapterｸﾗｽのgetViewﾒｯｿﾄﾞの実装
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: mLayoutInflater.inflate(R.layout.row_task_list,null)

        val textView1 = view.findViewById<TextView>(R.id.title_edit_text)
        val textView2 = view.findViewById<TextView>(R.id.category_edit_text) //追加
        val textView3 = view.findViewById<TextView>(R.id.content_edit_text)  //追加

        // Taskクラスから情報を取得
        textView1.text = taskList[position].title
        textView2.text = taskList[position].category //追加
        textView3.text = taskList[position].contents //追加

        return view
    }
}