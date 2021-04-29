package com.pomplarg.spe95

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.pomplarg.spe95.utils.Constants
import kotlinx.android.synthetic.main.activity_fullscreen.*


class FullScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen)

        setContentView(R.layout.activity_fullscreen)

        val photoPath = intent.getStringExtra(Constants.PATH_PHOTO_FULLSCREEN_KEY)

        val reference = photoPath?.let { FirebaseStorage.getInstance().getReference(it) }
        Glide.with(this)
            .load(reference)
            .into(iv_photo_expended)
    }
}