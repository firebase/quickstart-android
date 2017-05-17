package com.google.firebase.quickstart.perfmon;

import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ImageView mHeader;
    private TextView mContent;
    private Trace mTrace;
    private String STARTUP_TRACE_NAME = "startup_trace";
    private String REQUESTS_COUNTER_NAME = "requests sent";
    private String FILE_SIZE_COUNTER_NAME = "file size";
    private CountDownLatch mNumStartupTasks = new CountDownLatch(2);
    private final String DEFAULT_CONTENT_FILE = "default_content.txt";
    private final String CONTENT_FILE = "content.txt";
    private final String IMAGE_URL =
            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHeader = (ImageView) findViewById(R.id.imageView);
        mContent = (TextView) findViewById(R.id.textView);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // write 40 chars of random text to file
                File contentFile = new File(MainActivity.this.getFilesDir(), CONTENT_FILE);
                new WriteToFileTask(contentFile.getAbsolutePath()).execute(getRandomString(40) + "\n");
            }
        });

        // Begin tracing app startup tasks.
        mTrace = FirebasePerformance.getInstance().newTrace(STARTUP_TRACE_NAME);
        Log.d(TAG, "Starting trace");
        mTrace.start();
        loadImageFromWeb();
        // Increment the counter of number of requests sent in the trace.
        Log.d(TAG, "Incrementing number of requests counter in trace");
        mTrace.incrementCounter(REQUESTS_COUNTER_NAME);
        loadFileFromDisk();
        // Wait for app startup tasks to complete asynchronously and stop the trace.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mNumStartupTasks.await();
                } catch (InterruptedException e) {
                    Log.e(TAG, "Unable to wait for startup task completion.");
                } finally {
                    Log.d(TAG, "Stopping trace");
                    mTrace.stop();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Trace completed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void loadImageFromWeb() {
        Glide.with(this).
                load(IMAGE_URL)
                .placeholder(new ColorDrawable(ContextCompat.getColor(this, R.color.colorAccent)))
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(
                            Exception e, String model, Target<GlideDrawable> target,
                            boolean isFirstResource) {
                        mNumStartupTasks.countDown(); // Signal end of image load task.
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(
                            GlideDrawable resource, String model, Target<GlideDrawable> target,
                            boolean isFromMemoryCache, boolean isFirstResource) {
                        mNumStartupTasks.countDown(); // Signal end of image load task.
                        return false;
                    }
                }).into(mHeader);
    }

    private void loadFileFromDisk() {
        new AsyncTask<Void, Void, Boolean>(){
            private String fileContent;

            @Override
            protected Boolean doInBackground(Void... params) {
                File contentFile = new File(MainActivity.this.getFilesDir(), CONTENT_FILE);
                try {
                    if (contentFile.createNewFile()) {
                        // Content file exist did not exist in internal storage and new file was created.
                        // Copy in the default content.
                        InputStream is;
                        is = getAssets().open(DEFAULT_CONTENT_FILE);
                        int size = is.available();
                        byte[] buffer = new byte[size];
                        is.read(buffer);
                        is.close();
                        FileOutputStream fos = new FileOutputStream(contentFile);
                        fos.write(buffer);
                        fos.close();
                    }
                    FileInputStream fis = new FileInputStream(contentFile);
                    byte[] content = new byte[(int) contentFile.length()];
                    fis.read(content);
                    fileContent = new String(content);
                    return true;

                } catch (IOException e) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (!result) {
                    Log.e(TAG, "Couldn't read text file.");
                    Toast.makeText(MainActivity.this, getString(R.string.text_read_error),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                mContent.setText(fileContent);
                // Increment a counter with the file size that was read.
                Log.d(TAG, "Incrementing file size counter in trace");
                mTrace.incrementCounter(FILE_SIZE_COUNTER_NAME, fileContent.getBytes().length);
                mNumStartupTasks.countDown();
            }
        }.execute();
    }

    private String getRandomString(int length) {
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }

    private class WriteToFileTask extends AsyncTask<String, Void, Void> {
        private String filename;

        WriteToFileTask(String filename) {
            this.filename = filename;
        }

        @Override
        protected Void doInBackground(String... params) {
            String content = params[0];
            try (FileOutputStream fos = new FileOutputStream(filename, true)){
                fos.write(content.getBytes());
            } catch (IOException e) {
                Log.e(TAG, "Unable to write to file: " + filename);
                Log.e(TAG, Log.getStackTraceString(e));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loadFileFromDisk();
        }
    }
}
