package com.example.letschat.Activities;

import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.widget.ImageView;

import com.example.letschat.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;


public class ImageViewerActivity extends AppCompatActivity {
    private PhotoView imageViewer;
    private String imageUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        imageViewer=(PhotoView) findViewById(R.id.image_viewer);
        imageUrl=getIntent().getStringExtra("url");
        Picasso.get().load(imageUrl).placeholder(R.drawable.crop_image_menu_flip).into(imageViewer);
    }
}