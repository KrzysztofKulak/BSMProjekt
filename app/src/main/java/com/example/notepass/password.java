package com.example.notepass;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class password extends AppCompatActivity {
    private static final String ALGORITHM = "AES";
    private static final byte[] keyValue =
            new byte[]{'T', 'h', 'i', 's', 'I', 's', 'A', 'S', 'e', 'c', 'r', 'e', 't', 'K', 'e', 'y'};

    private SharedPreferences sharedPreferences;

    public String encrypt(String valueToEnc) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encValue = c.doFinal(valueToEnc.getBytes());
        String encryptedValue = Base64.getEncoder().encodeToString(encValue);
        return encryptedValue;
    }

    public String decrypt(String encryptedValue) throws Exception {
        if(encryptedValue == null || encryptedValue.length() == 0) {
            return "";
        }
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue = Base64.getDecoder().decode(encryptedValue.trim());
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }

    private Key generateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        sharedPreferences = this.getSharedPreferences("pl.notepass", Context.MODE_PRIVATE);
        byte[] salt = new byte[16];
        if (sharedPreferences.getString("salt", null) == null) {
            SecureRandom sr = SecureRandom.getInstanceStrong();
            sr.nextBytes(salt);
            String saltString = new String(salt);
            sharedPreferences.edit().putString("salt", saltString).apply();
        }
        PBEKeySpec spec = new PBEKeySpec(new String(keyValue).toCharArray(), salt, 1000, 128);
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec);
//        Cipher aes = Cipher.getInstance("AES");
//        aes.init(Cipher.ENCRYPT_MODE, key);
//        return new SecretKeySpec(keyValue, ALGORITHM);
    }

    EditText EditText1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button login = findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText1 = (EditText) findViewById(R.id.password);
                String pass = EditText1.getText().toString();
                String decodedText = null;
                try {
                    decodedText = open(pass);
                } catch (Exception e) {
                    Toast.makeText(password.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                if (decodedText == null) {
                    decodedText = "";
                }
                String noteText = decodedText;
                Intent intent = new Intent(password.this, MainActivity.class);
                intent.putExtra("NOTE_TEXT", noteText);
                startActivityForResult(intent, 200);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 200 && resultCode == RESULT_OK) {
                String noteText = data.getStringExtra("NOTE_TEXT");
                save(noteText);
            }
        } catch (Exception ex) {
            Toast.makeText(password.this, ex.toString(),
                    Toast.LENGTH_SHORT).show();
        }

    }

    public void save(String text) {
        try {
            OutputStreamWriter out =
                    new OutputStreamWriter(openFileOutput("Note1.txt", 0));
            String encText = encrypt(text);
            out.write(encText);
            out.close();
            Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
        } catch (Throwable t) {
            Toast.makeText(this, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }
    }


    public String open(String password) throws Exception {
        String content = "";
        String fileName = "Note1.txt";
        if (FileExists(fileName)) {
            try {
                InputStream in = openFileInput(fileName);
                if (in != null) {
                    InputStreamReader tmp = new InputStreamReader(in);
                    BufferedReader reader = new BufferedReader(tmp);
                    String str;
                    StringBuilder buf = new StringBuilder();
                    while ((str = reader.readLine()) != null) {
                        buf.append(str + "\n");
                    }
                    in.close();
                    content = buf.toString();
                }
            } catch (Throwable t) {
                Toast.makeText(this, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
            }
        }
        String decrypted_content = decrypt(content);
        return decrypted_content;
    }


    public boolean FileExists(String fname) {
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }


}
