package com.example.mousa3idi.ui;

import android.Manifest;
import android.app.PendingIntent;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mousa3idi.R;


public class SendMessageFragment extends Fragment {
    EditText cont,msg;
    String contact;
    String mesg;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_send, container, false);
      Button bt = root.findViewById(R.id.btsend);
         cont = root.findViewById(R.id.contact);
        msg= root.findViewById(R.id.msg);
bt.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        String c = cont.getText().toString();

        mesg = msg.getText().toString();

        if (msg.getText().toString().equals("")|| cont.getText().toString().equals("")) {

            Log.d("send", "vide");
            Toast.makeText(view.getContext(), "الرجاء إدخال كل المعلومات", Toast.LENGTH_LONG).show();
        } else {
            contact = get_Number(c);

            if (ActivityCompat.checkSelfPermission(view.getContext(),
                    Manifest.permission.SEND_SMS) !=
                    PackageManager.PERMISSION_GRANTED) {
                Log.d("perm envoi", "not granted");
                Toast.makeText(view.getContext(), "يجب عليك تفعيل الاذن اللازم للاستفادة من هاته الخدمة", Toast.LENGTH_LONG).show();
            } else {
               try{

                    String scAddress = null;

                    PendingIntent sentIntent = null, deliveryIntent = null;
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage (contact, scAddress, mesg, sentIntent, deliveryIntent);
                   Toast.makeText(view.getContext(), "تم بعث الرسالة", Toast.LENGTH_LONG).show();

               } catch (Exception ex) {

                    Toast.makeText(view.getContext(), "الرجاء التثبت من رقم او اسم المرسل اليه", Toast.LENGTH_LONG).show();

                }
            }
        }
    }

    });

         return root;
    }
            public void setSend(String c,String m){
        cont.setText(c);
        msg.setText(m);
            }

    public String get_Number(String name) {
        String number = name;


        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};
        if (ContextCompat.checkSelfPermission(getActivity(), "android.permission.READ_CONTACTS") == PackageManager.PERMISSION_GRANTED) {
            Cursor people = getActivity().getContentResolver().query(uri, projection, null, null, null);

            int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            people.moveToFirst();
            do {
                String Name = people.getString(indexName);
                String Number = people.getString(indexNumber);
                if (Name.equalsIgnoreCase(name)) {
                    return Number.replace("-", "");
                }

            } while (people.moveToNext());
        }

        return null;
    }
}