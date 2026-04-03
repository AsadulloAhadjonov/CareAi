import com.example.careai.ChatRequest
import com.example.careai.ChatResponse
import com.example.careai.LoginRequest
import com.example.careai.LoginResponse
import com.example.careai.RegResponse
import com.example.careai.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CareAiApiService {
    @POST("/api/chat/chat/") // O'zingizning endpointingizni yozing
    suspend fun sendChatMessage(
        @Header("Authorization") token: String,
        @Body request: ChatRequest
    ): Response<ChatResponse>

    @POST("api/auth/register/")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegResponse>

    @POST("api/auth/login/")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>
}