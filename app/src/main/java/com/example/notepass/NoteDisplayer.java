package com.example.notepass;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.EditText;

public class NoteDisplayer extends AppCompatActivity {
    EditText EditText1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_displayer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        EditText1 = (EditText) findViewById(R.id.EditText1);
        String noteText = getIntent().getStringExtra("NOTE_TEXT");
        EditText1.setText(noteText);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String noteText = EditText1.getText().toString();
                Intent intent = new Intent(NoteDisplayer.this, PasswordScreen.class);
                intent.putExtra("NOTE_TEXT", noteText);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String noteText = EditText1.getText().toString();
                Intent intent = new Intent(NoteDisplayer.this, PasswordScreen.class);
                intent.putExtra("NOTE_TEXT", noteText);
                intent.putExtra("CHANGE_PASSWORD", "CHANGE");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

}
