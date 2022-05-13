package com.sample.camerax.demo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class WebViewActivity extends AppCompatActivity {

    private static final String TAG = "Demo.Web";
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;
    private ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::onActivityResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            loadWebView();
        } else {
            requestPermissions(CAMERA_PERMISSION, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        loadWebView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    private void loadWebView() {
        WebView webView = findViewById(R.id.webView);
        WebSettings webViewSettings = webView.getSettings();
        webViewSettings.setJavaScriptEnabled(true);

        webView.addJavascriptInterface(new CustomJavascriptInterface(), "android");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.i(TAG, "Console message " + consoleMessage.message());
                return super.onConsoleMessage(consoleMessage);
            }
        });
        webView.loadData(getHtmlContent(), "text/html", "utf-8");
    }

    private String getHtmlContent() {
        try (InputStream inputStream = getResources().openRawResource(R.raw.index)) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[512];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return new String(outputStream.toByteArray(), "UTF-8");
        } catch (IOException e) {
            throw new IllegalStateException("Unable to get html", e);
        }
    }

    private void onActivityResult(ActivityResult result) {
        Intent data = result.getData();
        String activity = data.getStringExtra("ACTIVITY");
        Log.i(TAG, "onActivityResult " + activity);
        WebView webView = findViewById(R.id.webView);
        int delay = 500;
        // int delay = -1;
        String script;
        if (delay <= 0) {
            Log.i(TAG, "No delay for the activity " + activity);
            script = "javascript: onResponse()";
        } else {
            script = "javascript: window.setTimeout(() => onResponse(), " + delay + ")";
        }
        webView.evaluateJavascript(script, null);
        Log.i(TAG, "Script " + script);
    }

    private void startCameraActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        activityLauncher.launch(intent);
    }

    private class CustomJavascriptInterface {

        @JavascriptInterface
        public void openCameraX() {
            Log.i(TAG, "openCameraX");
            startCameraActivity(CameraXActivity.class);
        }

        @JavascriptInterface
        public void openCamera() {
            Log.i(TAG, "openCamera");
            startCameraActivity(CameraActivity.class);
        }
    }
}