package com.uzumaki.naruto.filemanager.views.activities;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.uzumaki.naruto.filemanager.R;
import com.uzumaki.naruto.filemanager.views.adapters.CustomAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 123;
    private Context mContext;

    // widgets
    ListView listView;
    CustomAdapter customAdapter;
    TextView folder_name;
    AlertDialog.Builder builder;

    // variables
    private String[] options = new String[]{"Rename", "Delete"};
    private ArrayList<HashMap<String, String>> hashMapArrayList;
    File root;
    File currentFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        checkForPermission();
    }

    private void bindViews() {
        folder_name = (TextView) findViewById(R.id.folder_name);
        listView = (ListView) findViewById(R.id.listview);
    }

    //////////////// Checking for permissions ////////////////////
    private void checkForPermission() {
        int res = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (res == PackageManager.PERMISSION_GRANTED) {
            init();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PERMISSION_REQUEST_CODE && resultCode == RESULT_OK) {
            init();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void init() {
        mContext = this;

        builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getString(R.string.choose_option));

        hashMapArrayList = new ArrayList<>();
        customAdapter = new CustomAdapter(mContext, hashMapArrayList);
        listView.setAdapter(customAdapter);

        // root directory files
        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        currentFolder = root;
        listDir(currentFolder);

        listView.setOnItemClickListener(onItemClickListener);
        listView.setOnItemLongClickListener(onItemLongClickListener);
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
            File selected = new File(hashMapArrayList.get(pos).get("path"));
            if (selected.isDirectory()) {
                listDir(selected);
            } else {

                // we are only handling here intents regarding images and mp3 files
                Log.d(TAG, "onItemClick: Selected file is " + selected.getAbsolutePath());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if (selected.getPath().toLowerCase().contains(".mp3")) {
                    intent.setDataAndType(Uri.fromFile(selected), "audio/*");
                } else if (selected.getPath().toLowerCase().contains(".jpg")) {
                    intent.setDataAndType(Uri.fromFile(selected), "image/*");
                } else if (selected.getPath().toLowerCase().contains(".png")) {
                    intent.setDataAndType(Uri.fromFile(selected), "image/*");
                } else if (selected.getPath().toLowerCase().contains(".txt")) {
                    intent.setDataAndType(Uri.fromFile(selected), "text/plain");
                }
                startActivity(Intent.createChooser(intent, "Choose an option:"));
            }
        }
    };

    AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int item_num, long id) {

            final File selected = new File(hashMapArrayList.get(item_num).get("path"));
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int option_num) {
                    if(option_num == 0) {
                        // rename
                    } else if (option_num == 1) {
                        // delete the file
                        final ProgressDialog dialog = new ProgressDialog(mContext);
                        dialog.setTitle("Are you sure?");
                        dialog.setMessage("This action cannot be reverted!");
                        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(selected.exists()) {
                                    selected.delete();
                                    hashMapArrayList.remove(item_num);
                                    customAdapter.updateSet(hashMapArrayList);
                                }
                            }
                        });
                        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    }
                }
            }).show();

            return false;
        }
    };

    private void listDir(File selected) {
        hashMapArrayList.clear();

        // this will set the global variable "currentFolder" and its path above the list
        currentFolder = selected;
        folder_name.setText(currentFolder.getPath());

        File[] files = currentFolder.listFiles();
        for (File f : files) {
            HashMap<String, String> hash = new HashMap<>();
            if (f.isDirectory()) {
                hash.put("type", "folder");
                try {
                    hash.put("count", String.valueOf(f.listFiles().length));
                } catch (NullPointerException e) {
                    hash.put("count", "0");
                }
            } else if (f.isFile()) {
                hash.put("type", "file");
                hash.put("size", String.valueOf(f.length() / 1024));
            }
            hash.put("name", f.getPath().substring(f.getPath().lastIndexOf("/") + 1));
            hash.put("path", f.getPath());

            hashMapArrayList.add(hash);
        }
        customAdapter.updateSet(hashMapArrayList);
    }

    @Override
    public void onBackPressed() {
        if (currentFolder.getAbsolutePath().equals(root.getAbsolutePath()))
            super.onBackPressed();
        else
            listDir(currentFolder.getParentFile());
    }
}
