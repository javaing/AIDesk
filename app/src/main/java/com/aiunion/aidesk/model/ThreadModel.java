package com.aiunion.aidesk.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.aiunion.aidesk.ai.AIDialogState;
import com.aiunion.aidesk.ai.AIViewModel;
import com.aiunion.aidesk.main.MyApplication;
import com.aiunion.aidesk.model.request.FaceRecognizeRequest;
import com.aiunion.aidesk.model.response.FaceConfigResponse;
import com.aiunion.aidesk.model.response.FaceRecognizeResponse;
import com.aiunion.aidesk.viewmodel.IpconfParams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**Api執行序*/
public class ThreadModel {
    private static Thread sThread;
    /**判斷單筆照片及資料是否已經成功抵達 STRING放的是?#後面的單筆URLID(不重複ID),List<Boolean> 假設有4筆有兩筆成功會出現[true,false,false,false]*/
    private static ConcurrentHashMap<String, List<Boolean>> mMap;
    /**為了統一管理執行續所使用的 線程池*/
    private static ExecutorService executor = Executors.newCachedThreadPool();
    //Token
    private static final String SERVER_GIVE_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiZmFjZWFpIl0sInNjb3BlIjpbImFwaS1zZXJ2aWNlIl0sImV4cCI6MTkyMTE1MzI1OCwiYXV0aG9yaXRpZXMiOlsiYWl1bmlvbiJdLCJqdGkiOiI3ODI3YTBkYi0zMGQ3LTRhODItYjQyYy0yMTQ0NTMyZWRlNDEiLCJjbGllbnRfaWQiOiJhcGktY2xpZW50In0.mE8WnaGzVuWhS5LfT0ajQcBr_JP2TUOVfhch-5dJ6mA";
    private static final long ALIVETIME = 50000;
    private static ThreadModel sInstance;
    /**最後一次辨識到的人臉ID*/
    private static AtomicInteger slastFaceId = new AtomicInteger(0);
    /**最後一次辨識到的時間(避免重複開門)*/
    private static AtomicLong slastDetectTime = new AtomicLong(0L);
    /**去除重複資料的時間(需要從 API v2/setting取得)*/
    private static AtomicLong sCheckTime = new AtomicLong(10000L);
    /**去除重複資料的參數更新頻率*/
    private static AtomicLong sConfigChecktime = new AtomicLong(60000L);
    /**最後一次偵測去重資料的時間*/
    private static AtomicLong sLastconfigChecktime = new AtomicLong(0L);
   
    private static List<IpconfParams> mIplist = new ArrayList<>();
    /**從VIEWMODLE取得的主機名單*/
    public void setmIplist(List<IpconfParams> nIplist) {
        this.mIplist = nIplist;
        System.out.println("mip" + mIplist);
    }

    ////////////////////////////////////////////////////////////////////////
    public static ThreadModel getInstance() {
        synchronized (ThreadModel.class) {
            if (sInstance == null) {
                sInstance = new ThreadModel();
            }
        }
        return sInstance;
    }

    //////////////////////////////////////////////////////////
    static {
        mMap = new ConcurrentHashMap<>();
        timeThread();
    }

    /**偵測照片是否過期及管理執行續的執行續*/
    private static void timeThread() {
        if (sThread == null || !sThread.isAlive()) {
            sThread = null;
            sThread = new Thread(() -> {
                while (true) {
                    if (mMap.size() > 0) {
                        long t1 = System.currentTimeMillis();
                        ////////////////////////////////////////////////
                        for (String key : mMap.keySet()) {
                            try {
                                if ((t1 - getTimeByString(key)) > ALIVETIME) {
                                    List<Boolean> sList = mMap.remove(key);
                                    sList.clear();
                                    sList = null;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        ////////////////////////////////////////////////
                        if (mMap.size() == 0) {
                            mMap = null;
                            mMap = new ConcurrentHashMap<>();
                        }
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            sThread.start();
      
        }
    }

    /**針對每一筆資料賦予ID*/
    public static String getKey(int size) {
        int sSize = size < 1 ? 1 : size;
        String key = String.format("%sa%sb%sc%sd%s", System.currentTimeMillis(), getRandomIntString(3),
                getRandomIntString(3), getRandomIntString(3), sSize);
        mMap.put(key, new ArrayList<>());
        return key;
    }

    /**管理是否已經取得資料的function*/
    public static boolean getRes(String key, boolean ok, Integer faceId) {
        if (key == null || key.trim().length() == 0) {
            return false;
        }
        final String sKey = "" + key;
        final boolean isOk = ok;
        boolean res = false;
        try {
            if (mMap.containsKey(sKey)) {
                long t1 = System.currentTimeMillis();
                long t2 = getTimeByString(sKey);
                if ((t1 - t2) <= ALIVETIME) {
                    System.out.println("is t1-t2<=ALIVETIME?" + (t1 - t2));
                    int size = getSizeByString(sKey);
                    List<Boolean> sList = mMap.get(sKey);
                    if (isOk && !sList.contains(true)) {
                        synchronized (slastFaceId) {
                            synchronized (slastDetectTime) {
                                System.out.println("192......." + (t1 - t2));
                                System.out.println("192... faceid " + "\t" + faceId + "\t" + "slastFaceId" + slastFaceId.get());
                                if (faceId != null && faceId > 0
                                        && (faceId != slastFaceId.get() || (t1 - slastDetectTime.get()) > sCheckTime.get())) {
                                    slastFaceId.set(faceId);
                                    slastDetectTime.set(t1);
                                    System.out.println("192..... inside faceid " + "\t" + faceId + "\t" + "slastFaceId" + slastFaceId.get());
                                    //System.out.println("192......."+(t1 - slastDetectTime.get()));
                                    res = true;
                                }
                            }
                        }
                    }
                    sList.add(isOk);
                    for (boolean bl : sList) {
                        System.out.println("this is boolean" + bl);
                    }
                    if (sList.size() == size) {
                        sList = mMap.remove(sKey);
                        sList.clear();
                        sList = null;
                        if (mMap.size() == 0) {
                            mMap = null;
                            mMap = new ConcurrentHashMap<>();
                        }
                    }
                } else {
                    List<Boolean> sList = mMap.remove(sKey);
                    sList.clear();
                    sList = null;
                    if (mMap.size() == 0) {
                        mMap = null;
                        mMap = new ConcurrentHashMap<>();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /**隨機數字來預防id重複*/
    private static String getRandomIntString(int len) {
        String res = "";
        if (len < 1) {
            return res;
        }
        final int length = len;
        try {
            int lens = 1;
            for (int i = 0; i < length; i++) {
                lens = lens * 10;
            }
            int nextint = ThreadLocalRandom.current().nextInt(0, lens);
            res = "" + nextint;
            while (res.length() < len) {
                res = "0" + res;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    //字串處理
    private static Long getTimeByString(String key) {
        Long res = null;
        if (key == null || key.trim().length() == 0) {
            return res;
        }
        final String str = "" + key;
        try {
            res = Long.valueOf(str.substring(0, str.indexOf("a")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    //字串處理
    private static Integer getSizeByString(String key) {
        Integer res = null;
        if (key == null || key.trim().length() == 0) {
            return res;
        }
        final String str = "" + key;
        try {
            res = Integer.valueOf(str.substring(str.indexOf("d") + 1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /**當掃到人臉會啟動並根據所設定的ip數量設定執行續數量*/
    public static void threadmain(String photoString) {
        if (mIplist != null) {
            new Thread(() -> {
                postCaptureConfig(mIplist.get(0).getServerIP());
            }).start();
        }
        //key生成需要代入主機數量
        final String key2 = getKey(mIplist.size());
        System.out.println("key2size" + mIplist.size());
        for (IpconfParams mApi : mIplist) {
            FaceRecognizeRequest fid = new FaceRecognizeRequest(photoString, mApi.getThreshoId(), mApi.getDeviceId(), mApi.getChannelID());
            String jsonStr = fid.toJson();
            Thread newThread = new Thread(() -> {
                postCaptureData(mApi.getServerIP().startsWith("http") ? mApi.getServerIP() : "http://" + mApi.getServerIP() + "/api/face/recognize/photoString?#" + key2, jsonStr, mApi.getServerIP());
            });
            ThreadModel.executor.submit(newThread);
        }
    }

    /**抓取設定檔的api baseurl*/
    private static void postCaptureConfig(String baseUrl) {
        /**取得設定檔 開始 */
        /**判別是否繼續往下執行 */
        long t1 = System.currentTimeMillis();
        if ((t1 - sLastconfigChecktime.get()) < sConfigChecktime.get()) {
            return;
        }

        baseUrl = baseUrl.startsWith("http") ? baseUrl : "http://" + baseUrl;
        /**OKHttpClient Request 設定 */
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder() //
                .connectTimeout(40, TimeUnit.SECONDS) //
                .writeTimeout(40, TimeUnit.SECONDS) //
                .readTimeout(60, TimeUnit.SECONDS) //
                .retryOnConnectionFailure(true) //
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        // try the request
                        Response response = chain.proceed(request);
                        int tryCount = 0;
                        while ((response == null || !response.isSuccessful()) && tryCount < 2) {
                            System.out.println("intercept, Request is not successful - " + tryCount);
                            tryCount++;
                            // retry the request
                            if (response != null) {
                                response.close();
                            }
                            try {
                                Thread.sleep(7000); // force wait the network thread for 5 seconds
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            response = sendReqeust(chain, request);
                        }
                        // otherwise just pass the original response on
                        return response;
                    }

                    private Response sendReqeust(Chain chain, Request request) {
                        try {
                            Response response = chain.proceed(request);
                            if (!response.isSuccessful()) {
                                response.close();
                                return null;
                            } else {
                                return response;
                            }
                        } catch (Exception e) {
                            return null;
                        }
                    }
                })
                .addInterceptor(httpLoggingInterceptor)
                .build(); // MediaType.get("application/json; charset=utf-8");

                /**OKHttpClient Request 設定  結束 */
        System.out.println("baseurl" + baseUrl);
        /**發送請求準備 */
        Request request = new Request
                .Builder()
                .url(baseUrl + "/api/v2/settings?pageSize=1000")
                .header("cache-control", "no-cache")
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Authorization", "Bearer " + SERVER_GIVE_TOKEN)
                .get()
                .build();
        /**取得回復 */        
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            System.out.println("my apiresonselist" + responseBody);
            Gson gson = new Gson();
            Type listType = new TypeToken<FaceConfigResponse>() {
            }.getType();
            FaceConfigResponse faceConfigResponsecr = gson.fromJson(responseBody, listType);
            for (FaceConfigResponse.ConfigResult fc : faceConfigResponsecr.getResult()) {
                System.out.println("fc.getVar" + fc.getVar());
                if (fc.getVar().equals("DE_DUPLICATE_FACE_TIME")) {
                    System.out.println("fc.getVar" + fc.getVar());
                    sCheckTime.set(Long.parseLong(fc.getVal()) * 2000);
                    System.out.println("this flivetime2" + sCheckTime.get());
                }
            }
            sLastconfigChecktime.set(t1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**發送圖片並處理回傳訊息的方法*/
    private static void postCaptureData(String url, String jsonStr, String baseUrl) {
        baseUrl = baseUrl.startsWith("http") ? baseUrl : "http://" + baseUrl;
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        /**OKhttp設定 */
        OkHttpClient client = new OkHttpClient.Builder() //
                .connectTimeout(40, TimeUnit.SECONDS) //
                .writeTimeout(40, TimeUnit.SECONDS) //
                .readTimeout(60, TimeUnit.SECONDS) //
                .retryOnConnectionFailure(true) //
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        // try the request
                        Response response = chain.proceed(request);
                        int tryCount = 0;
                        while ((response == null || !response.isSuccessful()) && tryCount < 2) {
                            System.out.println("intercept, Request is not successful - " + tryCount);
                            tryCount++;
                            // retry the request
                            if (response != null) {
                                response.close();
                            }
                            try {
                                Thread.sleep(7000); // force wait the network thread for 5 seconds
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            response = sendReqeust(chain, request);
                        }
                        // otherwise just pass the original response on
                        return response;
                    }

                    private Response sendReqeust(Chain chain, Request request) {
                        try {
                            Response response = chain.proceed(request);
                            if (!response.isSuccessful()) {
                                response.close();
                                return null;
                            } else {
                                return response;
                            }
                        } catch (Exception e) {
                            return null;
                        }
                    }
                })
                .addInterceptor(httpLoggingInterceptor)
                .build(); // MediaType.get("application/json; charset=utf-8");
        /**設定請求body*/
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), jsonStr);
        /**準備發送請求 */
        Request request = new Request
                .Builder()
                .url(url)
                .header("cache-control", "no-cache")
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Authorization", "Bearer " + SERVER_GIVE_TOKEN)
                .post(body)
                .build();

        String key = url.split("\\?#")[1];
        System.out.println("baseurl" + "\t" + baseUrl + "\t" + "Thekey" + "\t" + key);
        /**對得到的response進行臉部判別 */
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            // ... do something with response
            ////////////////////////////////////////////////////////////////////////////////////
            Gson gson = new Gson();
            Type listType = new TypeToken<List<FaceRecognizeResponse>>() {
            }.getType();
            List<FaceRecognizeResponse> myApiResponseList = gson.fromJson(responseBody, listType);
            Log.e("", "art responseBody=" + responseBody);
            //////////////////////////////對回復資料進行處理/////////////////////////////////////////////////
            if (myApiResponseList.size() > 0) {
                FaceRecognizeResponse face = myApiResponseList.get(0);
                FaceDetectService mFaceDetectService = MainModel.getInstance().getmFaceDetectService();
                FaceDetectService.FaceType faceType = mFaceDetectService.getFaceType(face);
                String serverIp = baseUrl;
                String imgUrl = face.getPhotoUri().contains("http://") ? face.getPhotoUri() : (serverIp + face.getRelativePhotoUri());
                System.out.println("FACETYPE" + "\t" + url + "\t" + faceType);
                boolean todo = false;
                /**如果這筆不是陌生人 */
                if (face != null && face.getFaceTypeId() != -1 && face.getId() > 0) {
                    todo = getRes(key, true, face.getId());
                } else {
                    todo = getRes(key, false, face.getId());
                }
                /**若得到解果且這個人在去重時間內沒被判定到繼續做是否通關的判斷 */
                if (todo) {
                    FaceDetectService.FaceType tempfaceType = FaceDetectService.FaceType.STRANGERS;
                    switch (tempfaceType) {
                        /**根據人臉判斷結果做出不同的應對*/
                        case PASSFACE:
                            if (MainModel.getInstance().getmFaceDetectListener() != null) {
                                Handler handler = new Handler(Looper.getMainLooper()) {
                                    @Override
                                    public void handleMessage(Message message) {
                                        MainModel.getInstance().getmFaceDetectListener().onPassFaceResult(face.getName(), imgUrl, faceType);
                                    }
                                };
                                Message message = handler.obtainMessage();
                                handler.sendMessage(message);
                                MainModel.getInstance().openDoor467();
                            }

                            System.out.println("art FACETYPE" + "\t" + url + "\t" + "PASSFACE" + "\t" + todo);

                            AIViewModel.getInstance(MyApplication.getInstance()).setState(AIDialogState.IS_BOOKING);
                            AIViewModel.getInstance(MyApplication.getInstance()).aiProcess("");


                            break;
                        case STRANGERS:
                            if (MainModel.getInstance().getmFaceDetectListener() != null)
                                MainModel.getInstance().getmFaceDetectListener().onStrangerResult();
                            System.out.println("art FACETYPE" + "\t" + url + "\t" + "STRANGER" + "\t" + todo);

                            AIViewModel.getInstance(MyApplication.getInstance()).setState(AIDialogState.IS_GUEST);
                            AIViewModel.getInstance(MyApplication.getInstance()).aiProcess("");

                            break;
                        case BLACKLIST:
                            System.out.println("art FACETYPE" + "\t" + url + "\t" + "BLACKLIST" + "\t" + todo);
                            Handler handler = new Handler(Looper.getMainLooper()) {
                                @Override
                                public void handleMessage(Message message) {
                                    MainModel.getInstance().getmFaceDetectListener().onPassFaceResult(face.getName(), imgUrl, faceType);
                                }
                            };
                            Message message = handler.obtainMessage();
                            handler.sendMessage(message);
                            if (MainModel.getInstance().getmFaceDetectListener() != null)
                                MainModel.getInstance().getmFaceDetectListener().onError();
                            break;
                    }
                } else {
                    if (MainModel.getInstance().getmFaceDetectListener() != null)
                        MainModel.getInstance().getmFaceDetectListener().onError();
                }
            } else {
                if (MainModel.getInstance().getmFaceDetectListener() != null)
                    MainModel.getInstance().getmFaceDetectListener().onError();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //`System.out.println(getRes(key, false));
    }

}
