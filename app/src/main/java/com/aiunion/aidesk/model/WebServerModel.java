package com.aiunion.aidesk.model;



import com.aiunion.aidesk.model.net.NanoHTTPD;
import com.aiunion.aidesk.model.net.RequestMapping;
import com.aiunion.aidesk.model.net.controller.DoorController;

import java.util.HashMap;

public class WebServerModel {

    private static WebServerModel sInstance;
    private FaceServer faceServer;
    private enum Action {
        DOOR_OPEN,
        DOOR_CLOSE,
        DOOR_STATE,
        NONE};
    private static HashMap<RequestMapping,Action> mRequestMap;

    private WebServerModel() {
        initRequestMap();
    }

    public static WebServerModel getInstance() {
        synchronized (WebServerModel.class) {
            if (sInstance == null) { sInstance = new WebServerModel(); }
        }
        return sInstance;
    }

    public void startServer() throws Exception {
        faceServer = new FaceServer();
        faceServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    public void stopServer() {
        if (faceServer != null) {
            faceServer.stop();
        }
    }

    private void initRequestMap() {
        mRequestMap = new HashMap<>();
        mRequestMap.put(new RequestMapping(NanoHTTPD.Method.POST,"/api/door/open"), Action.DOOR_OPEN);
        mRequestMap.put(new RequestMapping(NanoHTTPD.Method.POST,"/api/door/close"), Action.DOOR_CLOSE);
        mRequestMap.put(new RequestMapping(NanoHTTPD.Method.GET,"/api/door"), Action.DOOR_STATE);
    }


    private static class FaceServer extends NanoHTTPD {

        private static final String TAG = "asd";

        public FaceServer() {
            super(8081);
        }

        @Override
        public NanoHTTPD.Response serve(IHTTPSession session) {

            Action action = getAction(session.getMethod(),session.getUri());

            switch (action) {
                case DOOR_OPEN:
                    return new DoorController().openDoor(session);
                case DOOR_CLOSE:
                    return new DoorController().closeDoor(session);
                case DOOR_STATE:
                    return new DoorController().getDoorState(session);
            }
            return newFixedLengthResponse(Response.Status.FORBIDDEN, MIME_PLAINTEXT,"");
        }
    }

    private static Action getAction(final NanoHTTPD.Method method, final String uri) {
        for (RequestMapping request : mRequestMap.keySet()) {
            if (method.equals(request.getMethod()) && uri.startsWith(request.getUri())) {
                return mRequestMap.get(request);
            }
        }
        return Action.NONE;
    }
}
