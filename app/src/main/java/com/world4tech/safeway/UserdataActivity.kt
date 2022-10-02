package com.world4tech.safeway

import android.content.Intent

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.world4tech.safeway.database.DBHelper
import com.world4tech.safeway.databinding.ActivityUserdataBinding


class UserdataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserdataBinding
    private var firststart:Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserdataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var pref = getSharedPreferences("prefs", MODE_PRIVATE)
        firststart = pref.getBoolean("firstStart",true)
        binding.next.setOnClickListener {
            if (checkEmpty()){
                if (checkCorrect()){
                    val db = DBHelper(this, null)
                    val name = binding.name.text.toString()
                    val email = binding.email.text.toString()
                    val phone = binding.phoneno.text.toString()
                    val ephoneone = binding.ephoneone.text.toString()
                    val ephonetwo = binding.ephonetwo.text.toString()
                    // name to our database
                    db.addName(name,email,phone,ephoneone,ephonetwo)
                    if (firststart){
                        val pref = getSharedPreferences("prefs", MODE_PRIVATE)
                        val editor = pref.edit()
                        editor.putBoolean("firstStart",false)
                        editor.apply()
                    }
                    val i = Intent(this,DestinationActivity::class.java)
                    startActivity(i)
                }
            }
        }
//
    }

    private fun checkCorrect(): Boolean {
        if (binding.phoneno.text.length>10 || binding.phoneno.text.length<10){
            binding.warning.visibility = View.VISIBLE
            binding.hintnote.text = "Kindly Correct Phone Number"
            return false
        }else if (binding.ephoneone.text.length>10 || binding.ephoneone.text.length<10){
            binding.warning.visibility = View.VISIBLE
            binding.hintnote.text = "Kindly Enter Correct Emergency Number"
            return false
        }else if (binding.ephonetwo.text.length>10 || binding.ephonetwo.text.length<10){
            binding.warning.visibility = View.VISIBLE
            binding.hintnote.text = "Kindly Enter Correct Emergency Number"
            return false
        }else{
            return true
        }
        return true

    }

    private fun checkEmpty():Boolean {
        if(TextUtils.isEmpty(binding.name.text)){
            binding.warning.visibility = View.VISIBLE
            binding.hintnote.text = "Name Field Empty"
            return false
        }else if(TextUtils.isEmpty(binding.email.text)){
            binding.warning.visibility = View.VISIBLE
            binding.hintnote.text = "Email Field Empty"
            return false
        }else if(TextUtils.isEmpty(binding.phoneno.text)){
            binding.warning.visibility = View.VISIBLE
            binding.hintnote.text = "Phone number Field Empty"
            return false
        }else if(TextUtils.isEmpty(binding.ephoneone.text)){
            binding.warning.visibility = View.VISIBLE
            binding.hintnote.text = "Kindly enter atleast 2 emergency number"
            return false
        }else if(TextUtils.isEmpty(binding.ephonetwo.text)){
            binding.warning.visibility = View.VISIBLE
            binding.hintnote.text = "Kindly enter atleast 2 emergency number"
            return false
        }else{
            return true
        }
        return true
    }

    override fun onStart() {
        if(!firststart){
            val i = Intent(this,DestinationActivity::class.java)
            startActivity(i)
        }
        super.onStart()
    }
    //---------------------------Message Permission---------------------------------------
}