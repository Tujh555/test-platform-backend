package com.example.profile

import com.example.auth.database.Users
import com.example.common.Response
import com.example.common._error
import com.example.common.query
import com.example.common.success
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import net.coobird.thumbnailator.Thumbnails
import org.jetbrains.exposed.sql.update
import java.util.*
import javax.imageio.ImageIO

class ProfileService {
    suspend fun loadAvatar(userId: String, source: ByteReadChannel): Response<AvatarUpdateResponse> {
        val target = Root.folder.resolve("${UUID.randomUUID()}_thumbnail.jpg")
        try {
            val path = withContext(Dispatchers.IO) {
                target.createNewFile()
                println(target)
                val image = ImageIO.read(source.toInputStream(currentCoroutineContext().job))
                Thumbnails.of(image).size(200, 200).toFile(target)
                target.path
            }

            val url = "http://10.0.2.2:8080/$path"
            query {
                Users.update({ Users.id eq UUID.fromString(userId) }) {
                    it[avatar] = url
                }
            }

            return success { AvatarUpdateResponse(url) }
        } catch (e: Exception) {
            target.delete()
            e.printStackTrace()
            return _error(500)
        }
    }

    suspend fun changeName(userId: String, name: String) {
        query {
            Users.update({ Users.id eq UUID.fromString(userId) }) {
                it[Users.name] = name
            }
        }
    }
}