package com.world4tech.safeway

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.world4tech.safeway.database.DBHelper
import com.world4tech.safeway.databinding.ActivityProfileBinding


class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        For printing name
//         listener to our print name button
        val db = DBHelper(this, null)
        // and add to name text view
        val cursor = db.getName()
        cursor!!.moveToFirst()
        binding.name.append(cursor.getString(cursor.getColumnIndex(DBHelper.NAME_COl)) + "\n")
        binding.email.append(cursor.getString(cursor.getColumnIndex(DBHelper.EMAIL_COL)) + "\n")
        binding.phone.append(cursor.getString(cursor.getColumnIndex(DBHelper.PHONE_COL)) + "\n")
        binding.ephone1.append(cursor.getString(cursor.getColumnIndex(DBHelper.EPHONE_COL_ONE)) + "\n")
        binding.ephone2.append(cursor.getString(cursor.getColumnIndex(DBHelper.EPHONE_COL_TWO)) + "\n")
        while(cursor.moveToNext()){
            binding.name.append(cursor.getString(cursor.getColumnIndex(DBHelper.NAME_COl)) + "\n")
            binding.email.append(cursor.getString(cursor.getColumnIndex(DBHelper.EMAIL_COL)) + "\n")
            binding.phone.append(cursor.getString(cursor.getColumnIndex(DBHelper.PHONE_COL)) + "\n")
            binding.ephone1.append(cursor.getString(cursor.getColumnIndex(DBHelper.EPHONE_COL_ONE)) + "\n")
            binding.ephone2.append(cursor.getString(cursor.getColumnIndex(DBHelper.EPHONE_COL_TWO)) + "\n")
        }
        cursor.close()
        binding.save.setOnClickListener {
            val i = Intent(this,DestinationActivity::class.java)
            startActivity(i)
            finish()
        }
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }
}