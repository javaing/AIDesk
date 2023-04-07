package com.aiunion.aidesk.ai;

import static com.aiunion.aidesk.utils.ToastUtils.showToast;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class AIViewModel {
    public final String TAG = this.getClass().getSimpleName();
    private static AIViewModel sInstance;
    private static AIDialogState dialogState;
    Context mContext;

    public AIViewModel(Context c) {
        mContext = c;
        initTexttoSpeech();
    }

    public static AIViewModel getInstance(Context c) {
        synchronized (AIViewModel.class) {
            if (sInstance == null) {
                sInstance = new AIViewModel(c);
            }
        }
        return sInstance;
    }

    public void setState(AIDialogState state) {
        dialogState = state;
        Log.e(TAG, "art: setState="+dialogState );
    }

    private void say(String message) {
        textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null);
    }
    TextToSpeech textToSpeech;

    //早點初始化
    private void initTexttoSpeech() {
        Log.e(TAG, "art: initTexttoSpeech" );
        textToSpeech = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                if(i == TextToSpeech.SUCCESS)
                {
                    textToSpeech.setLanguage(Locale.CHINESE);
                    textToSpeech.setPitch(1.2f);// 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
                    textToSpeech.setSpeechRate(1f);
                    //openMic(true);
                    Log.e(TAG, "art: initTexttoSpeech SUCCESS" );
                } else {
                    Log.e(TAG, "art: initTexttoSpeech fail" );
                }
            }
        });

    }


    public void aiProcess(String input) {
        Log.e(TAG, "art: process="+dialogState );
        switch(dialogState) {
            case IS_HEAD:
                say("xxx早上好");
                break;

            case IS_EMPLYOEE:
                say("您好x經理很高興見到您");
                break;

            case NO_BOOKING:
                say("你好請問需要什麼服務嗎");
                break;

            case IS_BOOKING:
                say("請掃QR code報到");
                break;

            default:
                say("你好請問需要什麼服務嗎");
        }
    }

    void aiAssistSay(int saycase) {
        //語氣 ＋ 動作



    }


    void actionSIPCall() {

    }

    void actionFillform() {

    }

    void actionAboutUs() {

    }

    void actionGenQRCOde() {

    }
}

