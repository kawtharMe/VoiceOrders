package com.example.mousa3idi;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;


public class LaunchService extends Service implements RecognitionListener {
    Intent voicerecognize;
    SpeechRecognizer sr;
RestartBroadcastReceiver br;



    @Nullable
    @Override

    public IBinder onBind(Intent intent) {


        return null;
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId) {



     listen();

        return START_REDELIVER_INTENT;

    }
    public void listen() {
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(this);
        voicerecognize = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        voicerecognize.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        voicerecognize.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);// spilling
        voicerecognize.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-EG");
         voicerecognize.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,9000000);

        sr.startListening(voicerecognize);


    }



    @Override
    public void onDestroy() {

        super.onDestroy();
        sr.destroy();

    }


    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int i) {
        sr.cancel();
        listen();

    }

    @Override
    public void onResults(Bundle results) {


        ArrayList<String> res = results.getStringArrayList(sr.RESULTS_RECOGNITION);

        if(res.get(0).equals("توقف"))
             stopSelf();
        else{
            if (res.get(0).equals("مساعدي") || res.get(0).equals("مساعد")) {
                PackageManager pm = this.getPackageManager();
                Log.d("act", "aaaa1");
                sr.cancel();
                Intent appStartIntent = pm.getLaunchIntentForPackage("com.example.mousa3idi");

                try {
                    this.startActivity(appStartIntent);
                } catch (Exception e) {

                }
            }
        }
    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }




    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
            .setNotificationSilent()
                    .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                    .setContentText("").build();

            startForeground(1,notification);
        }
    }

}