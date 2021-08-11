package com.death.sudoku.persistence

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.death.sudoku.GameSettings
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

internal val Context.settingsDataStore: DataStore<GameSettings> by dataStore(
    fileName = "game_settings.pb",
    serializer = GameSettingsSerializer
)


private object GameSettingsSerializer: Serializer<GameSettings>{
    override val defaultValue: GameSettings
        get() = GameSettings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): GameSettings {
        try{
            return GameSettings.parseFrom(input)
        }catch (exception:InvalidProtocolBufferException){
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: GameSettings, output: OutputStream) = t.writeTo(output)

}