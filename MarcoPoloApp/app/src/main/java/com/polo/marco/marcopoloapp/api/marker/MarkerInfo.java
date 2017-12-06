package com.polo.marco.marcopoloapp.api.marker;

import android.content.Context;

/**
 * Created by Chase on 12/5/2017.
 */

public class MarkerInfo {
    public Context context;
    public String imgUrl;

    public MarkerInfo(Context context, String imgUrl) {
        this.context = context;
        this.imgUrl = imgUrl;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
