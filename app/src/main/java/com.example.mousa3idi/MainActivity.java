package com.example.mousa3idi;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;

import android.media.AudioManager;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.telephony.SmsManager;
import android.util.Log;

import android.view.MenuItem;
import android.widget.Toast;


import com.example.mousa3idi.ui.HomeFragment;
import com.example.mousa3idi.ui.ImageFragment;
import com.example.mousa3idi.ui.ReadMessageFragment;
import com.example.mousa3idi.ui.SendMessageFragment;

import com.example.mousa3idi.ui.AlarmFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Intent.FLAG_EXCLUDE_STOPPED_PACKAGES;

public class MainActivity extends AppCompatActivity implements  BottomNavigationView.OnNavigationItemSelectedListener, RecognitionListener, ImageFragment.MyListener {
    private MediaPlayer mp = null;

    int i = 0;
    private String kVoiceRssServer = "http://api.voicerss.org";
    private String kVoiceRSSAppKey = "8baf7e0e9a23442db2174218fd443f7f";

    Intent voicerecogize;
    SpeechRecognizer sr;
    String[] number = new String[6];
    String[] msg = new String[6];
    String[] dates = new String[6];
    Cursor cursor;

    String contact = "";
    AlarmManager alarmManager ;
    Intent alarmIntent;
    PendingIntent pendingIntent;
    int h, m, requestCode=1;

Intent intentService;


    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED||ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED||ActivityCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED||ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(this, "android.permission.READ_CONTACTS") != PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(this, "android.permission.READ_SMS") != PackageManager.PERMISSION_GRANTED ||ActivityCompat.checkSelfPermission(this,Manifest.permission.CALL_PHONE) !=PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.SEND_SMS, Manifest.permission.INTERNET, Manifest.permission.CALL_PHONE,Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS},
                    1);
        }
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        getMessages(this);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.bar);

        IntentFilter i=new IntentFilter(Intent.ACTION_SCREEN_OFF);
        i.addAction(Intent.ACTION_SCREEN_ON);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            registerReceiver(new RestartBroadcastReceiver(), i, null, null, FLAG_EXCLUDE_STOPPED_PACKAGES);

        loadFragment(new HomeFragment());
        BottomNavigationView navigation = findViewById(R.id.nav_view);
        navigation.setOnNavigationItemSelectedListener(this);
        listen(1);
        intentService=new Intent(this, LaunchService.class);
        stopService(intentService);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sr.destroy();

    }

    public void listen(int i) {
        requestCode=i;
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(this);
        voicerecogize = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        voicerecogize.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        voicerecogize.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);// spilling
        voicerecogize.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-EG");
        voicerecogize.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,9000000);

        voicerecogize.putExtra("requestCode",i);
        sr.startListening(voicerecogize);


    }



    public void onResults(Bundle data){

        Log.d("reqq",String.valueOf(requestCode));
        ArrayList<String> results = new ArrayList<String>();
        results = data.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (requestCode == 1) {

            if (results.get(0).equals("اقرا")) {
                getMessages(this);
                loadFragment(new ReadMessageFragment());
                String text1 = textMessage(0, this);
                readMessage(text1, 1);
                i++;

            } else if (results.get(0).equals("التالي")) {
                getMessages(this);
                loadFragment(new ReadMessageFragment());
                String text1 = textMessage(i, this);
                readMessage(text1, 1);
                if (i != 5)
                    i++;
                else
                    i = 0;

            } else if (results.get(0).equals("ارسل")) {
                sr.cancel();
                 loadFragment(new SendMessageFragment());
                readMessage("الرجاء إعطاء اسم او رقم الشخص", 2);

            } else if (results.get(0).equals("غادر")) {
                finish();
            } else if (results.get(0).equals("منبه")) {
                sr.cancel();
                loadFragment(new AlarmFragment());
                readMessage("الرجاء إعطاء الساعة من صفر الى ثلاث وعشرين", 4);

            } else if (results.get(0).equals("الغاءمنبه")) {
                alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                if (alarmManager != null) {
                    alarmManager.cancel(pendingIntent);
                }
                readMessage("لقد تم الغاء المنبه", 1);
            } else if (results.get(0).equals("اتصل")) {
                sr.cancel();
                readMessage("الرجاء اعطاء اسم او رقم الشخص", 6);
            } else if (results.get(0).equals("صورة")||results.get(0).equals("صوره")||results.get(0).equals("صور")) {

                  loadFragment(new ImageFragment());

            } else if(results.get(0).equals("الوقت")||results.get(0).equals("وقت")||results.get(0).equals("لوقت")) {
                String s=getTime();
                String h=s.substring(0,2);
                h=numberToStringHours(h);
                String m=s.substring(3,5);
                m=numberToStringMin(m);
                if(h.equals("er")||m.equals("er"))
                    readMessage("يوجد خطا في تحديد الوقت الرجاء تكرار المحاولة" ,1);
                else
                    readMessage(h+m,1);

            }else if(results.get(0).equals("اشرح")){

                readMessage("ادخل للاعدادات و انقر على التطبيقات والإشعارات ثم انقر على مساعدي و بعدها ادخل لاذونات و قم بتفعيل كل الاذونات المطلوبة" ,1);
            }else if(results.get(0).equals("مفاتيح")||results.get(0).equals("مفاتي")){


                readMessage1("لفتح التطبيقة صوتيا: عند فتح الشاشة و سماع الإشارة الصوتية انطق مساعدي و  لايقاف خدمة الاستماع الهادفة لفتح التطبيقة انطق توقف لتحديد مضمون الصورة المحيطة بك انطق صورة للاتصال بشخص انطق اتصل و لالغاء الاتصال انطق الغ ");

            }
            else {
                readMessage("لمعرفة كيفية استعمال التطبيقة انطق مفاتيح",1);

            }
        } else if (requestCode == 2) {
            sr.cancel();

            if (results.get(0).equals("الغي"))
                listen(1);
            else {
                if( android.util.Patterns.PHONE.matcher(results.get(0)).matches()==false)
                { contact = get_Number(results.get(0));
                    if(contact!=null)
                        readMessage("الرجاء إعطاء الرسالة", 3);
                    else
                        readMessage("الرجاء التثبت من اسم المرسل اليه واعطاء اسم شخص مسجل بهاتفك بالعربية", 2);
                }
                else
                {
                    readMessage("الرجاء إعطاء الرسالة", 3);}
            }

        } else if (requestCode == 3) {
            sr.cancel();
            String msg = results.get(0);
            if (results.get(0).equals("الغي"))
                listen(1);
            else {

                SendMessageFragment f=(SendMessageFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                f.setSend(contact,msg);
                Intent smsIntent = new Intent(Intent.ACTION_SEND);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) !=
                        PackageManager.PERMISSION_GRANTED) {

                    readMessage("يجب عليك تفعيل الاذن اللازم للاستفادة من هاته الخدمة و لمعرفة الطريقة انطق اشرح" ,1);

                } else {


                    try {

                        String scAddress = null;

                        PendingIntent sentIntent = null, deliveryIntent = null;
                        // Use SmsManager.
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage
                                (contact, scAddress, msg,
                                        sentIntent, deliveryIntent);
                        readMessage("تم بعث الرسالة", 1);

                    } catch (Exception ex) {

                        readMessage("الرجاء التثبت من اسم المرسل اليه واعطاء اسم شخص مسجل بهاتفك بالعربية", 2);
                    }
                }
            }

        } else if (requestCode == 4) {
            sr.cancel();

            if (results.get(0).equals("الغي"))
                listen(1);
            else {

                try {


                    h = stringToNumber(results.get(0));

                }catch(Exception e){h=-1;}
                if (h < 1 || h > 23)
                    readMessage("يوجد خطأ في الساعةالرجاء إعطاء الساعة من جديد", 4);
                else readMessage("الرجاء إعطاءالدقائق ", 5);
            }

        } else if (requestCode == 5) {
            sr.cancel();

            if (results.get(0).equals("الغي"))
                listen(1);
            else {
                try{

                   m = stringToNumber(results.get(0));
                }catch(Exception e){m=-1;}
                if (m < 0 || m >= 60)
                    readMessage("يوجد خطأ في الدقائق الرجاء إعطاء الدقائق من جديد", 4);
                else {
                    createAlarm(this, h, m);

                    AlarmFragment f=(AlarmFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);

                    f.fill(h,m);
                    readMessage("تم ضبط المنبه على الساعة " + h + "وا" + m + "دقيقة", 1);
                }
            }
        } else if (requestCode == 6) {
            sr.cancel();
            if (results.get(0).equals("الغي"))
                listen(1);
            else {
                contact = get_Number(results.get(0));
                if(contact==null)
                    readMessage("الرجاء التثبت من اسم المرسل اليه واعطاء اسم شخص مسجل بهاتفك بالعربية" ,6);
                else{
                    try {
                        call(this, contact);
                    } catch (Exception e) {

                        readMessage("يوجد مشكل في الاتصال", 1);
                    }}
                listen(1);
            }
        }


    }

    public void call(Context context, String c) {
        if (ActivityCompat.checkSelfPermission(context,Manifest.permission.CALL_PHONE) !=PackageManager.PERMISSION_GRANTED)
            readMessage("يجب عليك تفعيل الاذن اللازم للاستفادة من هاته الخدمة و لمعرفة الطريقة انطق اشرح" ,1);

        else{Intent callIntent = new Intent(Intent.ACTION_CALL);

            callIntent.setData(Uri.parse("tel:" + c));
            startActivity(callIntent);
        }
    }


    public void keywords() {
           readMessage("لفتح التطبيقة صوتيا: عند فتح الشاشة و سماع الإشارة الصوتية انطق مساعدي و  لايقاف خدمة الاستماع الهادفة لفتح التطبيقة انطق توقف لتحديد مضمون الصورة المحيطة بك انطق صف او صف لي للاتصال بشخص انطق اتصل و لالغاء الاتصال انطق أَلْغِ لقراءة رسالة انطق اقرا و للمرور للرسالة التالية انطق التالي لبعث رسالة انطق أرسل لضبط المنبه انطق منبه و لإلغائه انطق الغاءالمنبه للاستفسار عن الوقت انطق وقت لإغلاق التطبيقة انطق غادر", 1);
    }

    public void readMessage(String text1, int i) {
         sr.cancel();
        try {
            final MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setVolume(1, 1);
                    mp.start();
                }
            };

            final MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    return false;
                }
            };

            final MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                    mp = null;
                }
            };

            String url = buildSpeechUrl(text1, "ar-sa");

            AudioManager audioManager = (AudioManager) getSystemService(MainActivity.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

            try {
                if (mp != null)
                    mp.release();

                mp = new MediaPlayer();
                mp.setDataSource(url);
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mp.setOnErrorListener(onErrorListener);
                mp.setOnCompletionListener(onCompletionListener);
                mp.setOnPreparedListener(onPreparedListener);
                mp.prepareAsync();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {

                        listen(i);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void readMessage1(String text1) {
        sr.cancel();
        try {
            final MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setVolume(1, 1);
                    mp.start();
                }
            };

            final MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    return false;
                }
            };

            final MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                    mp = null;
                }
            };

            String url = buildSpeechUrl(text1, "ar-sa");

            AudioManager audioManager = (AudioManager) getSystemService(MainActivity.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

            try {
                if (mp != null)
                    mp.release();

                mp = new MediaPlayer();
                mp.setDataSource(url);
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mp.setOnErrorListener(onErrorListener);
                mp.setOnCompletionListener(onCompletionListener);
                mp.setOnPreparedListener(onPreparedListener);
                mp.prepareAsync();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {

                        readMessage(" لقراءة رسالة انطق اقرا و للمرور للرسالة التالية انطق التالي لضبط المنبه انطق منبه و لإلغائه انطق الغاء المنبه للاستفسار عن الوقت انطق وقت لإغلاق التطبيقة انطق غادر", 1);

                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void init(){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
    }
    public Cursor getMessages(Context context) {
        if (ContextCompat.checkSelfPermission(context, "android.permission.READ_SMS") != PackageManager.PERMISSION_GRANTED)
            readMessage("يجب عليك تفعيل الاذن اللازم للاستفادة من هاته الخدمة و لمعرفة الطريقة انطق اشرح" ,1);

        else{cursor = context.getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, "date DESC limit 5");
            String msgData = "";
            if (cursor != null) { // must check the result to prevent exception
                cursor.moveToFirst();
                int j = 0;
                do {
                    msgData += " " + cursor.getColumnName(i) + ":" + cursor.getString(i);
                    number[j] = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                    msg[j] = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                    dates[j] = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    j++;

                } while (cursor.moveToNext() && (j < 5));
            }
        }
        return cursor;
    }

    public String textMessage(int i, Context context) {
        String nom = getContactName(context, number[i]);
        String text1;
        Log.d("nomm", nom);
        String dat = millisToDate(dates[i]);
        if (i==0)
            text1 = buildText(nom, dat, msg[i],true);
        else
            text1 = buildText(nom, dat, msg[i],false);
        return text1;
    }

    /**
     * Build speech URL.
     */
    private String buildSpeechUrl(String words, String language) {
        String url = "";

        url = kVoiceRssServer + "/?key=" + kVoiceRSSAppKey + "&t=text&hl=" + language + "&src=" + words;
        Log.d("aaa", url);
        return url;
    }

    public String get_Number(String name) {
        String number = name;


        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};
        if (ContextCompat.checkSelfPermission(this, "android.permission.READ_CONTACTS") != PackageManager.PERMISSION_GRANTED)
            readMessage("يجب عليك تفعيل الاذن اللازم للاستفادة من هاته الخدمة و لمعرفة الطريقة انطق اشرح" ,1);

        else{Cursor people = getContentResolver().query(uri, projection, null, null, null);

            int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            people.moveToFirst();
            do {
                String Name = people.getString(indexName);
                String Number = people.getString(indexNumber);
                if (Name.equalsIgnoreCase(name)) {
                    return Number.replace("-", "");
                }
                // Do work...
            } while (people.moveToNext());
        }
        Log.d("numm", number);
        return null;
    }

    private String buildText(String nom, String dat, String msg, Boolean j) {
        String t = "";
        Log.d("nomm", nom);
        if(j==true)
            t = " آخر رسالة وصلتك من " + nom + " بتاريخ " + dat + " هي " + msg;
        else
            t = "  وصلتك رسالة من " + nom + " بتاريخ " + dat + " هي " + msg;
        Log.d("test", t);
        return t;
    }


    @SuppressLint("Range")
    public String getContactName(Context context, String number) {

        String name = number;

        // define the columns I want the query to return
        String[] projection = new String[]{
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup._ID};
        if (ContextCompat.checkSelfPermission(context, "android.permission.READ_CONTACTS") != PackageManager.PERMISSION_GRANTED)

            readMessage("يجب عليك تفعيل الاذن اللازم للاستفادة من هاته الخدمة و لمعرفة الطريقة انطق اشرح" ,1);
        else{
            Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

            Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);

            if (cursor.moveToFirst()) {

                name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));

            } else {

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

    public int stringToNumber(String s) {
        if (s.equals("صفر") || s.equals("0"))
            return 0;
        else if (s.equals("واحد") || s.equals("1"))
            return 1;
        else if (s.equals("اثنان") || s.equals("اثنين") || s.equals("اثنتان") || s.equals("2"))
            return 2;
        else if (s.equals("ثلاثة") || s.equals("ثلاث") || s.equals("3"))
            return 3;
        else if (s.equals("اربعة") || s.equals("اربع") || s.equals("اربعه") || s.equals("4"))
            return 4;
        else if (s.equals("خمسة") || s.equals("خمس") || s.equals("خمسه") || s.equals("5"))
            return 5;
        else if (s.equals("ستة") || s.equals("ست") || s.equals("سته") || s.equals("6"))
            return 6;
        else if (s.equals("سبعة") || s.equals("سبع") || s.equals("سبعه") || s.equals("7"))
            return 7;
        else if (s.equals("ثمانية") || s.equals("ثمان") || s.equals("8"))
            return 8;
        else if (s.equals("تسعة") || s.equals("تسع") || s.equals("تسعه") || s.equals("9"))
            return 9;
        else if (s.equals("عشرة") || s.equals("عشر") || s.equals("عشره") || s.equals("10"))
            return 10;
        else if (s.equals("احدى عشرة") || s.equals("احدى عشر") || s.equals("احدى عشره") || s.equals("11"))
            return 11;
        else if (s.equals("12") || s.equals("اثنا عشرة") || s.equals("اثنا عشر") || s.equals("اثنا عشره") || s.equals("اثنتي عشرة") || s.equals("اثنتي عشر") || s.equals("اثنتي عشره") || s.equals("اثنتا عشرة") || s.equals("اثنتا عشر") || s.equals("اثنتا عشره"))
            return 12;
        else if (s.equals("13") || s.equals("ثلاثة عشرة") || s.equals("ثلاث عشرة") || s.equals("ثلاثة عشره") || s.equals("ثلاث عشر") || s.equals("ثلاث عشره") || s.equals("ثلاثة عشر"))
            return 13;
        else if (s.equals("14") || s.equals("اربعة عشرة") || s.equals("اربعة عشر") || s.equals("اربعة عشره") || s.equals("اربع عشر") || s.equals("اربع عشره") || s.equals("اربع عشرة"))
            return 14;
        else if (s.equals("15") || s.equals("خمسة عشرة") || s.equals("خمسة عشر") || s.equals("خمسة عشره") || s.equals("خمس عشر") || s.equals("خمس عشره") || s.equals("خمس عشرة"))
            return 15;
        else if (s.equals("16") || s.equals("ستة عشرة") || s.equals("ستة عشر") || s.equals("ستة عشره") || s.equals("ست عشر") || s.equals("ست عشره") || s.equals("ست عشرة"))
            return 16;
        else if (s.equals("17") || s.equals("سبعة عشرة") || s.equals("سبعة عشر") || s.equals("سبعة عشره") || s.equals("سبع عشر") || s.equals("سبع عشره") || s.equals("سبع عشرة"))
            return 17;
        else if (s.equals("18") || s.equals("ثمانية عشرة") || s.equals("ثمانية عشر") || s.equals("ثمانية عشره") || s.equals("ثماني عشر") || s.equals("ثماني عشره") || s.equals("ثماني عشرة"))
            return 18;
        else if (s.equals("19") || s.equals("تسعة عشرة") || s.equals("تسعة عشر") || s.equals("تسعة عشره") || s.equals("تسع عشر") || s.equals("تسع عشره") || s.equals("تسع عشرة"))
            return 19;
        else if (s.equals("20") || s.equals("عشرون") || s.equals("عشرين "))
            return 20;
        else if (s.equals("21") || s.equals("واحدوعشرون") || s.equals("واحدوعشرين"))
            return 21;
        else if (s.equals("22") || s.equals("اثنان وعشرون") || s.equals("اثنين وعشرون") || s.equals("اثنتان وعشرون") || s.equals("اثنان وعشرين") || s.equals("اثنين وعشرين") || s.equals("اثنتان وعشرين"))
            return 22;
        else if (s.equals("23") || s.equals("ثلاثة وعشرون") || s.equals("ثلاث وعشرون") || s.equals("ثلاثه وعشرون") || s.equals("ثلاثة وعشرين") || s.equals("ثلاث وعشرين") || s.equals("ثلاثه وعشرين"))
            return 23;
        else if (s.equals("24") || s.equals("اربعة وعشرون") || s.equals("اربع وعشرون") || s.equals("اربعه وعشرون") || s.equals("اربعة وعشرين") || s.equals("اربع وعشرين") || s.equals("اربعه وعشرين"))
            return 24;
        else if (s.equals("25") || s.equals("خمسة وعشرون") || s.equals("خمس وعشرون") || s.equals("خمسه وعشرون") || s.equals("خمسة وعشرين") || s.equals("خمس وعشرين") || s.equals("خمسه وعشرين"))
            return 25;
        else if (s.equals("26") || s.equals("ستة وعشرون") || s.equals("ست وعشرون") || s.equals("سته وعشرون") || s.equals("ستة وعشرين") || s.equals("ست وعشرين") || s.equals("سته وعشرين"))
            return 26;
        else if (s.equals("27") || s.equals("سبعة وعشرون") || s.equals("سبع وعشرون") || s.equals("سبعه وعشرون") || s.equals("سبعة وعشرين") || s.equals("سبع وعشرين") || s.equals("سبعه وعشرين"))
            return 27;
        else if (s.equals("28") || s.equals("ثمانية وعشرون") || s.equals("ثمان وعشرين") || s.equals("ثمانيه وعشرون") || s.equals("ثمانية وعشرين") || s.equals("ثمان وعشرين") || s.equals("ثمانيه وعشرين"))
            return 28;
        else if (s.equals("29") || s.equals("تسعة وعشرون") || s.equals("تسع وعشرون") || s.equals("تسعه وعشرون") || s.equals("تسعة وعشرين") || s.equals("تسع وعشرين") || s.equals("تسعه وعشرين"))
            return 29;

        else if (s.equals("30") || s.equals("ثلاثون") || s.equals("عشرين "))
            return 30;
        else if (s.equals("31") || s.equals("واحدوثلاثون") || s.equals("واحدوثلاثين"))
            return 31;
        else if (s.equals("32") || s.equals("اثنان وثلاثون") || s.equals("اثنين وثلاثون") || s.equals("اثنتان وثلاثون") || s.equals("اثنان وثلاثين") || s.equals("اثنين وثلاثين") || s.equals("اثنتان وثلاثين"))
            return 32;
        else if (s.equals("33") || s.equals("ثلاثة وثلاثون") || s.equals("ثلاث وثلاثون") || s.equals("ثلاثه وثلاثون") || s.equals("ثلاثة وعشرين") || s.equals("ثلاث وثلاثين") || s.equals("ثلاثه وثلاثين"))
            return 33;
        else if (s.equals("34") || s.equals("اربعة وثلاثون") || s.equals("اربع وثلاثون") || s.equals("اربعه وثلاثون") || s.equals("اربعة وثلاثين") || s.equals("اربع وثلاثين") || s.equals("اربعه وثلاثين"))
            return 34;
        else if (s.equals("35") || s.equals("خمسة وثلاثون") || s.equals("خمس وثلاثون") || s.equals("خمسه وثلاثون") || s.equals("خمسة وثلاثين") || s.equals("خمس وثلاثين") || s.equals("خمسه وثلاثين"))
            return 35;
        else if (s.equals("36") || s.equals("ستة وثلاثون") || s.equals("ست وثلاثون") || s.equals("سته وثلاثون") || s.equals("ستة وثلاثين") || s.equals("ست وثلاثين") || s.equals("سته وثلاثين"))
            return 36;
        else if (s.equals("37") || s.equals("سبعة وثلاثون") || s.equals("سبع وثلاثون") || s.equals("سبعه وثلاثون") || s.equals("سبعة وثلاثين") || s.equals("سبع وثلاثين") || s.equals("سبعه وثلاثين"))
            return 37;
        else if (s.equals("38") || s.equals("ثمانية وثلاثون") || s.equals("ثمان وثلاثون") || s.equals("ثمانيه وثلاثون") || s.equals("ثمانية وثلاثين") || s.equals("ثمان وثلاثين") || s.equals("ثمانيه وثلاثين"))
            return 38;
        else if (s.equals("39") || s.equals("تسعة وثلاثون") || s.equals("تسع وثلاثون") || s.equals("تسعه وثلاثون") || s.equals("تسعة وثلاثين") || s.equals("تسع وثلاثين") || s.equals("تسعه وثلاثين"))
            return 39;

        else if (s.equals("40") || s.equals("اربعون") || s.equals("اربعين"))
            return 40;
        else if (s.equals("41") || s.equals("واحدواربعون") || s.equals("واحدواربعين"))
            return 41;
        else if (s.equals("42") || s.equals("اثنان واربعون") || s.equals("اثنين واربعون") || s.equals("اثنتان واربعون") || s.equals("اثنان واربعين") || s.equals("اثنين واربعين") || s.equals("اثنتان واربعين"))
            return 42;
        else if (s.equals("43") || s.equals("ثلاثة واربعون") || s.equals("ثلاث واربعون") || s.equals("ثلاثه واربعون") || s.equals("ثلاثة واربعين") || s.equals("ثلاث واربعين") || s.equals("ثلاثه واربعين"))
            return 43;
        else if (s.equals("44") || s.equals("اربعة واربعون") || s.equals("اربع واربعون") || s.equals("اربعه واربعون") || s.equals("اربعة واربعين") || s.equals("اربع واربعين") || s.equals("اربعه واربعين"))
            return 44;
        else if (s.equals("45") || s.equals("خمسة واربعون") || s.equals("خمس واربعون") || s.equals("خمسه واربعون") || s.equals("خمسة واربعين") || s.equals("خمس واربعين") || s.equals("خمسه واربعين"))
            return 45;
        else if (s.equals("46") || s.equals("ستة واربعون") || s.equals("ست واربعون") || s.equals("سته واربعون") || s.equals("ستة واربعين") || s.equals("ست واربعين") || s.equals("سته واربعين"))
            return 46;
        else if (s.equals("47") || s.equals("سبعة واربعون") || s.equals("سبع واربعون") || s.equals("سبعه واربعون") || s.equals("سبعة واربعين") || s.equals("سبع واربعين") || s.equals("سبعه واربعين"))
            return 47;
        else if (s.equals("48") || s.equals("ثمانية واربعون") || s.equals("ثمان واربعون") || s.equals("ثمانيه واربعون") || s.equals("ثمانية واربعين") || s.equals("ثمان واربعين") || s.equals("ثمانيه واربعين"))
            return 48;
        else if (s.equals("49") || s.equals("تسعة واربعون") || s.equals("تسع واربعون") || s.equals("تسعه واربعون") || s.equals("تسعة واربعين") || s.equals("تسع واربعين") || s.equals("تسعه واربعين"))
            return 49;

        else if (s.equals("50") || s.equals("خمسون") || s.equals("خمسين"))
            return 50;
        else if (s.equals("51") || s.equals("واحدوخمسون") || s.equals("واحدوخمسين"))
            return 51;
        else if (s.equals("52") || s.equals("اثنان وخمسون") || s.equals("اثنين وخمسون") || s.equals("اثنتان وخمسون") || s.equals("اثنان وخمسين") || s.equals("اثنين وخمسين") || s.equals("اثنتان وخمسين"))
            return 52;
        else if (s.equals("53") || s.equals("ثلاثة وخمسون") || s.equals("ثلاث وخمسون") || s.equals("ثلاثه وخمسون") || s.equals("ثلاثة وخمسين") || s.equals("ثلاث وخمسين") || s.equals("ثلاثه وخمسين"))
            return 53;
        else if (s.equals("54") || s.equals("اربعة وخمسون") || s.equals("اربع وخمسون") || s.equals("اربعه وخمسون") || s.equals("اربعة وخمسين") || s.equals("اربع وخمسين") || s.equals("اربعه وخمسين"))
            return 54;
        else if (s.equals("55") || s.equals("خمسة وخمسون") || s.equals("خمس وخمسون") || s.equals("خمسه وخمسون") || s.equals("خمسة وخمسين") || s.equals("خمس وخمسين") || s.equals("خمسه وخمسين"))
            return 55;
        else if (s.equals("56") || s.equals("ستة وخمسون") || s.equals("ست وخمسون") || s.equals("سته وخمسون") || s.equals("ستة وخمسين") || s.equals("ست وخمسين") || s.equals("سته وخمسين"))
            return 56;
        else if (s.equals("57") || s.equals("سبعة وخمسون") || s.equals("سبع وخمسون") || s.equals("سبعه وخمسون") || s.equals("سبعة وخمسين") || s.equals("سبع وخمسين") || s.equals("سبعه وخمسين"))
            return 57;
        else if (s.equals("58") || s.equals("ثمانية وخمسون") || s.equals("ثمان وخمسون") || s.equals("ثمانيه وخمسون") || s.equals("ثمانية وخمسين") || s.equals("ثمان وخمسين") || s.equals("ثمانيه وخمسين"))
            return 58;
        else if (s.equals("59") || s.equals("تسعة وخمسون") || s.equals("تسع وخمسون") || s.equals("تسعه وخمسون") || s.equals("تسعة وخمسين") || s.equals("تسع وخمسين") || s.equals("تسعه وخمسين"))
            return 59;
        else return 66;


    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Log.d("onReadyForSpech","onReadyFor");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d("onBeginning","onBeg");
    }

    @Override
    public void onRmsChanged(float v) {
        Log.d("onRmsChanged","onRmsChanged");
    }

    @Override
    public void onBufferReceived(byte[] bytes) {
        Log.d("onBuffer","onBuffer");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d("onEndOfSpeech","onEndOfSpeech");
    }

    @Override
    public void onError(int i) {
        sr.cancel();
        listen(requestCode);

    }


    @Override
    public void onPartialResults(Bundle bundle) {
        Log.d("onPartial","onPartial");
    }

    @Override
    public void onEvent(int i, Bundle bundle) {
        Log.d("onEvent","onEvent");
    }


    public void createAlarm(Context context, int h, int m) {
        alarmIntent = new Intent(context, AlarmBroadcastReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(
                this.getApplicationContext(), 280192, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, h);
        calendar.set(Calendar.MINUTE, m);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_HOUR, pendingIntent);
    }






    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {

        super.onPause();
    }
    public String getTime(){
        String ct = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        return ct;
    }
    public void translate(String text){
        StringBuilder t = new StringBuilder();

        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(FirebaseTranslateLanguage.EN)
                        .setTargetLanguage(FirebaseTranslateLanguage.AR)
                        .build();
        final FirebaseTranslator translator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                readMessage("يوجد خطا ما الرجاء تكرار المحاولة",1);
                            }
                        });
        translator.translate(text)
                .addOnSuccessListener(new OnSuccessListener<String>() {

                    public void onSuccess(@NonNull String translatedText) {
                        t.append(translatedText);
                        String s=t.toString();
                        readMessage("الصورة تحتوي على " + s, 1);
                        Log.d("translation", t.toString());
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Error.
                                Log.d("trans error",e.toString());
                                readMessage("يوجد خطا ما الرجاء تكرار المحاولة",1);
                            }
                        });



    }
    public String numberToStringHours(String s){
        String res="";
        int n=Integer.parseInt(s);
        switch(n){
            case 01: res= "الساعةالواحدة" ;
                break;
            case 02: res= "الساعةالثانية";
                break;
            case 03: res=   "الساعةالثالثة";
                break;
            case 04: res=     "الساعةالرابعة";
                break;
            case 05: res=    "الساعةالخامسة";
                break;
            case 06: res=     "الساعةالسادسة";
                break;
            case 07: res=    "الساعةالسابعة";
                break;
            case 8: res=     "الساعةالثامنة";
                break;
            case 9: res=             "الساعةالتاسعة";
                break;
            case 10:res=        "الساعةالعاشرة"   ;
                break;
            case 11: res=     "الساعةالحادية عشرة";
                break;
            case 12: res=     "الساعةالثانية عشرة";
                break;
            case 13: res=    "الساعةالثالثة عشرة";
                break;
            case 14: res=    "الساعة الرابعة عشرة";
                break;
            case 15: res=    "الساعة الخامسة عشرة";
                break;
            case 16: res=      "الساعةالسادسة عشرة" ;
                break;
            case 17: res=     "الساعة السابعة عشرة";
                break;
            case 18: res=     "الساعة الثامنة عشرة";
                break;
            case 19: res=    "الساعةالتاسعة عشرة";
                break;
            case 20: res=    "العشرون ";
                break;
            case 21: res=   "الساعة الواحدةوالعشرون";
                break;
            case 22: res=      "الساعةالثانية و العشرون";
                break;

            case 23: res=  "الساعةالثالثة والعشرون";
                break;
            case 00: res=  " الساعةمنتصفالليل";
                break;
            default: res="er";
        }
        return res;
    }
    public String numberToStringMin(String s){
        String res="";
        Integer n=Integer.parseInt(s);
        switch(n){
            case 01: res= "و دقيقة" ;
                break;
            case 02: res= "و دقيقتان";
                break;
            case 03: res=   "و ثلاث دقائق";
                break;
            case 04: res=     "و اربع دقائق";
                break;
            case 05: res=    "وخمس دقائق";
                break;
            case 06: res=     "وست دقائق";
                break;
            case 07: res=    "و سبع دقائق";
                break;
            case 8: res=     "وثمان دقائق";
                break;
            case 9: res=             "وتسع دقائق";
                break;
            case 10:res=        "وعشر دقائق"   ;
                break;
            case 11: res=     "و احدى عشرة دقيقة";
                break;
            case 12: res=     "واثنا عشرة دقيقة";
                break;
            case 13: res=    "وثلاث عشرة دقيقة";
                break;
            case 14: res=    "واربع عشرة دقيقة";
                break;
            case 15: res=    "وخمس عشرة دقيقة";
                break;
            case 16: res=      "وست عشرة دقيقة" ;
                break;
            case 17: res=     "وسبع عشرة دقيقة";
                break;
            case 18: res=     "وثماني عشرة دقيقة";
                break;
            case 19: res=    "وتسع عشرةدقيقة";
                break;
            case 20: res=    "وعشرون دقيقة";
                break;
            case 21: res=   "و واحد وعشرون دقيقة";
                break;
            case 22: res=      "واثنان وعشرون دقيقة";
                break;

            case 23: res=  "وثلاث وعشرون دقيقة";
                break;
            case 24: res=  "و  اربع وعشرون دقيقة";
                break;

            case 25: res=    "وخمس و عشرون دقيقة";
                break;
            case 26: res=   "و ست  وعشرون دقيقة";
                break;
            case 27: res=      "و سبع  و عشرون دقيقة";
                break;

            case 28: res=  "و ثمان وعشرون دقيقة";
                break;
            case 29: res=  "و تسع و عشرون دقيقة";
                break;

            case 30: res=    "وثلاثون دقيقة";
                break;
            case 31: res=   "و واحد وثلاثون دقيقة";
                break;
            case 32: res=      "واثنان وثلاثون دقيقة";
                break;

            case 33: res=  "وثلاث وثلاثون دقيقة";
                break;
            case 34: res=  "و  اربع وثلاثون دقيقة";
                break;

            case 35: res=    "وخمس و ثلاثون دقيقة";
                break;
            case 36: res=   "و ست  وثلاثون دقيقة";
                break;
            case 37: res=      "و سبع  و ثلاثون دقيقة";
                break;

            case 38: res=  "و ثمان و ثلاثون دقيقة";
                break;
            case 39: res=  "و تسع و ثلاثون دقيقة";
                break;

            case 40: res=    "واربعون دقيقة";
                break;
            case 41: res=   "و واحد واربعون دقيقة";
                break;
            case 42: res=      "واثنان واربعون دقيقة";
                break;

            case 43: res=  "وثلاث واربعون دقيقة";
                break;
            case 44: res=  "و  اربع واربعون دقيقة";
                break;

            case 45: res=    "وخمس و اربعون دقيقة";
                break;
            case 46: res=   "و ست  واربعون دقيقة";
                break;
            case 47: res=      "و سبع  و اربعون دقيقة";
                break;

            case 48: res=  "و ثمان و اربعون دقيقة";
                break;
            case 49: res=  "و تسع و اربعون دقيقة";
                break;
            case 50: res=    "وخمسون دقيقة";
                break;
            case 51: res=   "و واحد وخمسون دقيقة";
                break;
            case 52: res=      "واثنان وخمسون دقيقة";
                break;

            case 53: res=  "وثلاث وخمسون دقيقة";
                break;
            case 54: res=  "و  اربع وخمسون دقيقة";
                break;

            case 55: res=    "وخمس و خمسون دقيقة";
                break;
            case 56: res=   "و ست  وخمسون دقيقة";
                break;
            case 57: res=      "و سبع  و خمسون دقيقة";
                break;

            case 58: res=  "و ثمان و خمسون دقيقة";
                break;
            case 59: res=  "و تسع و خمسون دقيقة";
                break;
            default: res="er";
        }
        return res;
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment

        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)

                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {


        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragment = new HomeFragment();
                break;

            case R.id.navigation_read:
                fragment = new ReadMessageFragment();
                break;

            case R.id.navigation_send:
                fragment = new SendMessageFragment();
                break;

            case R.id.navigation_alarm:
                fragment = new AlarmFragment();
                break;
            case R.id.navigation_image:
                fragment = new ImageFragment();
                break;

        }

        return loadFragment(fragment);
    }
}

