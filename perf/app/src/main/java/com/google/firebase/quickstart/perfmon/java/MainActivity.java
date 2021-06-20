package com.google.firebase.quickstart.perfmon.java;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.google.firebase.quickstart.perfmon.R;
import com.google.firebase.quickstart.perfmon.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final String DEFAULT_CONTENT_FILE = "default_content.txt";
    private static final String CONTENT_FILE = "content.txt";
    private static final String IMAGE_URL =
            "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png";

    private Trace mTrace;

    private String STARTUP_TRACE_NAME = "startup_trace";
    private String REQUESTS_COUNTER_NAME = "requests sent";
    private String FILE_SIZE_COUNTER_NAME = "file size";
    private CountDownLatch mNumStartupTasks = new CountDownLatch(2);

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.button.setOnClickListener(new View.OnClickListener() {
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
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        mNumStartupTasks.countDown();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        mNumStartupTasks.countDown();
                        return false;
                    }
                }).into(binding.headerIcon);
    }

    private Task<Void> writeStringToFile(final String filename, final String content) {
        final TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FileOutputStream fos = new FileOutputStream(filename, true);
                    fos.write(content.getBytes());
                    fos.close();
                    taskCompletionSource.setResult(null);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        return taskCompletionSource.getTask();
    }

    private Task<String> loadStringFromFile() {
        final TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
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
                    taskCompletionSource.setResult(new String(content));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        return taskCompletionSource.getTask();
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
                        binding.textViewContent.setText(task.getResult());
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
