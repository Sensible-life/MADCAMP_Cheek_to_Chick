import com.mpackage.network.Book
import com.mpackage.network.LikedBooks
import retrofit2.Response
import retrofit2.http.GET

interface BooksApi {
    @GET("/api/books/liked?limit=6") // 좋아요가 true인 책을 가져오는 API
    suspend fun getLikedBooks(): Response<List<LikedBooks>>
}
