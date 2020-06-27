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
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

public class Password extends AppCompatActivity {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static byte[] keyValue;

    private SharedPreferences sharedPreferences;
    private boolean changePassword = false;
    private String textForReencoding;


    public byte[] getIv() {
        byte[] iv = null;
        if (sharedPreferences.getString("iv", null) == null || changePassword) {
            // generate iv if not present
            int ivSize = 16;
            iv = new byte[ivSize];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            String ivString = Base64.getEncoder().encodeToString(iv);
            sharedPreferences.edit().putString("iv", ivString).apply();
        } else {
            String ivString = sharedPreferences.getString("iv", "");
            iv = Base64.getDecoder().decode(ivString.trim());
        }
        return iv;
    }

    public String encrypt(String valueToEnc) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(getIv());
        c.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
        byte[] encValue = c.doFinal(valueToEnc.getBytes());
        String encryptedValue = Base64.getEncoder().encodeToString(encValue);
        return encryptedValue;
    }

    public String decrypt(String encryptedValue) throws Exception {
        if (encryptedValue == null || encryptedValue.length() == 0) {
            return "";
        }
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(getIv());
        c.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
        byte[] decodedValue = Base64.getDecoder().decode(encryptedValue.trim());
        byte[] decValue = c.doFinal(decodedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }

    Key generateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        sharedPreferences = this.getSharedPreferences("pl.notepass", Context.MODE_PRIVATE);
        byte[] salt = null;
        if (sharedPreferences.getString("salt", null) == null || changePassword) {
            // generate salt if not present
            SecureRandom sr = SecureRandom.getInstanceStrong();
            salt = new byte[16];
            sr.nextBytes(salt);
            String saltString = Base64.getEncoder().encodeToString(salt);
            sharedPreferences.edit().putString("salt", saltString).apply();
        } else {
            String saltString = sharedPreferences.getString("salt", "");
            salt = Base64.getDecoder().decode(saltString.trim());
        }
        PBEKeySpec spec = new PBEKeySpec(new String(keyValue).toCharArray(), salt, 1000, 128);
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec);
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
                keyValue = EditText1.getText().toString().getBytes();
                String decodedText = null;
                try {
                    if (changePassword) {
                        decodedText = textForReencoding;
                        changePassword = false;
                        textForReencoding = null;
                    } else {
                        decodedText = open();
                        if (decodedText == null) {
                            decodedText = "";
                        }
                    }
                    String noteText = decodedText;
                    Intent intent = new Intent(Password.this, MainActivity.class);
                    intent.putExtra("NOTE_TEXT", noteText);
                    startActivityForResult(intent, 200);
                } catch (javax.crypto.BadPaddingException e) {
                    Toast.makeText(Password.this, "Password incorrect", Toast.LENGTH_SHORT).show();
                } catch (java.security.spec.InvalidKeySpecException e) {
                    Toast.makeText(Password.this, "Password field empty", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(Password.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 200 && resultCode == RESULT_OK) {
                String noteText = data.getStringExtra("NOTE_TEXT");
                if (data.getStringExtra("CHANGE_PASSWORD") != null) {
                    changePassword = data.getStringExtra("CHANGE_PASSWORD").equals("CHANGE");
                    textForReencoding = noteText;
                }
                save(noteText);
            }
        } catch (Exception ex) {
            Toast.makeText(Password.this, ex.toString(),
                    Toast.LENGTH_SHORT).show();
        } finally {
            EditText1 = (EditText) findViewById(R.id.password);
            EditText1.setText("");
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


    public String open() throws Exception {
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
