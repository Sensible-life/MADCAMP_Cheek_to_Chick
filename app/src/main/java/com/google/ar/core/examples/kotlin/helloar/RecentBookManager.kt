import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mpackage.network.LikedBooks

object RecentBookManager {
    // 크기 3의 큐 (최근 3개의 책을 저장)
    private val _recentBooks = MutableLiveData<ArrayDeque<LikedBooks>>(ArrayDeque(3))
    val recentBooks: LiveData<ArrayDeque<LikedBooks>> get() = _recentBooks

    // 큐에 책을 추가하는 함수
    fun addBookToQueue(book: LikedBooks) {
        val books = _recentBooks.value ?: ArrayDeque(3)
        if (books.size >= 3) {
            books.removeFirst()
        }
        books.addLast(book)
        _recentBooks.value = books  // LiveData를 갱신하여 UI에 알림
    }

    // 큐에 있는 책들 출력 (디버깅 용도)
    fun printRecentBooks() {
        recentBooks.value?.forEach {
            Log.d("Recent Book", it.title)  // 책의 제목을 출력
        }
    }
}
