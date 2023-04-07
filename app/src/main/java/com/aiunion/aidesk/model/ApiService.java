package com.aiunion.aidesk.model;


import com.aiunion.aidesk.ai.pojo.ChatRequest;
import com.aiunion.aidesk.ai.pojo.GPTResult;
import com.aiunion.aidesk.model.request.FaceRecognizeRequest;
import com.aiunion.aidesk.model.response.FaceRecognizeResponse;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/face/recognize/photoString")
    Observable<FaceRecognizeResponse[]> queryFaceId(@Body FaceRecognizeRequest fid);

    @POST("v1/chat/completions")
    Call<GPTResult> chatCompletions(@Body ChatRequest fid);
}
