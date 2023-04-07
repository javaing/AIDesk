package com.aiunion.aidesk.viewmodel;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public abstract class BaseViewModel extends AndroidViewModel {
    public final String TAG = this.getClass().getSimpleName();

    protected Context mContext;

    public BaseViewModel(@NonNull Application application) {
        super(application);
        mContext = application.getApplicationContext();
    }

    public void create() {}
    public void start() {}
    public void restart() {}
    public void resume() {}
    public void pause() {}
    public void stop() {}
    public void destroy() {}

}
