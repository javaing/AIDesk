package com.aiunion.aidesk.model.response;

public class FaceRecognizeResponse {
    private int id;
    private String identity;
    private String photoUri = "";
    private String name;
    private float similarity;
    private int faceTypeId;

    public int getId() {
        return id;
    }

    public void setId(int bId) {
        this.id = bId;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public String getRelativePhotoUri() {
        return photoUri.startsWith("/",0) ? photoUri : ("/"+photoUri);
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public String getName() {
        return name!=null?name:"";
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getSimilarity() {
        return similarity;
    }

    public void setSimilarity(float similarity) {
        this.similarity = similarity;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public int getFaceTypeId() {
        return faceTypeId;
    }

    public void setFaceTypeId(int faceTypeId) {
        this.faceTypeId = faceTypeId;
    }
}
