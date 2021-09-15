package com.nureddinelmas.artbookkotlin

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nureddinelmas.artbookkotlin.databinding.RecyclerRowBinding

class ArtBookAdapter(private val artbookList : ArrayList<ArtBook>) :
    RecyclerView.Adapter<ArtBookAdapter.ArtBookHolder> (){

        class ArtBookHolder(val binding : RecyclerRowBinding): RecyclerView.ViewHolder(binding.root){

        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtBookHolder {
        // Yukarida olusturdugumuz ArtBookHolder ilk olusturdugumuz da ne olacak?? burayi onun icin girecegiz
        // burada layout ile baglama islemini yapiyoruz, recycler_row burada baglayacagiz

        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtBookHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtBookHolder, position: Int) {
        // burasi ise baglandiktan sonra ne olacak, hangi textte hangi veri kullanilacak
        holder.binding.recyclerViewTextView.text = artbookList.get(position).name
        //holder.binding.recyclerViewImageView.setImageResource(artbookList.get(position).image)

        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView.context, DetailsActivity::class.java)
            intent.putExtra("info", "old")
            intent.putExtra("id", artbookList[position].id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        // kac tane olusturacagiz bundan
        return artbookList.size
    }
}