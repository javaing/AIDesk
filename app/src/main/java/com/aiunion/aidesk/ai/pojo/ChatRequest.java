package com.aiunion.aidesk.ai.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import javax.annotation.Generated;

@Generated("jsonschema2pojo")
public class ChatRequest {

    @SerializedName("model")
    @Expose
    private String model;
    @SerializedName("messages")
    @Expose
    private List<GPTSendMessage> GPTSendMessages;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<GPTSendMessage> getMessages() {
        return GPTSendMessages;
    }

    public void setMessages(List<GPTSendMessage> GPTSendMessages) {
        this.GPTSendMessages = GPTSendMessages;
    }

}





