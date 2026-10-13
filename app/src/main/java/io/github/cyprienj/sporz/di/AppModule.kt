package io.github.cyprienj.sporz.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.cyprienj.sporz.data.FileSessionStore
import io.github.cyprienj.sporz.data.SessionStore
import io.github.cyprienj.sporz.data.history.FinishedGameDao
import io.github.cyprienj.sporz.data.history.GameHistory
import io.github.cyprienj.sporz.data.history.RoomGameHistory
import io.github.cyprienj.sporz.data.history.SporzDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/** Scope living as long as the app, for background saves (IO dispatcher). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    abstract fun sessionStore(store: FileSessionStore): SessionStore

    @Binds
    abstract fun gameHistory(history: RoomGameHistory): GameHistory

    companion object {
        @Provides
        @Singleton
        fun database(@ApplicationContext context: Context): SporzDatabase =
            Room.databaseBuilder(context, SporzDatabase::class.java, "sporz.db").build()

        @Provides
        fun finishedGames(database: SporzDatabase): FinishedGameDao = database.finishedGames()

        @Provides
        @Singleton
        @ApplicationScope
        fun applicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
