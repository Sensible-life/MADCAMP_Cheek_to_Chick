package com.google.ar.core.examples.kotlin.helloar.community

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.ar.core.examples.kotlin.helloar.R
import com.google.android.flexbox.FlexboxLayout
import com.google.ar.core.examples.kotlin.helloar.GPT.GPTRepository
import com.google.ar.core.examples.kotlin.helloar.home.HomeFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.w3c.dom.Text

@Suppress("DEPRECATION")
class CreateFragment3 : Fragment() {
    private val repository = GPTRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.screen_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val previousText = arguments?.getString("previousText")
        val currentText = arguments?.getString("currentText")

        val flexboxLayout = view.findViewById<FlexboxLayout>(R.id.flexbox_layout)
        val editText = view.findViewById<EditText>(R.id.create_edit)
        val nextButton = view.findViewById<CardView>(R.id.nextButton)
        val questionText = view.findViewById<TextView>(R.id.question)
        questionText.text = "독자의 연령대를 선택해 주세요."

        val nextButtonText = view.findViewById<TextView>(R.id.buttonText)
        nextButtonText.text = "책 생성하기"

        val chapterText = view.findViewById<TextView>(R.id.selected_text)
        chapterText.text = "연령대:"

        val avatarButton = view.findViewById<TextView>(R.id.my_avatar)
        avatarButton.visibility = View.GONE

        editText.hint = "\"8-10\"과 같이 입력해 주세요."

        nextButton.setOnClickListener {
            val editTextValue = editText.text.toString() // EditText의 값 가져오기

            // Bundle 생성 및 데이터 추가
            val bundle = Bundle().apply {
                putString("previousText", previousText) // 이전 값 전달
                putString("currentText", currentText) // 현재 값 전달
                putString("endText", editTextValue)
            }

            // CreateFragment3 인스턴스 생성 및 데이터 전달
            val fragment = CreateFragment4().apply {
                arguments = bundle
            }

            // Fragment 교체
            parentFragmentManager.beginTransaction()
                .replace(R.id.bigger_content_frame, fragment)
                .commit()
        }

        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT) // 키보드 표시
        val rootView = requireActivity().findViewById<View>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            private var isKeyboardVisible = false // 키보드 상태를 추적하기 위한 변수
            override fun onGlobalLayout() {
                val rect = Rect()
                rootView.getWindowVisibleDisplayFrame(rect)
                val screenHeight = rootView.height
                val keypadHeight = screenHeight - rect.bottom
                Log.d("KeypadHeight", "Keypad: $keypadHeight, Screen: $screenHeight")
                if (screenHeight < 1400) nextButton?.visibility = View.GONE
                else nextButton?.visibility = View.VISIBLE
            }
        })

        // 응답을 줄 단위로 나누기
        val response = "1. 0~1세\n2. 2~3세\n3. 4~5세\n4. 6~7세\n5.8~9세\n6.10~11세\n7.12~13세"
        val topics = response.split("\n")
            .map { it.trim().replace(Regex("^\\d+\\."), "").trim() } // 숫자와 마침표 제거
            .filter { it.isNotEmpty() }

        // 각 주제를 동적으로 FlexboxLayout에 추가
        topics.forEach { topic ->
            val textView = TextView(requireContext()).apply {
                text = topic
                setPadding(36, 24, 36, 24)
                setTextColor(resources.getColor(R.color.lightBlack))
                setBackgroundResource(R.drawable.background_topic) // 기본 배경
                layoutParams = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 8, 8, 8)
                }
                isClickable = true // 클릭 가능 설정
                setOnClickListener {
                    // 클릭 시 다른 항목 초기화
                    for (i in 0 until flexboxLayout.childCount) {
                        val child = flexboxLayout.getChildAt(i)
                        if (child is TextView) {
                            child.setBackgroundResource(R.drawable.background_topic) // 초기 배경
                        }
                    }
                    // 현재 클릭된 항목의 배경만 변경
                    setBackgroundResource(R.drawable.background_topic_selected)

                    // 선택된 텍스트를 EditText에 설정
                    editText.setText(topic)
                }
            }
            flexboxLayout.addView(textView)
        }

    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val botbar = requireActivity().findViewById<CardView>(R.id.nextButton)
        botbar.visibility = if (isKeyboardVisible()) View.GONE else View.VISIBLE
    }

    override fun onPause() {
        super.onPause()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    /*override fun onDestroyView() {
        super.onDestroyView()
        // botbar을 다시 VISIBLE로 설정
        val botbar = requireActivity().findViewById<CardView>(R.id.nextButton)
        botbar.visibility = View.VISIBLE
    }*/

    fun isKeyboardVisible(): Boolean {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return inputMethodManager.isAcceptingText
    }
}
