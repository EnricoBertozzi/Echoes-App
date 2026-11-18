package com.nohana.echoes_app.classroom

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nohana.echoes_app.data.TokenStorage
import com.nohana.echoes_app.network.NetworkProvider
import com.nohana.echoes_app.viewmodel.DeviceScenarioViewModel
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class ClassroomViewModelFactory(
    context: Context,
) : ViewModelProvider.Factory {

    private val applicationContext = context.applicationContext

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(
            modelClass == ClassroomViewModel::class.java ||
                    modelClass == DeviceScenarioViewModel::class.java
        ) {
            "ViewModel não suportado: ${modelClass.name}"
        }

        val baseUrl = NetworkProvider.getAddress()

        val client = NetworkProvider
            .buildSecureClient(baseUrl)
            .newBuilder()
            .apply {
                hostnameVerifier(OkHttpClient().hostnameVerifier)

                interceptors().removeAll {
                    it is HttpLoggingInterceptor
                }

                readTimeout(60, TimeUnit.SECONDS)
                writeTimeout(60, TimeUnit.SECONDS)
            }
            .build()

        val api = createAndroidClassroomApi(
            baseUrl = baseUrl,
            okHttpClient = client,
            tokens = TokenStorage(applicationContext),
        )

        val viewModel: ViewModel = when (modelClass) {
            ClassroomViewModel::class.java -> ClassroomViewModel(api)
            else -> DeviceScenarioViewModel(api)
        }

        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}