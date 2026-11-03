package com.zwac.mcmanager;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private static final String MINECRAFT_PACKAGE = "com.mojang.minecraftpe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);

        webView.addJavascriptInterface(new WebAppInterface(), "AndroidBridge");
        webView.setWebViewClient(new WebViewClient());

        webView.loadUrl("file:///android_asset/www/index.html");
    }

    public class WebAppInterface {

        @JavascriptInterface
        public boolean isMinecraftInstalled() {
            try {
                getPackageManager().getPackageInfo(MINECRAFT_PACKAGE, 0);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }

        @JavascriptInterface
        public void launchMinecraft() {
            try {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(MINECRAFT_PACKAGE);
                if (launchIntent != null) {
                    startActivity(launchIntent);
                } else {
                    Toast.makeText(MainActivity.this, "Minecraft bulunamadı!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @JavascriptInterface
        public void sendToMinecraft(String fileName, String base64Content) {
            try {
                byte[] data = Base64.decode(base64Content, Base64.DEFAULT);
                File exportDir = new File(getExternalCacheDir(), "mc_exports");
                if (!exportDir.exists()) exportDir.mkdirs();

                File file = new File(exportDir, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(data);
                fos.close();

                Uri fileUri = FileProvider.getUriForFile(
                        MainActivity.this,
                        getPackageName() + ".provider",
                        file
                );

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(fileUri, "*/*");
                intent.setPackage(MINECRAFT_PACKAGE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    // Minecraft ile doğrudan açılamadıysa Genel Aç komutu
                    Intent genericIntent = new Intent(Intent.ACTION_VIEW);
                    genericIntent.setDataAndType(fileUri, "*/*");
                    genericIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(genericIntent, "Minecraft Seçin"));
                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "İçe aktarma hatası: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        @JavascriptInterface
        public void showToast(String message) {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
