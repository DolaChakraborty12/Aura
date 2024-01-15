package com.example.splash;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class SOS extends AppCompatActivity {

    TextView textView, textView2, textView3;  // References for the TextViews
    ImageButton imageButton4, imageButton5, imageButton6;  // References for the ImageButtons
    Button button, deleteButton;


    String[] cnumber = new String[3];  // Array to store the contact numbers for up to 3 contacts
    static final int PICK_CONTACT = 1;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        // Initialize TextViews and ImageButtons
        textView = findViewById(R.id.textView4);
        textView2 = findViewById(R.id.textView5);
        textView3 = findViewById(R.id.textView6);
        sharedPreferences = getSharedPreferences("MyContacts", MODE_PRIVATE);


        imageButton4 = findViewById(R.id.imageButton4);
        imageButton5 = findViewById(R.id.imageButton5);
        imageButton6 = findViewById(R.id.imageButton6);

        button = findViewById(R.id.button2);
        sharedPreferences = getSharedPreferences("MyContacts", MODE_PRIVATE);
        Button saveButton = findViewById(R.id.saveButton);
        Button dlt = findViewById(R.id.dltbtn);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_CONTACTS},
                    1);
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CALL_PHONE},
                    2);
        }

        // Set onClickListener for the button to pick a contact
        button.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, PICK_CONTACT);
        });

        // Set onClickListeners for the ImageButtons to initiate calls
        imageButton4.setOnClickListener(v -> makeCall(0));
        imageButton5.setOnClickListener(v -> makeCall(1));
        imageButton6.setOnClickListener(v -> makeCall(2));
        loadSavedContacts();

        // Clear the contact associated with textView6

        dlt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clearContact(0);
                    }
                });
                textView2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clearContact(1);
                    }
                });
                textView3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clearContact(2);
                    }
                });
            }
        });

        saveButton.setOnClickListener(v -> {
            saveContactsToSharedPreferences();
        });


    }

    @SuppressLint("Range")
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c = getContentResolver().query(contactData, null, null, null, null);
                    if (c != null && c.moveToFirst()) {
                        String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                    null, null);
                            if (phones != null && phones.moveToFirst()) {
                                String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                                int index = findNextEmptyIndex();
                                if (index != -1) {
                                    cnumber[index] = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).trim();
                                    updateTextView(index, cnumber[index], name);
                                } else {
                                    Toast.makeText(this, "All slots are filled!Please delete contact", Toast.LENGTH_SHORT).show();
                                }
                                phones.close();
                            }
                        }
                        if (c != null) {
                            c.close();
                        }
                    }
                }
                break;
        }
    }

    // Find the next empty index in cnumbers array
    private int findNextEmptyIndex() {
        for (int i = 0; i < cnumber.length; i++) {
            if (cnumber[i] == null || cnumber[i].isEmpty()) {
                return i;
            }
        }
        return -1;  // Return -1 if all slots are filled
    }

    // Update the appropriate TextView based on the index
    private void updateTextView(int index, String phoneNumber, String name) {
        switch (index) {
            case 0:
                textView.setText(name + "\n" + phoneNumber);
                break;
            case 1:
                textView2.setText(name + "\n" + phoneNumber);
                break;
            case 2:
                textView3.setText(name + "\n" + phoneNumber);
                break;
            // Add more cases if you extend the number of pairs
        }
        saveContactToSharedPreferences(index, phoneNumber, name);
    }

    private void makeCall(int index) {
        String savedPhoneNumber = sharedPreferences.getString("phoneNumber" + index, ""); // Assuming you're using SharedPreferences to store phone numbers
        // Check if the retrieved number is valid
        if (savedPhoneNumber != null && !savedPhoneNumber.trim().isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + savedPhoneNumber.trim()));  // Ensure you trim the phone number
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startActivity(intent);
        } else {
            Toast.makeText(this, "No valid number available.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearContact(int index) {
        switch (index) {
            case 0:
                textView.setText("Number1");
                break;
            case 1:
                textView2.setText("Number2");
                break;
            case 2:
                textView3.setText("Number3");
                break;
        }
        cnumber[index] = "";  // Clear the corresponding phone number
    }

    private void saveContactsToSharedPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (int i = 0; i < cnumber.length; i++) {
            String phoneNumber = cnumber[i];
            editor.putString("phoneNumber" + i, phoneNumber);
        }

        editor.apply();  // Apply changes
        Toast.makeText(SOS.this, "Saved successfully!", Toast.LENGTH_SHORT).show();
    }


    private void saveContactToSharedPreferences(int index, String phoneNumber, String name) {

        if (sharedPreferences.getString("name" + index, "").isEmpty()
                && sharedPreferences.getString("phoneNumber" + index, "").isEmpty()) {

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("name" + index, name);
            editor.putString("phoneNumber" + index, phoneNumber);
            editor.apply();  // Commit changes
        }
    }

    private void loadSavedContacts() {

        for (int i = 0; i < 3; i++) {  // Assuming you have 3 contacts to load
            String name = sharedPreferences.getString("name" + i, "");
            String phoneNumber = sharedPreferences.getString("phoneNumber" + i, "");

            if (!name.isEmpty() && !phoneNumber.isEmpty()) {
                updateTextView(i, phoneNumber, name);
            }
        }
    }
}
