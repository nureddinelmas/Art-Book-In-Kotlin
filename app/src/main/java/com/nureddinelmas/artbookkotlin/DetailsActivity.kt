package com.nureddinelmas.artbookkotlin

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.google.android.material.snackbar.Snackbar
import com.nureddinelmas.artbookkotlin.databinding.ActivityDetailsBinding
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.sql.DatabaseMetaData
import java.util.jar.Manifest

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedBitmap : Bitmap? = null
    private lateinit var database : SQLiteDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val actionBar = supportActionBar
        actionBar!!.title = "Art Book Details"
        val intent = intent

        database = this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE, null)

        val info = intent.getStringExtra("info")
        if(info.equals("new")){
            binding.artBookName.setText("")
            binding.artBookYear.setText("")
            binding.artBookCountry.setText("")
            binding.imageView.setImageResource(R.drawable.selectimage)
            binding.save.visibility = View.VISIBLE
            binding.save.text = "Save"


        }else{

            binding.save.text = "Delete"
            binding.imageView.isEnabled = false
            binding.artBookName.isEnabled = false
            binding.artBookYear.isEnabled = false
            binding.artBookCountry.isEnabled = false


            val selectedId = intent.getIntExtra("id", 1)

            val cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))
            val artBookNameIx = cursor.getColumnIndex("artname")
            val artBookCountryIx = cursor.getColumnIndex("artcountry")
            val yearIx = cursor.getColumnIndex("year")
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){

                binding.artBookCountry.setText(cursor.getString(artBookCountryIx))
                binding.artBookYear.setText(cursor.getString(yearIx))
                binding.artBookName.setText(cursor.getString(artBookNameIx))

                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                binding.imageView.setImageBitmap(bitmap)

            }
            cursor.close()

        }

        registerLauncher()
    }

    fun saveClicked(view: View){

        if (binding.save.text == "Save"){


        var artBookName = binding.artBookName.text.toString()
        var artBookCountry = binding.artBookCountry.text.toString()
        var artBookYear = binding.artBookYear.text.toString()

        if(selectedBitmap != null){
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 300)

            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 50,outputStream)
            val byteArray = outputStream.toByteArray()

            try {
                // database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
                database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR, artcountry VARCHAR, year VARCHAR,image  BLOB)")

                val sqlString = "INSERT INTO arts (artname, artcountry, year, image) VALUES (?, ?, ?, ?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1,artBookName)
                statement.bindString(2,artBookCountry)
                statement.bindString(3,artBookYear)
                statement.bindBlob(4,byteArray)
                statement.execute()

            }catch (e : Exception){
                e.printStackTrace()
            }

            val intent = Intent(this@DetailsActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // bundan once kac tane akviti varsa hepsini kapatiyor
            startActivity(intent)

        }

    }else{
            val selectedId = intent.getIntExtra("id", 0)
            val db = this.database

           db.delete("arts","id = ?", arrayOf(selectedId.toString()))

            val intent = Intent(this@DetailsActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)





        }
    }


    private fun makeSmallerBitmap(image : Bitmap, maximumSize: Int): Bitmap{

        var width =image.width
        var height = image.height

        val bitmapRatio : Double = width.toDouble()/height.toDouble()

        if (bitmapRatio > 1){
            // landscape
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        }
        else{

            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun selectImage(view: View){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            // Eger kullanici izin vermemis ise
            // rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                // kullanicinin izin vermesi icin bilgilendirme yapmak
                Snackbar.make(view, "Permission needed", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener { result ->
                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }).show()
            }
            else{
                // permission
                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)

            }
        }else{
            // kullanici zaten izin vermisse
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            // intent
            activityResultLauncher.launch(intentToGallery)


        }
    }

    private fun registerLauncher(){
        // kullanicinin galerisindan bilgi almak ve resimleri cekmek

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if (result.resultCode == RESULT_OK){
                val intentFromResult = result.data
                if (intentFromResult != null){
                    val imageData = intentFromResult.data
                    if (imageData != null){
                        try {
                            if (Build.VERSION.SDK_INT >= 28){
                                val source = ImageDecoder.createSource(this@DetailsActivity.contentResolver, imageData)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }else{
                                selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageData)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }
                        }catch (e : Exception) {
                            e.printStackTrace()
                    }
                }
                }

            }

        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result->
            if (result){
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
            else{
                Toast.makeText(this@DetailsActivity, "Permission Needed", Toast.LENGTH_LONG).show()
            }

        }

    }
}