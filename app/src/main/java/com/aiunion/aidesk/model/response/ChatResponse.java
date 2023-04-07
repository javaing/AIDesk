package com.aiunion.aidesk.model.response;


import java.util.ArrayList;

// import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
// import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
/* ObjectMapper om = new ObjectMapper();
Root root = om.readValue(myJsonString, Root.class); */
class Choice{
    public int index;
    public Message message;
    public String finish_reason;
}

class Message{
    public String role;
    public String content;
}

public class ChatResponse{
    public String id;
    public String object;
    public int created;
    public ArrayList<Choice> choices;
    public Usage usage;
}

class Usage{
    public int prompt_tokens;
    public int completion_tokens;
    public int total_tokens;
}


