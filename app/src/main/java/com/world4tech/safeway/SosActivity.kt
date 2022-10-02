package com.world4tech.safeway

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.SmsManager
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.world4tech.safeway.database.DBHelper
import com.world4tech.safeway.databinding.ActivitySosBinding
import com.world4tech.safeway.util.MyTask
import java.lang.String
import kotlin.Exception
import kotlin.Long
import kotlin.toString


class SosActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySosBinding
    var counter = 10
    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val i = intent?.extras?.getString("currentlocation").toString()
        val lat = intent?.extras?.getString("lat").toString()
        val lon = intent?.extras?.getString("lon").toString()
        val option = intent?.extras?.getString("option").toString()
        println("Received data is: $lat and longitude is $lon and current location is: $i")
        //DB helper
        val db = DBHelper(this, null)
        // and add to name text view
        val cursor = db.getName()
        cursor!!.moveToFirst()
        binding.name.append(cursor.getString(cursor.getColumnIndex(DBHelper.NAME_COl)) + "\n")
        binding.ephone1.append(cursor.getString(cursor.getColumnIndex(DBHelper.EPHONE_COL_ONE)) + "\n")
        binding.ephone2.append(cursor.getString(cursor.getColumnIndex(DBHelper.EPHONE_COL_TWO)) + "\n")
        while(cursor.moveToNext()){
            binding.name.append(cursor.getString(cursor.getColumnIndex(DBHelper.NAME_COl)) + "\n")
            binding.ephone1.append(cursor.getString(cursor.getColumnIndex(DBHelper.EPHONE_COL_ONE)) + "\n")
            binding.ephone2.append(cursor.getString(cursor.getColumnIndex(DBHelper.EPHONE_COL_TWO)) + "\n")
        }
        cursor.close()
        println("Name is: ${binding.name.text} ephone1 is: ${binding.ephone1.text} ephone2 is: ${binding.ephone2.text}")
        var message = "Sos Message!! ${binding.name.text} ,Last location is $i & Click on the link to check current location :- https://www.google.com/maps/place/$lat,$lon"
        var phonenum = binding.ephone1.toString()
        binding.NameHere.text = binding.name.text.toString()
        //------------------
        val counttime: TextView = binding.countdown
        object : CountDownTimer(11000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                counttime.setText(String.valueOf(counter))
                counter--
            }
            override fun onFinish() {
                var phonenum = binding.ephone1.text.toString()
                var phonenum2 = binding.ephone2.text.toString()
                val name = binding.name.text.toString()
                var lastlocation  = i.toString()
                println("------0000000000000000000------------------")
                println("Fetched name is: $name")
                var message1 = "This is emergency message!!, Harsh Last locatino is: $lastlocation "
                var message2 = "Emergency message \nClick on the link to track $name 's last location :- https://www.google.com/maps/place/$lat,$lon"
                try {
                    if(option=="1"){
                        val smsManager: SmsManager = SmsManager.getDefault()
                        smsManager.sendTextMessage(phonenum, null, message1, null, null)
                        smsManager.sendTextMessage(phonenum, null, message2, null, null)
                        smsManager.sendTextMessage(phonenum2, null, message1, null, null)
                        smsManager.sendTextMessage(phonenum2, null, message2, null, null)
                        Toast.makeText(applicationContext, "Message Sent", Toast.LENGTH_LONG).show()
                        makePhoneCall()
                    }else{
                        val safenow = "$name,feeling safe now 🙂"
                        val smsManager: SmsManager = SmsManager.getDefault()
                        smsManager.sendTextMessage(phonenum, null, safenow, null, null)
                        smsManager.sendTextMessage(phonenum2, null, safenow, null, null)
                        Toast.makeText(applicationContext, "Message Sent", Toast.LENGTH_LONG).show()

                    }

                } catch (e: Exception) {
                    Toast.makeText(applicationContext, e.message.toString(), Toast.LENGTH_LONG)
                        .show()
                }
            }
        }.start()
        binding.latitudeLocation.text = "$lat,$lon"
        binding.callNow.setOnClickListener {
            makePhoneCall()
        }
        binding.cancelNow.setOnClickListener {
            binding.countdown.visibility=View.INVISIBLE
            finish()

        }

        binding.accidentAttack.setOnClickListener {
            val name = binding.NameHere.text.toString()
            val i = Intent(this,HelpActivity::class.java)
            i.putExtra("btn_no","1")
            i.putExtra("name",name)
            startActivity(i)
        }
        binding.heartAttack.setOnClickListener {
            val name = binding.NameHere.text.toString()
            val i = Intent(this,HelpActivity::class.java)
            i.putExtra("btn_no","2")
            i.putExtra("name",name)
            startActivity(i)
        }
        binding.theftAttack.setOnClickListener {
            val name = binding.NameHere.text.toString()
            val i = Intent(this,HelpActivity::class.java)
            i.putExtra("btn_no","3")
            i.putExtra("name",name)
            startActivity(i)
        }
    }

    private fun makePhoneCall() {
        val phone_number: kotlin.String = binding.ephone1.text.toString()
        val phone_intent = Intent(Intent.ACTION_CALL)
        phone_intent.data = Uri.parse("tel:+91$phone_number")
        startActivity(phone_intent)
    }
//    private fun sendmessage(phonenum: String, message: String) {
//        println("============================Phone Number ===========================")
//        println(" phone number is: $phonenum and message is: $message")
//        println("--------------------------------------------------------------------")
//        try {
//            MyTask.run(this,phonenum,message)
//        } catch (ex: java.lang.Exception) {
//            println("${ex.message.toString()}")
//            ex.printStackTrace()
//        }
//    }
}