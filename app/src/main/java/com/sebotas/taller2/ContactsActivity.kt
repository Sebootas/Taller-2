package com.sebotas.taller2

import android.os.Bundle
import android.provider.ContactsContract
import androidx.appcompat.app.AppCompatActivity
import android.widget.ListView

class ContactsActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var contactsAdapter: ContactsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        listView = findViewById(R.id.listView)

        // Query the contacts database and get a cursor
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
            ),
            null,
            null,
            null
        )

        // Initialize the adapter with the cursor
        contactsAdapter = ContactsAdapter(this, cursor, 0)

        // Set the adapter to the ListView
        listView.adapter = contactsAdapter
    }
}
