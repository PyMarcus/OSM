package com.example.osm

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.osm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // hide action bar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleEvents()
    }

    override fun onClick(v: View) {
        when(v.id){
            binding.btnOpenMap.id -> openMap()
        }
    }

    private fun handleEvents(){
        binding.btnOpenMap.setOnClickListener(this)
    }

    private fun openMap(){
        startActivity(Intent(this, MapActivity::class.java))
    }
}