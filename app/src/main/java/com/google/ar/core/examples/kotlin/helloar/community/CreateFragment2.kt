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
class CreateFragment2 : Fragment() {
    private val repository = GPTRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.screen_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val receivedText = arguments?.getString("editTextValue")

        val flexboxLayout = view.findViewById<FlexboxLayout>(R.id.flexbox_layout)
        val editText = view.findViewById<EditText>(R.id.create_edit)
        val nextButton = view.findViewById<CardView>(R.id.nextButton)
        val questionText = view.findViewById<TextView>(R.id.question)
        questionText.text = "책의 주인공을 선택해 주세요."

        val nextButtonText = view.findViewById<TextView>(R.id.buttonText)
        nextButtonText.text = "다음(2/3)"

        val chapterText = view.findViewById<TextView>(R.id.selected_text)
        chapterText.text = "주인공"

        val avatarButton = view.findViewById<TextView>(R.id.my_avatar)
        avatarButton.setOnClickListener {
            // 클릭 시 선택된 텍스트를 아래에 표시
            editText.setText(avatarButton.text)
        }

        editText.hint = "원하는 주인공이 없다면, 입력해 주세요."

        // 로딩 화면 표시
        showLoadingFragment(2)

        nextButton.setOnClickListener {
            val editTextValue = editText.text.toString() // EditText의 값 가져오기

            // Bundle 생성 및 데이터 추가
            val bundle = Bundle().apply {
                putString("previousText", receivedText) // 이전 값 전달
                putString("currentText", editTextValue) // 현재 값 전달
            }

            // CreateFragment3 인스턴스 생성 및 데이터 전달
            val fragment = CreateFragment3().apply {
                arguments = bundle
            }

            // Fragment 교체
            parentFragmentManager.beginTransaction()
                .replace(R.id.bigger_content_frame, fragment)
                .addToBackStack(null)
                .commit()
        }

        // 프롬프트 정의
        val inputPrompt = receivedText + "라는 교훈을 가진 어린이들을 위한 10페이지짜리 동화책을 만들 거야. " +
                "주인공이 될 만한 캐릭터 10개만 만들어 줄래? 동물이나, 캐릭터면 좋을 것 같아." +
                "답변은 엔터로 구분된, 10개의 주제만 보내주면 될 것 같아." +
                "꼭 10개를 보내줘야 하고, 각 답변은 10자 이내였으면 좋겠어. 답변은 한글로 주렴."

        // 비동기 작업으로 API 호출
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = repository.getChatResponse(inputPrompt)

                // 응답이 오면 로딩 화면을 제거하고 데이터를 표시
                hideLoadingFragment()
                populateFlexboxLayout(response, flexboxLayout, editText)
            } catch (e: Exception) {
                e.printStackTrace()
                // 에러 발생 시 로딩 화면에 에러 메시지 표시
            }
        }
    }

    private fun populateFlexboxLayout(response: String, flexboxLayout: FlexboxLayout, editText: EditText) {
        val nextButton = view?.findViewById<CardView>(R.id.nextButton)

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

    private fun showLoadingFragment(pageNumber: Int) {
        val loadingFragment = LoadingFragment.newInstance(pageNumber)
        parentFragmentManager.beginTransaction()
            .add(R.id.bigger_content_frame, loadingFragment, "LoadingFragment")
            .commit()
    }

    private fun hideLoadingFragment() {
        // 로딩 화면 제거
        val loadingFragment = parentFragmentManager.findFragmentByTag("LoadingFragment")
        if (loadingFragment != null) {
            parentFragmentManager.beginTransaction()
                .remove(loadingFragment)
                .commit()
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

    override fun onDestroyView() {
        super.onDestroyView()
        // botbar을 다시 VISIBLE로 설정
        val botbar = requireActivity().findViewById<CardView>(R.id.nextButton)
        botbar.visibility = View.VISIBLE
    }

    fun isKeyboardVisible(): Boolean {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return inputMethodManager.isAcceptingText
    }
}
