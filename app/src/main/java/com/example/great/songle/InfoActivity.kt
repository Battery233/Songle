package com.example.great.songle

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import kotlinx.android.synthetic.main.activity_info.*

class InfoActivity : AppCompatActivity() {
    var name: Array<String> = arrayOf("Current song numbers:\n 18","Database version:\n 2017-10-09 10:00:33.775[Europe/London]","Player numbers:\n 1", "Game started times:\n 6")
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_achievements -> {
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        val adp: Any = ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                name
        )
        list_view.adapter = adp as ListAdapter?


        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
