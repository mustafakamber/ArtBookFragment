package com.mustafakamber.artbookfragmentkotlin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.mustafakamber.artbookfragmentkotlin.databinding.RecyclerRowBinding

class ArtListAdapter(val artList : ArrayList<Art>) : RecyclerView.Adapter<ArtListAdapter.ArtHolder>() {
    class ArtHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        //recycler_row'u ListFragmentAdapter'a baglama
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ArtHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
        //Erkanda gosterilecekler
        //Art ismini recyclerView listesinde gosterme
        holder.binding.recyclerViewTextView.text = artList.get(position).name
        //Art ismine basilinca yapilacaklar
        holder.itemView.setOnClickListener {
            //Kayitli bir gorsel gosterilecek
            val action = ListFragmentDirections.actionListFragmentToDetailsFragment("old",artList[position].id)
            Navigation.findNavController(it).navigate(action)
        }

    }

    override fun getItemCount(): Int {
        //Kac tane ArrayList elemanimiz var
        println(artList.size)
        return artList.size
    }

}