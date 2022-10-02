package com.world4tech.safeway.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.world4tech.safeway.MapsActivity
import com.world4tech.safeway.R
import com.world4tech.homework.database.Notes



class MyAdapter(mContext: Context):RecyclerView.Adapter<MyAdapter.myViewHolder>() {
    private var context: Context? = mContext

    private var notesList= emptyList<Notes>()
    class myViewHolder(itemView: View):RecyclerView.ViewHolder (itemView){
        val txt_type:TextView = itemView.findViewById(R.id.locationname)
        val txt_adress:TextView = itemView.findViewById(R.id.adress)
        val recentImage: ImageView = itemView.findViewById(R.id.recentImg)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recent_layout,parent,false)
        return myViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val currentItem=notesList[position]
        holder.txt_type.text=currentItem.loc
        holder.txt_adress.text = currentItem.address
        holder.recentImage.setOnClickListener {
            val i = Intent(context!!.applicationContext, MapsActivity::class.java)
            i.putExtra("lat","${currentItem.lat}")
            i.putExtra("lon","${currentItem.lon}")
            context!!.startActivity(i)
        }
    }
    fun getTaskAt(position: Int): Notes {
        return notesList.get(position)
    }


    override fun getItemCount(): Int {
        return notesList.size
    }

    fun setData(notes:List<Notes>){

        this.notesList=notes
        notifyDataSetChanged()
    }

}