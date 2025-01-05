package com.google.ar.core.examples.kotlin.helloar

import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.widget.LinearLayout
import com.google.ar.core.examples.kotlin.helloar.community.CreateFragment
import com.google.ar.core.examples.kotlin.helloar.home.HomeFragment
import com.google.ar.core.examples.kotlin.helloar.profile.ProfileFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)    //자동 생성 상단바 없앰

        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_main)
        supportFragmentManager.beginTransaction().
        replace(R.id.content_frame, HomeFragment()).commit()

        val homeLayout= findViewById<LinearLayout>(R.id.homeLayout)
        val homeButton = findViewById<ImageButton>(R.id.homeButton)

        val cameraLayout= findViewById<LinearLayout>(R.id.cameraLayout)
        val cameraButton = findViewById<ImageButton>(R.id.cameraButton)

        val communityLayout= findViewById<LinearLayout>(R.id.communityLayout)
        val communityButton = findViewById<ImageButton>(R.id.communityButton)

        val profileLayout= findViewById<LinearLayout>(R.id.profileLayout)
        val profileButton = findViewById<ImageButton>(R.id.profileButton)

        homeLayout.setOnClickListener {
            homeButton.setImageResource(R.drawable.icon_home_selected)
            cameraButton.setImageResource(R.drawable.icon_camera)
            communityButton.setImageResource(R.drawable.icon_community)
            profileButton.setImageResource(R.drawable.icon_profile)
            supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, HomeFragment()).commit()
        }
        homeButton.setOnClickListener {
            homeButton.setImageResource(R.drawable.icon_home_selected)
            cameraButton.setImageResource(R.drawable.icon_camera)
            communityButton.setImageResource(R.drawable.icon_community)
            profileButton.setImageResource(R.drawable.icon_profile)
            supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, HomeFragment()).commit()
        }


        cameraLayout.setOnClickListener {
            homeButton.setImageResource(R.drawable.icon_home)
            cameraButton.setImageResource(R.drawable.icon_camera_selected)
            communityButton.setImageResource(R.drawable.icon_community)
            profileButton.setImageResource(R.drawable.icon_profile)
            startActivity(Intent(this, HelloArActivity::class.java))
        }
        cameraButton.setOnClickListener {
            homeButton.setImageResource(R.drawable.icon_home)
            cameraButton.setImageResource(R.drawable.icon_camera_selected)
            communityButton.setImageResource(R.drawable.icon_community)
            profileButton.setImageResource(R.drawable.icon_profile)
            startActivity(Intent(this, HelloArActivity::class.java))
        }

        communityLayout.setOnClickListener {
            homeButton.setImageResource(R.drawable.icon_home)
            cameraButton.setImageResource(R.drawable.icon_camera)
            communityButton.setImageResource(R.drawable.icon_community_selected)
            profileButton.setImageResource(R.drawable.icon_profile)
            supportFragmentManager.beginTransaction()
                .replace(R.id.bigger_content_frame, CreateFragment())
                .addToBackStack(null).commit()
        }
        communityButton.setOnClickListener {
            homeButton.setImageResource(R.drawable.icon_home)
            cameraButton.setImageResource(R.drawable.icon_camera)
            communityButton.setImageResource(R.drawable.icon_community_selected)
            profileButton.setImageResource(R.drawable.icon_profile)
            supportFragmentManager.beginTransaction()
                .replace(R.id.bigger_content_frame, CreateFragment()).commit()
        }

        profileLayout.setOnClickListener {
            homeButton.setImageResource(R.drawable.icon_home)
            cameraButton.setImageResource(R.drawable.icon_camera)
            communityButton.setImageResource(R.drawable.icon_community)
            profileButton.setImageResource(R.drawable.icon_profile_selected)
            supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, ProfileFragment()).commit()
        }
        profileButton.setOnClickListener {
            homeButton.setImageResource(R.drawable.icon_home)
            cameraButton.setImageResource(R.drawable.icon_camera)
            communityButton.setImageResource(R.drawable.icon_community)
            profileButton.setImageResource(R.drawable.icon_profile_selected)
            supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, ProfileFragment()).commit()
        }
    }
}