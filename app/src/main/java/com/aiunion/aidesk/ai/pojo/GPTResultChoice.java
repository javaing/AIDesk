package com.aiunion.aidesk.ai.pojo;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class GPTResultChoice {

    @SerializedName("message")
    @Expose
    private GPTResultMessage message;
    @SerializedName("finish_reason")
    @Expose
    private String finishReason;
    @SerializedName("index")
    @Expose
    private Integer index;

    public GPTResultMessage getMessage() {
        return message;
    }

    public void setMessage(GPTResultMessage message) {
        this.message = message;
    }

    public String getFinishReason() {
        return finishReason;
    }

    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

}
