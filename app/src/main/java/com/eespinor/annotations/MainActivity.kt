package com.eespinor.annotations

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.eespinor.annotations.ui.theme.AnnotationsTheme
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

class MainActivity : ComponentActivity() {

    private val api by lazy {
        Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com")
            .client(
                OkHttpClient
                    .Builder()
                    .addInterceptor(AuthInterceptor())
                    .addInterceptor(HttpLoggingInterceptor().setLevel(
                        HttpLoggingInterceptor.Level.BODY
                    )).build()
            )
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(MyApi::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            api.getPost()
            api.getUser()
        }
    }
}

data class User(
    val name: String,

    @AllowedRegex("\\d{4}-\\d{2}-\\d{2}") val birthDate: String
) {
    init {
        val fields = this::class.java.declaredFields
        fields.forEach { field ->
            field.annotations.forEach {
                if (field.isAnnotationPresent(AllowedRegex::class.java)) {
                    val regex = field.getAnnotation(AllowedRegex::class.java)?.regex
                    if (regex?.toRegex()?.matches(birthDate) == false) {
                        throw IllegalArgumentException(
                            "Birt date is not " +
                                    "a valid date $birthDate"
                        )
                    }
                }

            }
        }
    }
}

@Target(AnnotationTarget.FIELD)

annotation class AllowedRegex(val regex: String)