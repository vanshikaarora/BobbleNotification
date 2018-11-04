package vanshika.example.com.notofication;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.net.URLEncoder;

public class Confirmation extends Activity {
    Button yes, no;
    String name;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);
        yes = findViewById(R.id.yes);
        no = findViewById(R.id.no);
        name = getIntent().getStringExtra("Name");
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPermission();

            }
        });
    }

    private String getContact(String name) {
/*
        if(name.contains("(")){
            name = name.split("(")[0];
        }
*/
        /*String ret = null;
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like'%" + name +"%'";
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor c = getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, selection, null, null);
        if (c.moveToFirst()) {
            ret = c.getString(0);
        }
        c.close();
        if(ret==null)
            ret = "Unsaved";
        return ret;*/
        final String[] projection = {
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.Data.MIMETYPE,
                "account_type",
                ContactsContract.Data.DATA3,
        };

        final String selection = ContactsContract.Data.MIMETYPE + " =? and account_type=?";
        final String[] selectionArgs = {
                "vnd.android.cursor.item/vnd.com.whatsapp.profile",
                "com.whatsapp"
        };

        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(
                ContactsContract.Data.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);
        String watssappNumber = null;
        while (c.moveToNext()) {
            String id = c.getString(c.getColumnIndex(ContactsContract.Data.CONTACT_ID));
            String number = c.getString(c.getColumnIndex(ContactsContract.Data.DATA3));
            String name1 = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

            Log.v("WhatsApp", "name " + name1 + " - number - " + number);
            if (name1.equals(name)) {
                watssappNumber = number;
                Log.v("line80", name + " " + watssappNumber);
            }

        }
        Log.v("WhatsApp", "Total WhatsApp Contacts: " + c.getCount());
        c.close();
        return watssappNumber;
    }


    private void getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method

        } else {
            String phone_no = getContact(name);
            Log.v("line32", phone_no + " ");
            if (phone_no == null) {
                return;
            }
            sendMessage(phone_no);
        }
    }

    private void sendMessage(String num) {
        if (num != null) {
            num = num.replace("+", "").replace(" ", "");

            /*Intent sendIntent = new Intent("android.intent.action.MAIN");
            sendIntent.putExtra("jid", num+ "@s.whatsapp.net");
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Test");
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setPackage("com.whatsapp");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);*/
            PackageManager packageManager = getPackageManager();
            Intent i = new Intent(Intent.ACTION_VIEW);

            try {
                String url = "https://api.whatsapp.com/send?phone=" + num + "&text=" + URLEncoder.encode("Hey", "UTF-8");
                i.setPackage("com.whatsapp");
                i.setData(Uri.parse(url));
                if (i.resolveActivity(packageManager) != null) {
                    startActivity(i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // contains spaces.

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {


            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
