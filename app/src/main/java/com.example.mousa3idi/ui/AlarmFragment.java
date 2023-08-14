package com.example.mousa3idi.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.mousa3idi.AlarmBroadcastReceiver;
import com.example.mousa3idi.R;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

public class AlarmFragment extends Fragment {
Button bt;
EditText h,m;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_alarm, container, false);
h=root.findViewById(R.id.heure);
        m=root.findViewById(R.id.min);
bt=root.findViewById(R.id.bt_al);
bt.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View view) {
                              if (h.getText().toString().equals("") || m.getText().toString().equals("")) {
                                  Log.d("alarm", "test1");
                                  Toast.makeText(view.getContext(), "الرجاء إدخال كل المعلومات", Toast.LENGTH_LONG).show();
                              } else
                                  try {
                                      Log.d("alarm", "test2");
                                      int hh = Integer.parseInt(h.getText().toString());
                                      int mm = Integer.parseInt(m.getText().toString());

                                      if (hh < 0 || hh > 23)
                                          Toast.makeText(view.getContext(), "يوجد خطا في الساعات من 0 الى 23", Toast.LENGTH_LONG).show();
                                      else if (mm < 0 || mm > 59)
                                          Toast.makeText(view.getContext(), "يوجد خطا في الدقائق من 0 الى 59", Toast.LENGTH_LONG).show();
                                      else {



                                          Intent alarmIntent = new Intent(view.getContext(), AlarmBroadcastReceiver.class);
                                          PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                                  view.getContext(), 280192, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                                          Calendar calendar = Calendar.getInstance();
                                          calendar.setTimeInMillis(System.currentTimeMillis());
                                          calendar.set(Calendar.HOUR_OF_DAY, hh);
                                          calendar.set(Calendar.MINUTE, mm);

                                          AlarmManager alarmManager = (AlarmManager) view.getContext().getSystemService(ALARM_SERVICE);

                                          alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                                                  AlarmManager.INTERVAL_HOUR, pendingIntent);
                                          Toast.makeText(view.getContext(), "تم تعديل المنبه", Toast.LENGTH_LONG).show();
                                      }  } catch(Exception e){
                                          Log.d("alarm", e.toString());
                                          Toast.makeText(view.getContext(), "الرجاء إدخال كل المعلومات بشكل صحيح ساعات من 0 الى23 و دقائق من 0 الى 60", Toast.LENGTH_LONG).show();
                                      }
                                  }

                      }

);

        return root;
    }
    public void fill(int hh,int mm){
        h.setText(String.valueOf(hh));
        m.setText(String.valueOf(mm));

    }
}