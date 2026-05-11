package com.procar.core.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Service
class ImageService(
    private val storageService: StorageService
) {

    suspend fun resizeImage(dataFlow: Flow<ByteArray>, size: Int, formatName: String): ByteArray {
        val originalBytes = dataFlow.map { it }.toList().first()
        return resizeImage(originalBytes, size, formatName)
    }

    fun resizeImage(bytes: ByteArray, size: Int, formatName: String): ByteArray {
        val originalImage = ImageIO.read(ByteArrayInputStream(bytes))
        val originalWidth = originalImage.width
        val originalHeight = originalImage.height

        val scaledWidth: Int
        val scaledHeight: Int

        if (originalWidth > originalHeight) {
            scaledWidth = size
            scaledHeight = (size * originalHeight.toDouble() / originalWidth).toInt()
        } else {
            scaledHeight = size
            scaledWidth = (size * originalWidth.toDouble() / originalHeight).toInt()
        }

        val scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH)
        val resizedImage = BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB)
        val graphics = resizedImage.createGraphics()
        graphics.drawImage(scaledImage, 0, 0, null)
        graphics.dispose()

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(resizedImage, formatName, outputStream)
        return outputStream.toByteArray()
    }

}