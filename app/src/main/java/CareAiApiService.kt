
import com.example.careai.LoginRequest
import com.example.careai.LoginResponse
import com.example.careai.RegResponse
import com.example.careai.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CareAiApiService {
    @POST("api/auth/register/")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegResponse>

    @POST("api/auth/login/")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>
}