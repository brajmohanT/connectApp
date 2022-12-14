package com.example.connectapp.ui

import android.Manifest
import android.Manifest.permission.READ_CALL_LOG
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.provider.CallLog.Calls.LIMIT_PARAM_KEY
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.connectapp.databinding.FragmentContactsBinding
import java.text.SimpleDateFormat
import java.util.*

class ContactsFragment : Fragment() {

    private val READ_CALL_LOG_REQ_CODE = 199

    // getting the instance of binding class
    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onStart() {
        super.onStart()
        getContact()
        getLogs()
    }

    private fun getContact() {
        binding.btAddContact.setOnClickListener(View.OnClickListener {
            sendIntent()
        })
    }

    private fun sendIntent() {

        var intent = Intent(Intent.ACTION_PICK);
        intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        resultLauncher.launch(intent);

        ActivityCompat.requestPermissions(
            this.requireActivity(),
            arrayOf(Manifest.permission.READ_CALL_LOG), PackageManager.PERMISSION_GRANTED
        )
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val contactUri: Uri? = it.data?.data;

                val columns = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.LAST_TIME_CONTACTED,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID
//                    ContactsContract.CommonDataKinds.Phone.DATE
                );

                val resolver = requireActivity().contentResolver
                val cursor = resolver.query(contactUri!!, columns, null, null, null)
                if (cursor?.moveToNext()!!) {
                    // arg inside the getstring are the colmn number
                    binding.etContactName.setText(cursor.getString(1))
                    binding.etContactNumber.setText(cursor.getString(4))
//                    Log.d("Braj", columns.toString())

                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getLogs() {

        binding.btnLoadLogs.setOnClickListener {
            if (checkPermission()) {
                loadLogs()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(READ_CALL_LOG),
                    READ_CALL_LOG_REQ_CODE
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            loadLogs()
        else
            Toast.makeText(requireActivity(), "permission de do", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("Range", "Recycle", "SimpleDateFormat")
    private fun loadLogs() {
        val resolver: ContentResolver = requireActivity().contentResolver
        val uri =
            CallLog.Calls.CONTENT_URI.buildUpon().appendQueryParameter(LIMIT_PARAM_KEY, "10")
                .build()
        val columns = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.DATE,
//           CallLog.Calls.
        );
            val number = "7897505316"
        val cursorCallLog = resolver.query(
            uri,
            columns,
            CallLog.Calls.NUMBER + " = ? OR "+ CallLog.Calls.NUMBER + " = ?",
            arrayOf<String> ("+91$number", number),
            CallLog.Calls.DATE + " DESC limit 1;"

            /*        CallLog.Calls.NUMBER + " = ? OR "+ CallLog.Calls.NUMBER + " = ?",
            new String[]{"+91" + custMobNo, custMobNo},
            CallLog.Calls.DATE + " DESC limit 1;");*/

            ///the new way through gpt but not effective because getting the same results
            /*        CallLog.Calls.NUMBER+" = ?",
            arrayOf<String> (number),
            CallLog.Calls.DATE+" DESC"*/
        )
        if (cursorCallLog != null) {
            while (cursorCallLog.moveToNext()) {
                val name =
                    cursorCallLog.getString(cursorCallLog.getColumnIndex(CallLog.Calls.CACHED_NAME))
                val num =
                    cursorCallLog.getString(cursorCallLog.getColumnIndex(CallLog.Calls.NUMBER))
                val date =
                    cursorCallLog.getString(cursorCallLog.getColumnIndex(CallLog.Calls.DATE))
                        .toLongOrNull()


                val simple = SimpleDateFormat(
                    "dd MMM yyyy HH:mm:ss:SSS Z"
                )

                val result = Date(date!!)

                val timeZoneDate = (simple.format(result))



                Log.d("logs", "$name  $num  $timeZoneDate")


            }
        }
    }

    private fun checkPermission(): Boolean {
        val callLogPermissionResult: Int =
            ActivityCompat.checkSelfPermission(requireActivity(), READ_CALL_LOG)
        return callLogPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


/*==============================Notes=======================*/
//27Nov
// Make the selection query to get the call history of that particular number only.
// Means keval ek number ke liye query hogi

//07Dec
/*getting the latest modified call logs but when we don't have any number in selection
* for a particular number getting latest is still far*/

//14Dec
/*
able to get the latest calling of number. Not getting the latest ones earlier
because I was searching the call logs for $number not for "+91$number"

NEXT TIME=> get the latest callLogs of the number we picking and show the time on screen.
*/

/*OLD*/
//convert the date into right format
