package com.aiunion.aidesk.ai.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import javax.annotation.Generated;
import java.util.List;

@Generated("jsonschema2pojo")
public class GPTResult {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("object")
    @Expose
    private String object;
    @SerializedName("created")
    @Expose
    private Integer created;
    @SerializedName("model")
    @Expose
    private String model;
    @SerializedName("usage")
    @Expose
    private GPTResultUsage usage;
    @SerializedName("choices")
    @Expose
    private List<GPTResultChoice> choices;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public Integer getCreated() {
        return created;
    }

    public void setCreated(Integer created) {
        this.created = created;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public GPTResultUsage getUsage() {
        return usage;
    }

    public void setUsage(GPTResultUsage usage) {
        this.usage = usage;
    }

    public List<GPTResultChoice> getChoices() {
        return choices;
    }

    public void setChoices(List<GPTResultChoice> choices) {
        this.choices = choices;
    }

}
