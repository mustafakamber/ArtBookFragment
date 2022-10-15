package com.mustafakamber.artbookfragmentkotlin

import android.Manifest
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.mustafakamber.artbookfragmentkotlin.databinding.FragmentDetailsBinding
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception


class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>

    var selectedPicture : Uri? = null
    var selectedBitmap : Bitmap? = null

    private lateinit var database: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()

        database = requireActivity().openOrCreateDatabase("Arts", Context.MODE_PRIVATE,null)



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailsBinding.inflate(inflater,container,false)
        val view = binding.root
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageView.setOnClickListener { selectImageClicked(view) }
        binding.saveButton.setOnClickListener { saveButtonClicked(view) }
        binding.deleteButton.setOnClickListener { deleteButtonClicked(view) }
        arguments?.let {
            val info = DetailsFragmentArgs.fromBundle(it).info
            if(info.equals("new")){
                //Yeni bir sanat girdisi alinacak
                binding.artNameText.setText("")
                binding.artistNameText.setText("")
                binding.yearText.setText("")
                binding.saveButton.visibility = View.VISIBLE
                binding.deleteButton.visibility = View.INVISIBLE

                val selectedImageBackground = BitmapFactory.decodeResource(context?.resources,R.drawable.selectimage)
                binding.imageView.setImageBitmap(selectedImageBackground)

            }else{
                //Eski bir sanat verisi goruntulenecek

                binding.saveButton.visibility = View.INVISIBLE
                binding.deleteButton.visibility = View.VISIBLE
                val selectedId = DetailsFragmentArgs.fromBundle(it).id

                //Tiklanan veriyi veritabanindan cekme
                val cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))

                val artNameIx = cursor.getColumnIndex("artname")
                val artistNameIx = cursor.getColumnIndex("artistname")
                val yearIx = cursor.getColumnIndex("year")
                val imageIx = cursor.getColumnIndex("image")

                while(cursor.moveToNext()){
                    //Veriler ekranda gosterme
                    binding.artNameText.setText(cursor.getString(artNameIx))
                    binding.artistNameText.setText(cursor.getString(artistNameIx))
                    binding.yearText.setText(cursor.getString(yearIx))

                    //Resmi 0,1 verisinden bitmap'e donsuturup ekranda gosterme
                    val byteArray = cursor.getBlob(imageIx)
                    val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                    binding.imageView.setImageBitmap(bitmap)
                }

                cursor.close()
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    fun saveButtonClicked(view : View){
        //Girdileri ve resimleri kullanicidan girdi olarak alma

        //Girdileri alma
        val artName = binding.artNameText.text.toString()
        val artistName = binding.artistNameText.text.toString()
        val year = binding.yearText.text.toString()
        //Resim'i alma
        if(selectedBitmap!=null){
           val smallBitmap = makeSmallerBitmap(selectedBitmap!!,550)

           //Kullanicinin sectigi resmi (0,1) (veriye) donusturme
           val outputStream = ByteArrayOutputStream()
           smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
           val byteArray = outputStream.toByteArray()

           //Verileri veritabani olusturup kaydetme
            try{
                //Veritabani olusturma
                database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR, artistname VARCHAR, year VARCHAR, image BLOB)")

                val sqlString = "INSERT INTO arts (artname, artistname, year, image) VALUES (?, ?, ?, ?)"

                //Verileri veritabanina kaydetme
                val statement = database.compileStatement(sqlString)
                statement.bindString(1,artName)
                statement.bindString(2,artistName)
                statement.bindString(3,year)
                statement.bindBlob(4, byteArray)

                statement.execute()

            }catch (e : Exception){
                e.printStackTrace()
            }

            //Kaydetme isleminden sonra ListFragment'a git
            val action = DetailsFragmentDirections.actionDetailsFragmentToListFragment()
            Navigation.findNavController(requireView()).navigate(action)

        }else{
            Toast.makeText(requireContext(),"You have to choose an image",Toast.LENGTH_LONG).show()
        }



    }
    fun deleteButtonClicked(view : View){
        //Ekranda goruntulenen veriyi silme
        arguments.let {
            val selectedId = DetailsFragmentArgs.fromBundle(it!!).id
            database.execSQL("DELETE  FROM arts WHERE id = ?", arrayOf(selectedId.toString()))
            val action = DetailsFragmentDirections.actionDetailsFragmentToListFragment()
            Navigation.findNavController(requireView()).navigate(action)

        }



    }
    fun makeSmallerBitmap(image: Bitmap,maximumSize :Int) :Bitmap{
        var width = image.width
        var height = image.height

        val bitmapRatio : Double = width.toDouble() / height.toDouble()
        if(bitmapRatio > 1){
            //Indirilen gorsel yatay
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()

        }else{
            //Indirilen gorsel dikey
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image,width,height,true)
    }
    fun selectImageClicked(view : View){
        activity?.let {
            if(ContextCompat.checkSelfPermission(requireActivity().applicationContext,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //Daha once hic izin verilmedi izin istenecek
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)){
                    //Snackbarli izin
                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                        //Izin isteme
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }).show()

                }else{
                    //Snackbarsiz izin
                    //Izin isteme
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }else{
                //Onceden izin verilmis,galeriye gidilecek
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }
    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if(result.resultCode == AppCompatActivity.RESULT_OK){
                //Kullanici galeriden gorseli secti
                val intentFromResult = result.data
                if(intentFromResult != null){
                    selectedPicture = intentFromResult.data
                    //Secilen gorseli bitmap'e cevirip ekranda gosterme
                    try {
                        if(Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(requireActivity().contentResolver,selectedPicture!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(selectedBitmap)
                        }else{
                            selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,selectedPicture)
                            binding.imageView.setImageBitmap(selectedBitmap)
                        }
                    }catch (e : IOException){
                        e.printStackTrace()
                    }
                }
            }
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
            if(result){
                //Izin alindi galeriye gidilecek
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                //Izin alinamadi hata mesaji gosterilecek
                Toast.makeText(requireContext(),"Permission needed!!",Toast.LENGTH_LONG).show()
            }

        }
    }

}