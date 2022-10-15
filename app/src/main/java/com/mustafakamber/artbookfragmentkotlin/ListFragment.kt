package com.mustafakamber.artbookfragmentkotlin

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.mustafakamber.artbookfragmentkotlin.databinding.FragmentListBinding
import java.lang.Exception


class ListFragment : Fragment() {
      private var _binding: FragmentListBinding? = null
      private val binding get() = _binding!!

      private lateinit var database: SQLiteDatabase

      private lateinit var artList : ArrayList<Art>

      private lateinit var artAdapter : ArtListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        artList = ArrayList<Art>()
        getDataFromSQLite()



    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListBinding.inflate(inflater,container,false)
        val view = binding.root
        return view




    }

    //DetailsFragment'daki kayitli verileri cekme
    fun getDataFromSQLite(){
        try{
            //database'i cekme
            val database = requireActivity().openOrCreateDatabase("Arts",Context.MODE_PRIVATE,null)
            //cursor ile beraber databasedeki verileri okuyup cekme
            val cursor = database.rawQuery("SELECT * FROM arts",null)
            val artNameIx = cursor.getColumnIndex("artname")
            val idIx = cursor.getColumnIndex("id")

            //verileri cekme
            while(cursor.moveToNext()){
                val name = cursor.getString(artNameIx)
                val id = cursor.getInt(idIx)
                val art = Art(name,id)
                artList.add(art)
            }

            //adapter  en onemli madde
            artAdapter.notifyDataSetChanged()

            cursor.close()

        }catch (e : Exception){
            e.printStackTrace()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Recyclerview ile Adapter'in baglanmasi
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        artAdapter = ArtListAdapter(artList)
        binding.recyclerView.adapter = artAdapter
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }}