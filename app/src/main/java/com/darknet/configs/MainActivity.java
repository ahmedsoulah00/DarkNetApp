package com.darknet.configs;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String ADMIN_PASSWORD = "AHMED_VIP_DARK"; 
    private static final String DATABASE_URL = "http://your-firebase-project-default-rtdb.firebaseio.com/config.json"; 
    private static final String FREE_HOST = "choof.ooredoo.dz"; 

    private LinearLayout layoutUserInterface;
    private Button btnFetchConfig, btnCopyConfig;
    private TextView tvConfigDisplay, tvNoteDisplay;

    private LinearLayout layoutAdminLogin;
    private EditText etAdminPassword;
    private Button btnAdminLogin;

    private LinearLayout layoutAdminPanel;
    private EditText etNewConfigText, etNewConfigNote;
    private Button btnPublishUpdate, btnLogoutAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        buildDynamicLayout();

        btnFetchConfig.setOnClickListener(v -> fetchConfigFromServer());

        btnCopyConfig.setOnClickListener(v -> {
            String configText = tvConfigDisplay.getText().toString();
            if (!configText.isEmpty() && !configText.startsWith("الكونفيج")) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("DarkNetConfig", configText);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.this, "✅ تم نسخ كود الكونفيج! افتح تطبيق DarkTunnel ودير استيراد.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "❌ الرجاء جلب الكونفيج أولاً!", Toast.LENGTH_SHORT).show();
            }
        });

        btnAdminLogin.setOnClickListener(v -> {
            String enteredPassword = etAdminPassword.getText().toString().trim();
            if (enteredPassword.equals(ADMIN_PASSWORD)) {
                layoutUserInterface.setVisibility(View.GONE);
                layoutAdminLogin.setVisibility(View.GONE);
                layoutAdminPanel.setVisibility(View.VISIBLE);
                etAdminPassword.setText("");
                Toast.makeText(MainActivity.this, "مرحباً بك يا أحمد في لوحتك الخاصة! 🥷🏻", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "❌ الرمز السري خاطئ!", Toast.LENGTH_SHORT).show();
            }
        });

        btnPublishUpdate.setOnClickListener(v -> {
            String newConfig = etNewConfigText.getText().toString().trim();
            String newNote = etNewConfigNote.getText().toString().trim();
            if (!newConfig.isEmpty()) {
                publishConfigToServer(newConfig, newNote);
            } else {
                Toast.makeText(MainActivity.this, "❌ الرجاء إدخال كود الكونفيج أولاً!", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogoutAdmin.setOnClickListener(v -> {
            layoutAdminPanel.setVisibility(View.GONE);
            layoutUserInterface.setVisibility(View.VISIBLE);
            layoutAdminLogin.setVisibility(View.VISIBLE);
            Toast.makeText(MainActivity.this, "تم الخروج من لوحة الإدارة.", Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchConfigFromServer() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(DATABASE_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Host", FREE_HOST);
                    conn.setRequestProperty("X-Online-Host", FREE_HOST);
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(8000);

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = in.readLine()) != null) response.append(line);
                        in.close();
                        return response.toString();
                    }
                } catch (Exception e) {
                    return null;
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        String config = json.getString("config_text");
                        String note = json.optString("note", "لا توجد ملاحظات حالية.");
                        
                        tvConfigDisplay.setText(config);
                        tvNoteDisplay.setText("📝 معلومات الكونفيج:\n" + note);
                        btnCopyConfig.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this, "✅ تم جلب التحديث بنجاح!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "خطأ في معالجة البيانات.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "❌ فشل الاتصال! تأكد من تشغيل بيانات أوريدو.", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private void publishConfigToServer(String configText, String note) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    URL url = new URL(DATABASE_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setDoOutput(true);

                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("config_text", configText);
                    jsonBody.put("note", note);

                    OutputStream os = conn.getOutputStream();
                    os.write(jsonBody.toString().getBytes("UTF-8"));
                    os.close();

                    return conn.getResponseCode() == HttpURLConnection.HTTP_OK;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(MainActivity.this, "🔥 تم تجديد كود الكونفيج بنجاح على السيرفر!", Toast.LENGTH_LONG).show();
                    etNewConfigText.setText("");
                    etNewConfigNote.setText("");
                } else {
                    Toast.makeText(MainActivity.this, "❌ فشل الرفع! تأكد من توفر إنترنت عادي للأدمن.", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void buildDynamicLayout() {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 40, 40, 40);

        layoutUserInterface = new LinearLayout(this);
        layoutUserInterface.setOrientation(LinearLayout.VERTICAL);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("👑 𓆩 DARK NET CONFIGS 𓆪 👑");
        tvTitle.setTextSize(20);
        tvTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvTitle.setPadding(0, 10, 0, 30);

        btnFetchConfig = new Button(this);
        btnFetchConfig.setText("🔄 جلب تحديث الكونفيج (بدون إنترنت)");

        tvConfigDisplay = new TextView(this);
        tvConfigDisplay.setPadding(20, 20, 20, 20);
        tvConfigDisplay.setHint("كود الكونفيج المحدث سيظهر هنا...");
        tvConfigDisplay.setBackgroundColor(0x11000000);
        tvConfigDisplay.setTextIsSelectable(true);

        btnCopyConfig = new Button(this);
        btnCopyConfig.setText("📋 نسخ الكود بالكامل");
        btnCopyConfig.setVisibility(View.GONE);

        tvNoteDisplay = new TextView(this);
        tvNoteDisplay.setPadding(0, 20, 0, 20);
        tvNoteDisplay.setText("شغل بيانات شريحة أوريدو (حتى بدون رصيد) ثم اضغط على زر التحديث لجلب كود الـ 3 ساعات الجديد.");

        layoutUserInterface.addView(tvTitle);
        layoutUserInterface.addView(btnFetchConfig);
        layoutUserInterface.addView(tvConfigDisplay);
        layoutUserInterface.addView(btnCopyConfig);
        layoutUserInterface.addView(tvNoteDisplay);

        layoutAdminLogin = new LinearLayout(this);
        layoutAdminLogin.setOrientation(LinearLayout.HORIZONTAL);
        layoutAdminLogin.setPadding(0, 50, 0, 0);

        etAdminPassword = new EditText(this);
        etAdminPassword.setHint("رمز سري للأدمن");
        etAdminPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        
        btnAdminLogin = new Button(this);
        btnAdminLogin.setText("دخول");

        layoutAdminLogin.addView(etAdminPassword, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        layoutAdminLogin.addView(btnAdminLogin);

        layoutAdminPanel = new LinearLayout(this);
        layoutAdminPanel.setOrientation(LinearLayout.VERTICAL);
        layoutAdminPanel.setVisibility(View.GONE);

        TextView tvAdminTitle = new TextView(this);
        tvAdminTitle.setText("🥷🏻 لوحة إدارة وتجديد الكونفيجات الخاصة بأحمد");
        tvAdminTitle.setTextSize(18);
        tvAdminTitle.setPadding(0, 10, 0, 20);

        etNewConfigText = new EditText(this);
        etNewConfigText.setHint("الصق هنا كود كونفيج DarkTunnel الجديد");
        
        etNewConfigNote = new EditText(this);
        etNewConfigNote.setHint("اكتب وصف التحديث (مثال: سيرفر بلجيكا 3 ساعات يدعم ألعاب)");

        btnPublishUpdate = new Button(this);
        btnPublishUpdate.setText("📤 نشر وتجديد الكود فوراً");

        btnLogoutAdmin = new Button(this);
        btnLogoutAdmin.setText("🚪 تسجيل الخروج والعودة");
        btnLogoutAdmin.setBackgroundColor(0xFFFF2222);
        btnLogoutAdmin.setTextColor(0xFFFFFFFF);

        layoutAdminPanel.addView(tvAdminTitle);
        layoutAdminPanel.addView(etNewConfigText);
        layoutAdminPanel.addView(etNewConfigNote);
        layoutAdminPanel.addView(btnPublishUpdate);
        layoutAdminPanel.addView(btnLogoutAdmin);

        mainLayout.addView(layoutUserInterface);
        mainLayout.addView(layoutAdminLogin);
        mainLayout.addView(layoutAdminPanel);
        
        scrollView.addView(mainLayout);
        setContentView(scrollView);
    }
}
