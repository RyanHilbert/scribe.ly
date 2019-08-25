package ly.scribe;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Range;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

//https://developer.android.com/guide/topics/media/mediarecorder
public class AudioRecordTest extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecordTest";
    private static Context context;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileNameWithPath = null;
    private static String fileName =  null;

    private RecordButton recordButton = null;
    private MediaRecorder recorder = null;

    private PlayButton   playButton = null;
    private MediaPlayer   player = null;

    private UploadButton uploadButton = null;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void onUpload(boolean start) {
        if (start) {
            startUploading();
        } /*else {
            stopPlaying();
        }*/
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileNameWithPath);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    private void startUploading() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    sendReceiveRequest(fileName, getAppContext());
                    //Your code goes here
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }


    private void startRecording() {
        //https://github.com/Parrot-Developers/Samples/blob/master/Android/SDKSample/app/src/main/java/com/parrot/sdksample/audio/AudioRecorder.java
        //TODO: noise suppression

        fileName = "audiorecordtest" + System.currentTimeMillis() + ".mp4";
        fileNameWithPath = getFilesDir().getAbsolutePath();
        fileNameWithPath += "/" + fileName;


        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(fileNameWithPath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    class RecordButton extends android.support.v7.widget.AppCompatButton {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    class PlayButton extends android.support.v7.widget.AppCompatButton {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }

    class UploadButton extends android.support.v7.widget.AppCompatButton {
        boolean mStartUpload = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onUpload(mStartUpload);
                if (mStartUpload) {
                    setText("Uploading...");
                } else {
                    setText("Start Uploading");
                }
                mStartUpload = !mStartUpload;
            }
        };

        public UploadButton(Context ctx) {
            super(ctx);
            setText("Start Uploading");
            setOnClickListener(clicker);
        }
    }

    public static Context getAppContext() {
        return AudioRecordTest.context;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        AudioRecordTest.context = getApplicationContext();

        // Record to the external cache directory for visibility
        //fileNameWithPath = getExternalCacheDir().getAbsolutePath();

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        LinearLayout ll = new LinearLayout(this);
        recordButton = new RecordButton(this);
        ll.addView(recordButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        playButton = new PlayButton(this);
        ll.addView(playButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        uploadButton = new UploadButton(this);
        ll.addView(uploadButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));

        setContentView(ll);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }
    }

    private Boolean sendReceiveRequest(String fileName, Context context) {
        try {
            final String finalFileName = fileName;
            final Context finalContext = context;
            boolean writeComplete = writeAudioToAWS(context, fileName);
            if (writeComplete) {
                Thread getProcessedResultThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try  {
                            getParsedAudio(finalContext, finalFileName);
                            //Your code goes here
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                getProcessedResultThread.start();
            }

        } catch(IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private String getParsedAudio (Context context, String fileName) throws IOException, InterruptedException {

        String parsedAudio = null;
        int napsTaken = 0;
        final int NAP_LENGTH = 5000;
        final int MAX_NAPS = 50;

        while(napsTaken < MAX_NAPS) {
            URL responseUrl = new URL("http://scribely.s3.amazonaws.com/" + fileName + ".json");
            HttpURLConnection responseConn = (HttpURLConnection) responseUrl.openConnection();
            responseConn.setRequestMethod("GET");
            responseConn.setRequestProperty("x-amz-acl", "public-read-write");
            if (responseConn.getResponseCode() == 404) {
                responseConn.disconnect(); // probably should do this
                Thread.sleep(NAP_LENGTH);
                napsTaken += 1;
            } else if (responseConn.getResponseCode() == 200) {
                Log.e("AudieRecordTest", "data has been parsed finally!");
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(responseConn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                parsedAudio = response.toString();
                in.close();
                break;
            }
        }
        return parsedAudio;

    }
    private boolean writeAudioToAWS (Context context, String fileName) throws IOException {
        HttpURLConnection connection;
        DataOutputStream request = null;

        FileInputStream inputStream =  context.openFileInput(fileName);
        if (inputStream == null) {
            Log.e("ApiCall","file was null");
            return false;
        }

        byte[] binaryData = IOUtils.toByteArray(inputStream);

        URL url = null;

        url = new URL("http://scribely.s3.amazonaws.com/" + fileName);
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        connection.setRequestProperty("x-amz-acl", "public-read-write");
        connection.setRequestMethod("PUT");
        request = new DataOutputStream(connection.getOutputStream());

        request.write(binaryData);
        request.flush();
        request.close();
        inputStream.close();

        return connection.getResponseCode() == 200;
    }

    private String parseAudioResponse(String parsedAudio) {
        try {
            boolean hasMultipleSpeakers = false;
            HashMap<String, ArrayList<Range>> speakerToSpeechRange = null;

            JSONObject returnedJson = new JSONObject(parsedAudio);
            JSONObject results = returnedJson.getJSONObject("results");
            JSONArray transcriptsArr = results.getJSONArray("transcripts");
            if (transcriptsArr.length() == 0) return null;
            JSONObject transcriptObj = transcriptsArr.getJSONObject(0);

            String transcript = transcriptObj.getString("transcript");

            JSONObject speakerLabels =  results.getJSONObject("speaker_labels");
            Integer speakerCount = speakerLabels.getInt("speakers");
            JSONArray speakingSegments = speakerLabels.getJSONArray("segments");

            if (speakerCount > 1) {
                hasMultipleSpeakers = true;
                speakerToSpeechRange = new HashMap<>();
            }
            if (speakingSegments.length() == 0) return null;
            for (int i = 0; i< speakingSegments.length(); i++) {
                JSONObject segment =  speakingSegments.getJSONObject(i);
                Double startTime = segment.getDouble("start_time");
                Double endTime = segment.getDouble("end_time");
                String speakerLbl = segment.getString("speaker_label");
                Range<Double> speakingRange = new Range<>(startTime, endTime);

                //speakerToSpeechRange.put(speakerLbl

            }





        } catch (JSONException e) {
            e.printStackTrace();
        }//
                return null;
    }
}