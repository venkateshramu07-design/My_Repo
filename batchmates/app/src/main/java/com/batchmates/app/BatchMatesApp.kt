package com.batchmates.app

import android.app.Application
import com.batchmates.app.data.AuthRepository
import com.batchmates.app.data.ReunionRepository
import com.batchmates.app.data.demo.DemoAuthRepository
import com.batchmates.app.data.demo.DemoReunionRepository
import com.batchmates.app.data.firebase.FirebaseAuthRepository
import com.batchmates.app.data.firebase.FirestoreReunionRepository
import com.batchmates.app.data.geo.GeoRepository
import com.google.firebase.FirebaseApp
import org.osmdroid.config.Configuration

class BatchMatesApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().apply {
            userAgentValue = "BatchMates/${BuildConfig.VERSION_NAME}"
            osmdroidBasePath = cacheDir
            osmdroidTileCache = cacheDir.resolve("tiles")
        }
        container = AppContainer(this)
    }
}

/** Hand-rolled DI: small app, no need for a framework. */
class AppContainer(app: Application) {
    /** Firebase is live only when google-services.json was present at build time. */
    val isDemo: Boolean = FirebaseApp.getApps(app).isEmpty() && FirebaseApp.initializeApp(app) == null

    val auth: AuthRepository = if (isDemo) DemoAuthRepository() else FirebaseAuthRepository(app)
    val reunion: ReunionRepository = if (isDemo) DemoReunionRepository() else FirestoreReunionRepository()
    val geo = GeoRepository(app)
}
