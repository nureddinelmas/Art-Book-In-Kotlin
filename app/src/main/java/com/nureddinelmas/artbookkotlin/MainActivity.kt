package com.nureddinelmas.artbookkotlin

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.appcompat.app.ActionBar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.nureddinelmas.artbookkotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val actionBar = supportActionBar
        actionBar!!.title = "Art Book"


        var artbookList = ArrayList<ArtBook>()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

         var artBookAdapter = ArtBookAdapter(artbookList)

        binding.recyclerView.adapter = artBookAdapter

        try {
            val database = this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE,null)

            val cursor = database.rawQuery("SELECT * FROM arts", null)
            val artNameIx = cursor.getColumnIndex("artname")
            val artIdIx = cursor.getColumnIndex("id")

            while (cursor.moveToNext()){
                val name = cursor.getString(artNameIx)
                val id = cursor.getInt(artIdIx)
                val art = ArtBook(name,id)
                artbookList.add(art)
            }
            artBookAdapter.notifyDataSetChanged()
            cursor.close()

        } catch (e: Exception){
            e.printStackTrace()
        }


    }

    override fun onCreateOptionsMenu(menu : Menu?):Boolean{
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.art_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) : Boolean{
        if (item.itemId == R.id.art_menu ){
            val intent = Intent(this@MainActivity, DetailsActivity::class.java)
            intent.putExtra("info", "new")
            startActivity(intent)
        }
        if (item.itemId == R.id.delete_menu){
          Toast.makeText(this@MainActivity, "Please Select an item from List!",Toast.LENGTH_LONG).show()


        }
            return super.onOptionsItemSelected(item)

    }

}