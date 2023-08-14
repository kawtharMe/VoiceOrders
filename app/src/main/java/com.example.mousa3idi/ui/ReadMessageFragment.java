package com.example.mousa3idi.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mousa3idi.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ReadMessageFragment extends Fragment {


    private MediaPlayer mp = null;

    String[] number = new String[6];
    String[] msg = new String[6];
    String[] dates = new String[6];
    Cursor cursor;

    private String kVoiceRssServer = "http://api.voicerss.org";
    private String kVoiceRSSAppKey = "8baf7e0e9a23442db2174218fd443f7f";
    ListView lv;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_read, container, false);
        lv = root.findViewById(R.id.lv);


        if (ContextCompat.checkSelfPermission(getActivity(), "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED) {
            cursor = getActivity().getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, "date DESC limit 5");

            if (cursor != null) {
                cursor.moveToFirst();
                int j = 0;
                do {


                    number[j] = cursor.getString(2);
                    msg[j] = cursor.getString(13);
                    dates[j] = cursor.getString(6);
                    Log.d("number", number[j]);
                    Log.d("msg", msg[j]);
                    Log.d("date", dates[j]);
                    Log.d("tes", getContactName(getActivity(), number[0]));
                    //  }
                    j++;

                } while (cursor.moveToNext() && (j < 5));


                cursor.moveToFirst();

                //Attached Cursor with adapter and display in listview
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.item_list, cursor,
                        new String[]{"address", "date", "body"}, new int[]{
                        R.id.adresse, R.id.dates, R.id.msg});

                adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

                    public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) {

                        if (aColumnIndex == aCursor.getColumnIndex("date")) {
                            String createDate = aCursor.getString(aColumnIndex);
                            TextView textView = (TextView) aView;
                            textView.setText(millisToDate(createDate));
                            return true;
                        }
                        if (aColumnIndex == aCursor.getColumnIndex("address")) {
                            String number = aCursor.getString(aColumnIndex);
                            TextView textView = (TextView) aView;

                            if (getContactName(getActivity(), number) != null)
                                textView.setText(getContactName(getActivity(), number));
                            else textView.setText(number);
                            return true;
                        }

                        return false;
                    }
                });

                lv.setAdapter(adapter);
            } else {

            }
        } else Log.d("err", "errooooor");

     //   listen();

return root;
    }


    public String textMessage(int i,Context context) {
        String nom = getContactName(context, number[i]);

        Log.d("nomm", nom);
        String dat = millisToDate(dates[i]);
        String text1 = buildText(nom, dat, msg[i]);
        return text1;
    }

    private String buildText(String nom, String dat, String msg) {
        String t = "";
        Log.d("nomm", nom);
        t = " آخر رسالة وصلتك من " + nom + " بتاريخ " + dat + " هي " + msg;
        Log.d("test", t);
        return t;
    }


    @SuppressLint("Range")
    public String getContactName(Context context, String number) {

        String name = number;


        String[] projection = new String[]{
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup._ID};
        if (ContextCompat.checkSelfPermission(context, "android.permission.READ_CONTACTS") == PackageManager.PERMISSION_GRANTED) {

            Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

            Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);

            if (cursor.moveToFirst()) {

                name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                Log.v("num", "Started uploadcontactphoto: Contact Found @ " + number);
                Log.v("nom", "Started uploadcontactphoto: Contact name  = " + name);
            } else {
                Log.v("errrr", "Contact Not Found @ " + number);
            }
            cursor.close();
        }

        return name;
    }

    static boolean isNum(String s) {
        try {
            int i = Integer.parseInt(s);
            return true;
        } catch (NumberFormatException er) {
            return false;
        }
    }

    public static String millisToDate(String TimeMillis) {
        String finalDate;
        long tm = Long.parseLong(TimeMillis);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(tm);
        Date date = calendar.getTime();
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
        finalDate = outputFormat.format(date);
        return finalDate;
    }

}




