package ly.scribe;

import android.content.Context;

/**
 * Created by melody on 2019-08-24.
 */

public class UploadAudioDto {
    private String filename;
    private Context appContext;

    public UploadAudioDto(String filename, Context appContext) {
        this.filename = filename;
        this.appContext = appContext;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Context getAppContext() {
        return appContext;
    }

    public void setAppContext(Context appContext) {
        this.appContext = appContext;
    }

}
