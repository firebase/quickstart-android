package com.google.firebase.quickstart.perfmon.java;

import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.google.firebase.quickstart.perfmon.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final String DEFAULT_CONTENT_FILE = "default_content.txt";
    private static final String CONTENT_FILE = "content.txt";
    private static final String IMAGE_URL =
            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png";

    private ImageView mHeader;
    private TextView mContent;
    private Trace mTrace;

    private String STARTUP_TRACE_NAME = "startup_trace";
    private String REQUESTS_COUNTER_NAME = "requests sent";
    private String FILE_SIZE_COUNTER_NAME = "file size";
    private CountDownLatch mNumStartupTasks = new CountDownLatch(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHeader = findViewById(R.id.headerIcon);
        mContent = findViewById(R.id.textViewContent);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // write 40 chars of random text to file
                File contentFile = new File(MainActivity.this.getFilesDir(), CONTENT_FILE);

                writeStringToFile(contentFile.getAbsolutePath(), getRandomString(40) + "\n")
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()) {
                                    Log.e(TAG, "Unable to write to file", task.getException());
                                    return;
                                }

                                loadFileFromDisk();
                            }
                        });
                }
        });

        // Begin tracing app startup tasks.
        mTrace = FirebasePerformance.getInstance().newTrace(STARTUP_TRACE_NAME);
        Log.d(TAG, "Starting trace");
        mTrace.start();
        loadImageFromWeb();
        // Increment the counter of number of requests sent in the trace.
        Log.d(TAG, "Incrementing number of requests counter in trace");
        mTrace.incrementMetric(REQUESTS_COUNTER_NAME, 1);
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

    private Task<Void> writeStringToFile(final String filename, final String content) {
        return Tasks.call(
                AsyncTask.THREAD_POOL_EXECUTOR,
                new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        FileOutputStream fos = new FileOutputStream(filename, true);
                        fos.write(content.getBytes());
                        fos.close();
                        return null;
                    }
                });
    }

    private Task<String> loadStringFromFile() {
        return Tasks.call(
                AsyncTask.THREAD_POOL_EXECUTOR,
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        File contentFile = new File(getFilesDir(), CONTENT_FILE);
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
                        return new String(content);
                    }
                });
    }

    private void loadFileFromDisk() {
        loadStringFromFile()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Couldn't read text file.");
                            Toast.makeText(
                                    MainActivity.this, getString(R.string.text_read_error), Toast.LENGTH_LONG)
                                    .show();
                            return;
                        }

                        String fileContent = task.getResult();
                        mContent.setText(task.getResult());
                        // Increment a counter with the file size that was read.
                        Log.d(TAG, "Incrementing file size counter in trace");
                        mTrace.incrementMetric(FILE_SIZE_COUNTER_NAME, fileContent.getBytes().length);
                        mNumStartupTasks.countDown();
                    }
                });
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
}
