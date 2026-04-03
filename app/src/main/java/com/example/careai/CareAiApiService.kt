import com.example.careai.ChatResponse
import com.example.careai.LoginRequest
import com.example.careai.LoginResponse
import com.example.careai.RegResponse
import com.example.careai.RegisterRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CareAiApiService {
    @Multipart
    @POST("api/chat/send-audio/")
    suspend fun sendAudio(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("session_id") sessionId: RequestBody? = null
    ): retrofit2.Response<ChatResponse>

    @POST("api/auth/register/")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegResponse>

    @POST("api/auth/login/")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>
}