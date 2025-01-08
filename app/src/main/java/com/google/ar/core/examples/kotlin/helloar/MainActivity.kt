package com.google.ar.core.examples.kotlin.helloar

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import com.google.ar.core.examples.kotlin.helloar.BuildConfig
import com.google.ar.core.examples.kotlin.helloar.book.BookActivity
import com.google.ar.core.examples.kotlin.helloar.community.CreateFragment
import com.google.ar.core.examples.kotlin.helloar.home.HomeFragment
import com.google.ar.core.examples.kotlin.helloar.profile.ProfileFragment
import com.google.gson.Gson
import com.mpackage.network.LikedBooks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import com.google.ar.core.examples.kotlin.helloar.Ranking.RankingFragment



@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private val client = OkHttpClient()
    override fun onCreate(savedInstanceState: Bundle?) {


        val splashScreen = installSplashScreen()
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)    //자동 생성 상단바 없앰

        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_main)
        supportFragmentManager.beginTransaction().replace(R.id.content_frame, HomeFragment())
            .commit()

        val dragArea = findViewById<FrameLayout>(R.id.dragArea)
        val colorChange = findViewById<ImageView>(R.id.bottom_circle)
        val outline = findViewById<ImageView>(R.id.outline)
        val backCircle = findViewById<ImageView>(R.id.backCircle)
        val backgroundDark = findViewById<View>(R.id.backgroundDark)
        val recentBook1 = findViewById<ImageView>(R.id.recentBook_1)
        val recentBook2 = findViewById<ImageView>(R.id.recentBook_2)
        val recentBook3 = findViewById<ImageView>(R.id.recentBook_3)


        // dragArea에 대한 작업 수행
        dragArea?.setOnDragListener { view, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    // 드래그 시작 시
                    // view.setBackgroundColor(Color.LTGRAY)  // 색상 변경
                    outline.visibility = View.VISIBLE
                    true
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    // 드래그가 드롭 영역 안으로 들어왔을 때
                    Log.d("DRAG", "DROPPED IN!")
                    //view.setBackgroundColor(Color.YELLOW)  // 색상 변경
                    colorChange.setBackgroundResource(R.drawable.background_center_circle_yellow)
                    outline.visibility = View.GONE
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    // 드래그가 드롭 영역을 벗어났을 때
                    //view.setBackgroundColor(Color.LTGRAY)
                    colorChange.setBackgroundResource(R.drawable.background_center_circle)
                    outline.visibility = View.VISIBLE
                    true
                }
                DragEvent.ACTION_DROP -> {
                    // 드롭 시 (드래그한 뷰를 드롭 위치로 이동)
                    val droppedView = event.localState as View
                    val x = event.x
                    val y = event.y

                    val id = droppedView.id
                    var selectedBook: LikedBooks = LikedBookManager.lbooks[0]

                    when (id) {
                        R.id.book_1 -> {
                            selectedBook = LikedBookManager.lbooks[0]
                        }
                        R.id.book_2 -> {
                            selectedBook = LikedBookManager.lbooks[1]
                        }
                        R.id.book_3 -> {
                            selectedBook = LikedBookManager.lbooks[2]
                        }
                        R.id.book_4 -> {
                            selectedBook = LikedBookManager.lbooks[3]
                        }
                        R.id.book_5 -> {
                            selectedBook = LikedBookManager.lbooks[4]
                        }
                        R.id.book_6 -> {
                            selectedBook = LikedBookManager.lbooks[5]
                        }
                    }
                    // 드롭된 뷰 위치 업데이트

                    selectedBook.let {
                        val jsonData = Gson().toJson(it)
                        Log.d("Book Data", jsonData)

                        // JSON 문자열을 JSONObject로 변환
                        val jsonObject = JSONObject(jsonData)
                        val pagesArray = jsonObject.getJSONArray("pages")
                        val contents = mutableListOf<String>() // 페이지 내용 리스트
                        val savedFilePaths = mutableListOf<String>()
                        val contentsWithPaths = mutableListOf<ContentWithPath>()

                        for (i in 0 until pagesArray.length()) {
                            val pageObject = pagesArray.getJSONObject(i)
                            val content = pageObject.getString("content")
                            contents.add(content)  // "content" 값을 contents 리스트에 추가

                            val imgUrl = pageObject.getString("image_url")
                            imgUrl.let { base64Image ->
                                val filename = "image_${i + 1}.jpg"
                                val filePath = saveBase64ImageToFile(base64Image, this, filename)
                                filePath?.let {
                                    savedFilePaths.add(it)
                                    // content와 imagePath를 쌍으로 묶어 contentsWithPaths에 추가
                                    contentsWithPaths.add(ContentWithPath(content, it))
                                }
                            }
                        }
                        val intent = Intent(this, BookActivity::class.java)
                        intent.putExtra("contentsWithPaths", ArrayList(contentsWithPaths))
                        startActivity(intent)
                    }

                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    // 드래그가 끝났을 때
                    // view.setBackgroundColor(Color.TRANSPARENT)
                    outline.visibility = View.GONE
                    colorChange.setBackgroundResource(R.drawable.background_center_circle)
                    true
                }
                else -> false
            }
        }



        val homeLayout = findViewById<LinearLayout>(R.id.homeLayout)
        val homeButton = findViewById<ImageButton>(R.id.homeButton)

        val cameraLayout = findViewById<LinearLayout>(R.id.cameraLayout)
        val cameraButton = findViewById<ImageButton>(R.id.cameraButton)

        val communityLayout = findViewById<LinearLayout>(R.id.communityLayout)
        val communityButton = findViewById<ImageButton>(R.id.communityButton)

        val profileLayout = findViewById<LinearLayout>(R.id.profileLayout)
        val profileButton = findViewById<ImageButton>(R.id.profileButton)

        homeLayout.setOnClickListener {
            homeButton.setImageResource(R.drawable.icon_home_selected)
            cameraButton.setImageResource(R.drawable.icon_camera)
            communityButton.setImageResource(R.drawable.icon_community)
            profileButton.setImageResource(R.drawable.icon_profile)
            supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, HomeFragment())
                .addToBackStack(null)
                .commit()
        }
        homeButton.setOnClickListener {
            homeButton.setImageResource(R.drawable.icon_home_selected)
            cameraButton.setImageResource(R.drawable.icon_camera)
            communityButton.setImageResource(R.drawable.icon_community)
            profileButton.setImageResource(R.drawable.icon_profile)
            supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, HomeFragment())
                .addToBackStack(null)
                .commit()
        }

        cameraLayout.setOnClickListener {
            homeButton.setImageResource(R.drawable.icon_home)
            cameraButton.setImageResource(R.drawable.icon_camera_selected)
            communityButton.setImageResource(R.drawable.icon_community)
            profileButton.setImageResource(R.drawable.icon_profile)
            supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, RankingFragment())
                .addToBackStack(null)
                .commit()
        }
        cameraLayout.setOnClickListener {
            homeButton.setImageResource(R.drawable.icon_home)
            cameraButton.setImageResource(R.drawable.icon_camera_selected)
            communityButton.setImageResource(R.drawable.icon_community)
            profileButton.setImageResource(R.drawable.icon_profile)
            supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, RankingFragment())
                .addToBackStack(null)
                .commit()
        }

        communityLayout.setOnClickListener {
            homeButton.setImageResource(R.drawable.icon_home)
            cameraButton.setImageResource(R.drawable.icon_camera)
            communityButton.setImageResource(R.drawable.icon_community_selected)
            profileButton.setImageResource(R.drawable.icon_profile)
            supportFragmentManager.beginTransaction()
                .replace(R.id.bigger_content_frame, CreateFragment())
                .addToBackStack(null)
                .commit()
        }
        communityButton.setOnClickListener {
            homeButton.setImageResource(R.drawable.icon_home)
            cameraButton.setImageResource(R.drawable.icon_camera)
            communityButton.setImageResource(R.drawable.icon_community_selected)
            profileButton.setImageResource(R.drawable.icon_profile)
            supportFragmentManager.beginTransaction()
                .replace(R.id.bigger_content_frame, CreateFragment())
                .addToBackStack(null)
                .commit()
        }

        profileLayout.setOnClickListener {
            homeButton.setImageResource(R.drawable.icon_home)
            cameraButton.setImageResource(R.drawable.icon_camera)
            communityButton.setImageResource(R.drawable.icon_community)
            profileButton.setImageResource(R.drawable.icon_profile_selected)
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    0,
                    0,
                    R.anim.slide_in_left,  // 뒤로 가기 버튼을 눌렀을 때, 새 fragment가 왼쪽에서 오른쪽으로 들어옴
                    R.anim.slide_out_right  // 기존 fragment가 오른쪽으로 나감
                )
                .replace(R.id.content_frame, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }
        profileButton.setOnClickListener {
            homeButton.setImageResource(R.drawable.icon_home)
            cameraButton.setImageResource(R.drawable.icon_camera)
            communityButton.setImageResource(R.drawable.icon_community)
            profileButton.setImageResource(R.drawable.icon_profile_selected)
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    0,
                    0,
                    R.anim.slide_in_left,  // 뒤로 가기 버튼을 눌렀을 때, 새 fragment가 왼쪽에서 오른쪽으로 들어옴
                    R.anim.slide_out_right  // 기존 fragment가 오른쪽으로 나감
                )
                .replace(R.id.content_frame, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        /*profileButton.setOnClickListener {
            send3DRequest()
        }*/

    }






    fun saveBase64ImageToFile(base64String: String, context: Context, filename: String): String? {
        return try {
            // Base64 문자열을 디코딩하여 바이트 배열로 변환
            val decodedString: ByteArray = Base64.decode(base64String, Base64.NO_WRAP)

            // 바이트 배열을 Bitmap으로 변환
            val bitmap: Bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

            // 파일 경로 정의 (앱의 내부 저장소)
            val file = File(context.filesDir, filename)

            // 파일에 Bitmap을 PNG 형식으로 저장
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            // 저장된 파일의 경로 반환
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null // 오류 발생 시 null 반환
        }
    }
    fun decodeBase64Image(base64String: String): Bitmap? {
        return try {
            val decodedString: ByteArray = Base64.decode(base64String, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}