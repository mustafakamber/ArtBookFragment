package com.mustafakamber.artbookfragmentkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //options_menu.xml'i MainActivity'e baglama
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.options_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.add_art){
            //DetailsFragment.kt'ye gidilecek (Yeni bir sanat girdi ekrani acilacak)
            val action = ListFragmentDirections.actionListFragmentToDetailsFragment("new",0)
            Navigation.findNavController(this,R.id.fragment).navigate(action)
        }
        return super.onOptionsItemSelected(item)
    }
}