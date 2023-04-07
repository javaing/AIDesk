package com.aiunion.aidesk.main;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;


import com.aiunion.aidesk.AdvSettingViewBinding;
import com.aiunion.aidesk.BuildConfig;
import com.aiunion.aidesk.MainViewBinding;
import com.aiunion.aidesk.R;
import com.aiunion.aidesk.SettingViewBinding;
import com.aiunion.aidesk.ai.AIDialogState;
import com.aiunion.aidesk.ai.AIViewModel;
import com.aiunion.aidesk.model.CharGPTApiClient;
import com.aiunion.aidesk.model.FaceDetectService;
import com.aiunion.aidesk.ai.pojo.ChatRequest;
import com.aiunion.aidesk.ai.pojo.GPTResult;
import com.aiunion.aidesk.ai.pojo.GPTSendMessage;
import com.aiunion.aidesk.utils.ToastUtils;
import com.aiunion.aidesk.view.DialogView;
import com.aiunion.aidesk.viewmodel.AdvSettingViewModel;
import com.aiunion.aidesk.viewmodel.MainViewModel;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private String TAG = this.getClass().getSimpleName();
    private static final int REQUEST_CODE_PERMISSION = 2;
    private static final String[] PERMISSIONS_REQ = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    //===================變數 宣告=========================
    private MainViewBinding activityBinding;
    private SettingViewBinding dialogBinding;
    private AdvSettingViewBinding advDialogBinding;

    private android.app.AlertDialog mDialog;
    private Dialog inputDialog, advSettingsDialog;
    //private static final int DIALOG_DURATION = 500;
    private volatile boolean mIpConfigured;
    //////////////////////////////
    private MediaPlayer mp;
    ///////////////////////////////
    //Handler
    private final Handler handler = new Handler(Looper.getMainLooper(), this::onHandleMessage);

    protected boolean onHandleMessage(Message msg) {
        return false;
    }

    //==================View Model========================
    private MainViewModel mViewModel;
    private AdvSettingViewModel mAdvSettingsViewModel;

    TextView tv_chat_main;
    SpeechRecognizer speechRecognizer;
    final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

    //String greeting = "請問你的版本";
    String greeting = "請問南港在哪裡";
    String greeting1 = "愛麗絲夢遊仙境的皇后象徵著什麼";
    String greeting2 = "一百字以內介紹航海王";
    //String[] talks = { "櫃台為您服務", "慢走", "你好，請問找誰，我可以幫您撥電話" };
    String[] talks = { "櫃台為您服務", "慢走", "你好" };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        //設定監聽事件
        mViewModel.setOnResourceListener(mResourceListener);
        mViewModel.setOnActionListener(mActionListener);
        //創建MainViewModel
        mViewModel.create();
        activityBinding.setVm(mViewModel);
        /**  mainViewModel init end */
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            checkPermissions(this);
        }

        initUI();
        initView();
        initVideoView();
        initSpeech();
        initTexttoSpeech();
        AIViewModel.getInstance(this);
        //openMic(true);

        //chatWithAI(greeting);
    }


    CharGPTApiClient apiService = new CharGPTApiClient();
    private void chatWithAI(String peopleSaid) {
        openMic(false);

        //組message
        GPTSendMessage m = new GPTSendMessage();
        m.setRole("user");
        m.setContent(peopleSaid);
        ArrayList al = new ArrayList();
        al.add(m);

        //組request
        ChatRequest req = new ChatRequest();
        req.setModel("gpt-3.5-turbo");
        req.setMessages(al);

        Call<GPTResult> call1 = apiService.getApiService().chatCompletions(req);
        call1.enqueue(new Callback<GPTResult>() {
            @Override
            public void onResponse(Call<GPTResult> call, Response<GPTResult> response) {
                //{"id":"chatcmpl-72GYaAWbTjIVWkec63y0PcXNJfhDp","object":"chat.completion","created":1680774656,"model":"gpt-3.5-turbo-0301","usage":{"prompt_tokens":24,"completion_tokens":97,"total_tokens":121},"choices":[{"message":{"role":"assistant","content":"我是一個AI數字助手，可以提供多種服務，例如：翻譯、日程管理、警報提醒、天氣預報、電子郵件管理、音樂播放、提供購物建議、回答問題等等。如果您有任何需求，可以直接問我。"},"finish_reason":"stop","index":0}]}
                //Log.e(TAG, "art response=" + response.body().getChoices().get(0).getMessage().getContent());

                try {
                    String content = response.body().getChoices().get(0).getMessage().getContent();
                    //Log.e(TAG, "art content=" + content);
                    say(content);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<GPTResult> call, Throwable t) {
                Log.e(TAG, "art fail=" + t.getMessage());
            }

        });
    }




    private void initSpeech() {
        if(SpeechRecognizer.isRecognitionAvailable(this)) {
            //SpeechRecognizer sr = SpeechRecognizer.createSpeechRecognizer(this);
            textViewAppend(tv_chat_main,"語音辨識啟動");
            //say("語音辨識啟動");
        } else {
            //say("語音辨識無法啟動");
            textViewAppend(tv_chat_main,"語音辨識無法啟動");
            return;
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
//        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE);
        speechRecognizer.setRecognitionListener(recognitionListener);
        //textViewAppend(tv_chat_main,"mic device OK");
    }


    RecognitionListener recognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            //textViewAppend(tv_chat_main, "speechRecognizer ready 請說...");
            //say(greeting2);
            openMic(true);
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onResults(Bundle bundle) {
            //micButton.setImageResource(R.drawable.ic_mic_black_off);
            ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String peopleDaid = data.get(0);
            textViewAppend(tv_chat_main, "[辨識結果]"+ peopleDaid);

            //chatWithAI(peopleDaid);
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
        }

        @Override
        public void onPartialResults(Bundle bundle) {

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    };

    private void textViewAppend(TextView tv, String str) {
        StringBuilder sb = new StringBuilder(tv.getText().toString());
        sb.append("\n" + str);
        tv.setText(sb.toString());
    }

    private void openMic(boolean openmic) {
        if(speechRecognizer==null) {
            textViewAppend(tv_chat_main, "speechRecognizer not init");
            return;
        }
        if(!SpeechRecognizer.isRecognitionAvailable(this)) {
            textViewAppend(tv_chat_main, "no speech");
            return;
        }
        Log.e(TAG, "art MIC:" + (openmic? "open ":"close") );
        if (openmic){
            speechRecognizer.startListening(speechRecognizerIntent);
        } else {
            speechRecognizer.stopListening();
        }
    }

    private void initUI() {
        tv_chat_main = findViewById(R.id.tv_chat_main);
        if(BuildConfig.DEBUG) {
            tv_chat_main.setVisibility(View.VISIBLE);
        }

        Button tv_call_main =  findViewById(R.id.tv_call_main);
        tv_call_main.setOnClickListener(v -> {
            ToastUtils.showToast("功能製作中");
        });
    }

    TextToSpeech textToSpeech;
    private void initTexttoSpeech() {
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                if(i == TextToSpeech.SUCCESS)
                {
                    textToSpeech.setOnUtteranceProgressListener(speakCallback);
                    textToSpeech.setLanguage(Locale.CHINESE);
                    //textToSpeech.setPitch(1.5f);// 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
                    //textToSpeech.setSpeechRate(1f);
                    //say(talks[2]);
                    openMic(true);
                    //ToastUtils.showToast("textToSpeech ready");
                }

                // if No error is found then only it will run
//                if (i != TextToSpeech.ERROR) {
//                    // To Choose language of speech
//                    textToSpeech.setLanguage(Locale.TAIWAN);
//                    textToSpeech.setPitch(1.5f);// 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
//                    textToSpeech.setSpeechRate(0.5f);
//                }

            }
        });

    }

    UtteranceProgressListener speakCallback = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
            Log.e(TAG, "art saying ");
            openMic(false);
        }

        @Override
        public void onDone(String utteranceId) {
            Log.e(TAG, "art saying done");
            openMic(true);
        }

        @Override
        public void onError(String utteranceId) {
            Log.e(TAG, "art saying error");
        }
    };

    private void initVideoView() {
        VideoView videoView = (VideoView) findViewById(R.id.videoView_main);

        //Creating MediaController
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);

        //specify the location of media file
        //String movieUrl = "file:///android_asset/fb2.mp4";
        String movieUrl = "android.resource://" + getPackageName() + "/raw/" + R.raw.fb2;
        Uri uri = Uri.parse(movieUrl);

        //Setting MediaController and URI, then starting the videoView
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(uri);
        videoView.requestFocus();
        videoView.start();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //MediaPlayer mp3player = MediaPlayer.create(MainActivity.this, R.raw.saymyname);
                //mp3player.start();

                //say(talks[0]);
            }
        }, 2000);

    }

    private void say(String message) {
        textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
    }




    private void initView() {
        /**  DialogDataBinding 初始化  開始*/

        //將dialog_ip綁定(DataBindingUtil是為了在無法確定綁定物件的情況下使用)
        dialogBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_ip, null, false);
        //inputDialog給予新的自訂義對話框
        inputDialog=new DialogView(this,dialogBinding.getRoot());
        //將view回傳至MainViewModel(需考慮生命週期)
        mViewModel.setDialogViewitem(inputDialog);
        dialogBinding.versionName.setText("Version："+getVersionName(this));
        dialogBinding.versionName.setEnabled(false);
        inputDialog.setCancelable(false);
        //客製化視窗介面
        customDialogSize(inputDialog);
        //視窗取消(不出現)一開始一定不顯示,所以直接開始臉部偵測
        inputDialog.setOnDismissListener(dialog -> {
            mIpConfigured = false;
            mViewModel.startFaceDetect();
        });
        mViewModel.stopFaceDetect();

        /**  DialogDataBinding 初始化  結束*/

        /**        進階設定(圖像設定)    初始化      開始 */
        //AdvSettingsDialog
        mAdvSettingsViewModel = ViewModelProviders.of(this).get(AdvSettingViewModel.class);
        mAdvSettingsViewModel.setOnActionListener((isSave) -> {
            mViewModel.cancelSetting();
            if (isSave) {
                mViewModel.reOpenCamera();
            }
            if (advSettingsDialog != null && advSettingsDialog.isShowing()) {
                advSettingsDialog.dismiss();
            }
        });
        advDialogBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_camera_preview, null, false);
        advSettingsDialog = new Dialog(this);
        advSettingsDialog.setContentView(advDialogBinding.getRoot());
        advSettingsDialog.setCancelable(false);
        customDialogSize(advSettingsDialog);
        /**        進階設定(圖像設定)    初始化      結束 */
        Log.e(TAG, "art face init");

    }
    /**確認應用程式權限*/
    private static boolean checkPermissions(Activity activity) {
        int write_permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read_permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int camera_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        int recording_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);

        if (write_permission != PackageManager.PERMISSION_GRANTED || read_permission != PackageManager.PERMISSION_GRANTED || camera_permission != PackageManager.PERMISSION_GRANTED
        || recording_permission!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_REQ, REQUEST_CODE_PERMISSION);
            return false;
        } else {
            return true;
        }
    }


    private boolean executeCmd(String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            Log.i(TAG, "executeCmd succeed: " + command);
            process.waitFor();
        } catch (Exception e) {
            Log.i(TAG, "executeCmd error:" + e.toString());
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**顯示臉部辨識的結果卡 裡面的FAKE是用來測試用的不要拿掉*/
    public void showResult(String userName, String imgUrl, int dialogDuration, FaceDetectService.FaceType faceType) {

        Log.e(TAG, "art face result");
        mViewModel.stopFaceDetect();
        //say("你好, " + userName.replace("107", "") + ", 請問今天也是找人嗎？");
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //say("好的，為您聯絡陳小姐");
                mViewModel.startFaceDetect();
            }
        }, 6000);

        if(mDialog!=null && mDialog.isShowing()){
            mDialog.dismiss();
        }


        AIViewModel.getInstance(this).setState(AIDialogState.IS_BOOKING);
        AIViewModel.getInstance(this).aiProcess("");

        //直接將其以視窗的方式彈出
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this, R.style.BDAlertDialog);
        View view;
        if(userName.equals("FAKE")){
            view = View.inflate(getBaseContext(), R.layout.dialog_tmp, null);
        }else{
            view = View.inflate(getBaseContext(), R.layout.dialog_result, null);
        }

        builder.setView(view);
        builder.setCancelable(true);

        final Runnable mDelayDismissResultDialogRunnable = new Runnable() {
            @Override
            public void run() {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                    mViewModel.closeDoor467();
                }
            }
        };
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface mDialog) {
                if (!mIpConfigured) {
                    mViewModel.startFaceDetect();
                }
                mViewModel.closeLED();
                handler.removeCallbacks(mDelayDismissResultDialogRunnable);
            }
        });

        if(userName.equals("FAKE")){
            TextView isLive = view.findViewById(R.id.isLive);//設置標題
            isLive.setText(userName);
        }else{

            TextView userNameTxt = view.findViewById(R.id.user_name);//設置標題
            userNameTxt.setText(userName);
            TextView accesssTxt = view.findViewById(R.id.welcome_txt);
            CircularImageView userAvatart = view.findViewById(R.id.ivAvatar);//輸入內容
            ImageView passornot=view.findViewById(R.id.passornot);
            //若臉辯成功則針對結果卡片進行微調
            if(faceType== FaceDetectService.FaceType.PASSFACE){
                passornot.setImageResource(R.drawable.ic_confirm);
                accesssTxt.setText("PASS");
            }
            else if(faceType== FaceDetectService.FaceType.BLACKLIST) {
                passornot.setImageResource(R.drawable.ic_refuse);


                accesssTxt.setText("DENIED");
                //撥放音樂
                if (mp == null) {
                    mp = MediaPlayer.create(MainActivity.this, R.raw.blacklist);
                    mp.start();
                    if (mp != null) {
                        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                mp.stop();
                                mp.reset();
                                mp.release();
                                mp = null;
                            }
                        });
                    }
                }

            }
            System.out.println("glideapp");
            //畫畫
            GlideApp.with(getApplicationContext()).load(imgUrl).placeholder(R.drawable.icon_default).diskCacheStrategy(DiskCacheStrategy.RESOURCE).override(182, 182).into(userAvatart);

        }

        //取消或确定按钮监听事件处理
        mDialog = builder.create();
        handler.postDelayed(mDelayDismissResultDialogRunnable, dialogDuration);
        mDialog.show();


    }
    /**顯示設定視窗*/
    public void showIPInputDialog() {
        //將現在的狀態設定為正在編輯
        mIpConfigured = true;
        //若還已經創建對話框且對話框已經顯示則關閉對話框(為了程序安全(不會創建到兩個對話框))
        if (inputDialog != null && inputDialog.isShowing()) {
            inputDialog.dismiss();
        }
        //設定viewmodel給 MainViewModel
        dialogBinding.setVm(mViewModel);
        //顯示對話框
        inputDialog.show();
    }

    public void showAdvSettingsDialog() {

        if (advSettingsDialog != null && advSettingsDialog.isShowing()) {
            advSettingsDialog.dismiss();
        }
        mAdvSettingsViewModel.create();
        advDialogBinding.setVm(mAdvSettingsViewModel);
        advSettingsDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIpConfigured = false;

        this.executeCmd("am broadcast -a com.android.internal.policy.impl.hideNavigationBar");
        // System.out.println("mdid"+inputDialog);
        mViewModel.setDialogViewitem(inputDialog);
        mViewModel.resume();
        //for test disable
        mViewModel.stopFaceDetect();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mIpConfigured = true;

        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }

        if (inputDialog != null && inputDialog.isShowing()) {
            inputDialog.dismiss();
        }

        this.executeCmd("am broadcast -a com.android.internal.policy.impl.showNavigationBar");
        mViewModel.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewModel.destroy();
        if(speechRecognizer!=null)
            speechRecognizer.destroy();
    }

    /**
     * get App versionName
     * @param context
     * @return
     */
    public String getVersionName(Context context){
        PackageManager packageManager=context.getPackageManager();
        PackageInfo packageInfo;
        String versionName="";
        try {
            packageInfo=packageManager.getPackageInfo(context.getPackageName(),0);
            versionName=packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public MainViewModel.OnActionListener mActionListener = new MainViewModel.OnActionListener() {

        @Override
        public void onShowIpInput() {
            showIPInputDialog();
        }

        @Override
        public void onShowAdvSettings() {
            showAdvSettingsDialog();
        }

        @Override
        public void onShowResult(String userName, String imgUrl, int dialogDuration, FaceDetectService.FaceType faceType) {
            showResult(userName,imgUrl,dialogDuration,faceType);
        }

        @Override
        public void onStrangerResult() {
            mViewModel.stopFaceDetect();
            //say("你好,請問有什麼可以為您服務的嗎？");

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mViewModel.startFaceDetect();
                }
            }, 4000);
        }

        @Override
        public void onShowErrorLed(final int dialogDuration) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!mIpConfigured) {
                        mViewModel.startFaceDetect();
                    }
                    mViewModel.closeLED();
                }
            },dialogDuration);
        }

        @Override
        public void onDismiss() {
            if (inputDialog != null && inputDialog.isShowing()) {
                inputDialog.dismiss();
            }
        }
        @Override
        public void onShowRemind(){

        }
    };

    /**客製化dialog大小 API16以後的作法*/
    private void customDialogSize(Dialog dialog) {

        DisplayMetrics displayMetrics = new DisplayMetrics();//取得螢幕寬高
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels; //width in pixels
        int displayHeight = displayMetrics.heightPixels; //height in pixels
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(); // Initialize a new window manager layout parameters

        // Copy the alert dialog window attributes to new layout parameter instance
        layoutParams.copyFrom(dialog.getWindow().getAttributes());

        int dialogWindowWidth;
        int dialogWindowHeight;

        // Set alert dialog width equal to screen width 60%
        dialogWindowWidth = (int) (displayWidth * 0.7f);
        // Set alert dialog height equal to screen height 40%
        dialogWindowHeight = (int) (displayHeight * 0.8f);


        // Set the width and height for the layout parameters
        // This will bet the width and height of alert dialog
        layoutParams.width = dialogWindowWidth;
        layoutParams.height = dialogWindowHeight;

        // Apply the newly created layout parameters to the alert dialog window
        dialog.getWindow().setAttributes(layoutParams);


    }

    private MainViewModel.OnResourceListener mResourceListener = new MainViewModel.OnResourceListener() {
        @Override
        public String getString(MainViewModel.StringStatus status) {
            int resId = 0;
            switch (status) {
                case FACE_NUMBER:
                    resId = R.string.face_number_is;
                    break;
                case NO_FACE:
                    resId = R.string.no_full_face;
                    break;
                case FACE_TOO_FAR:
                    resId = R.string.face_too_far;
                    break;
                case FACE_TOO_CLOSE:
                    resId = R.string.face_too_close;
                    break;
            }
            return resId != 0 ? getResources().getString(resId) : "";
        }
    };
}