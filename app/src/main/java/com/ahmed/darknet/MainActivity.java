package com.darknet.configs;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView configTextView;
    private EditText adminPasswordInput, newConfigInput;
    private Button fetchConfigButton, updateConfigButton;
    private View adminPanel;
    
    // كلمة السر المعتمدة لأحمد
    private final String ADMIN_PASSWORD = "AHMED_VIP_DARK";
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // تهيئة قاعدة البيانات والواجهات
        mDatabase = FirebaseDatabase.getInstance().getReference().child("DarkNetConfig");
        
        configTextView = findViewById(R.id.configTextView);
        adminPasswordInput = findViewById(R.id.adminPasswordInput);
        newConfigInput = findViewById(R.id.newConfigInput);
        fetchConfigButton = findViewById(R.id.fetchConfigButton);
        updateConfigButton = findViewById(R.id.updateConfigButton);
        adminPanel = findViewById(R.id.adminPanel);

        // جلب التحديث للمستخدمين
        fetchConfigButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchConfigFromFirebase();
            }
        });

        // زر التحديث الخاص بالأدمن أحمد
        updateConfigButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredPassword = adminPasswordInput.getText().toString().trim();
                if (enteredPassword.equals(ADMIN_PASSWORD)) {
                    String newConfig = newConfigInput.getText().toString().trim();
                    if (!newConfig.isEmpty()) {
                        mDatabase.setValue(newConfig);
                        Toast.makeText(MainActivity.this, "تم تحديث الكونفيج بنجاح!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "الرجاء كتابة الكونفيج أولاً", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "كلمة السر خاطئة!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchConfigFromFirebase() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://darknetapp-dummy-default-rtdb.firebaseio.com/DarkNetConfig.json");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Host", "choof.ooredoo.dz");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    
                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String config = dataSnapshot.getValue(String.class);
                                    configTextView.setText(config);
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // معالجة الخطأ عند الإلغاء
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
