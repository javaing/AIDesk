package com.aiunion.aidesk.model.net.controller;


import static com.aiunion.aidesk.model.net.NanoHTTPD.MIME_PLAINTEXT;

import com.aiunion.aidesk.model.MainModel;
import com.aiunion.aidesk.model.net.NanoHTTPD;
import com.aiunion.aidesk.model.net.dto.ResponesDTO;
import com.aiunion.aidesk.model.response.DoorStateResponse;
import com.google.gson.Gson;

public class DoorController extends BaseController{

    public NanoHTTPD.Response openDoor(NanoHTTPD.IHTTPSession session) {
        if (checkInternal(session) == 500) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT,"");
        }
        if (!session.getHeaders().containsKey(AUTHORIZATION_KEY) || !checkAuthorization(session.getHeaders().get(AUTHORIZATION_KEY))) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.FORBIDDEN, MIME_PLAINTEXT,"");
        }

        MainModel.getInstance().apiOpenDoor();

        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, MIME_PLAINTEXT,"");
    }

    public NanoHTTPD.Response closeDoor(NanoHTTPD.IHTTPSession session) {
        if (checkInternal(session) == 500) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT,"");
        }
        if (!session.getHeaders().containsKey(AUTHORIZATION_KEY) || !checkAuthorization(session.getHeaders().get(AUTHORIZATION_KEY))) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.FORBIDDEN, MIME_PLAINTEXT,"");
        }

        MainModel.getInstance().apiCloseDoor();

        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, MIME_PLAINTEXT,"");
    }

    public NanoHTTPD.Response getDoorState(NanoHTTPD.IHTTPSession session) {

        if (!session.getHeaders().containsKey(AUTHORIZATION_KEY) || !checkAuthorization(session.getHeaders().get(AUTHORIZATION_KEY))) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.FORBIDDEN, MIME_PLAINTEXT,"");
        }

        boolean isOpen = MainModel.getInstance().getDoorState();
        ResponesDTO res = new ResponesDTO("200","OK");
        DoorStateResponse state = new DoorStateResponse();
        state.setOnoff(isOpen);
        res.setResult(state);
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json;charset=UTF-8",
                new Gson().toJson(res));
    }
}
