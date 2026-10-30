package com.devbyzenith.rabbitskater

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Build
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import javax.xml.parsers.DocumentBuilderFactory

class SpriteManager {

    data class SpriteFrame(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int,
        val rotated: Boolean,
        val offsetX: Float,
        val offsetY: Float
    )

    private val atlasLock = Any()
    private val spriteCache = ConcurrentHashMap<String, Bitmap>()

    @Volatile
    private var frameIndex: Map<String, SpriteFrame> = emptyMap()

    @Volatile
    private var atlasDecoder: BitmapRegionDecoder? = null

    @Volatile
    private var atlasView: Map<String, Bitmap> = emptyMap()

    private var loadedPlistPath: String? = null
    private var loadedPngPath: String? = null

    fun parsePlist(inputStream: InputStream): Map<String, SpriteFrame> {
        val documentFactory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = false
            isValidating = false
            runCatching {
                setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            }
            runCatching {
                setFeature("http://xml.org/sax/features/external-general-entities", false)
            }
            runCatching {
                setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            }
        }

        // 기존 Buildbox 에셋의 자체 닫힘 DOCTYPE은 표준 XML 파서가 거부합니다.
        // 원본 파일을 수정하지 않고 알려진 선언만 제거하며 외부 DTD는 로드하지 않습니다.
        val normalizedXml = inputStream.use { stream ->
            stream.readBytes().toString(Charsets.UTF_8).replace(LEGACY_PLIST_DOCTYPE, "")
        }
        val document = normalizedXml.byteInputStream().use { stream ->
            documentFactory.newDocumentBuilder().parse(stream)
        }

        val plistNode = document.documentElement ?: return emptyMap()
        val rootDict = firstChildElement(plistNode, DICT_TAG) ?: return emptyMap()
        val framesDict = findValueElement(rootDict, FRAMES_KEY)?.takeIf { it.tagName == DICT_TAG }
            ?: return emptyMap()

        val parsedFrames = LinkedHashMap<String, SpriteFrame>()
        var child: Node? = framesDict.firstChild
        while (child != null) {
            if (child.nodeType == Node.ELEMENT_NODE && child.nodeName == KEY_TAG) {
                val spriteName = child.textContent.trim()
                val frameDict = nextElementSibling(child)
                if (spriteName.isNotEmpty() && frameDict != null && frameDict.tagName == DICT_TAG) {
                    parseFrameData(frameDict)?.let { parsedFrames[spriteName] = it }
                }
            }
            child = child.nextSibling
        }

        return parsedFrames.toMap()
    }

    fun loadAtlas(context: Context, plistPath: String, pngPath: String): Map<String, Bitmap> {
        val normalizedPlistPath = normalizeAssetPath(plistPath)
        val normalizedPngPath = normalizeAssetPath(pngPath)

        synchronized(atlasLock) {
            val sameAtlas = normalizedPlistPath == loadedPlistPath && normalizedPngPath == loadedPngPath
            if (!sameAtlas || atlasDecoder == null) {
                releaseAtlasLocked()

                val parsedFrames = context.assets.open(normalizedPlistPath).use(::parsePlist)
                val atlasBytes = context.assets.open(normalizedPngPath).use { it.readBytes() }
                val decoder = createRegionDecoder(atlasBytes, normalizedPngPath)

                frameIndex = parsedFrames.toMap()
                atlasDecoder = decoder
                loadedPlistPath = normalizedPlistPath
                loadedPngPath = normalizedPngPath
                atlasView = LazySpriteMap(frameIndex.keys.toSet())
            }

            return atlasView
        }
    }

    fun getSprite(name: String): Bitmap? {
        spriteCache[name]?.let { cached ->
            if (!cached.isRecycled) {
                return cached
            }
            spriteCache.remove(name, cached)
        }

        synchronized(atlasLock) {
            spriteCache[name]?.let { cached ->
                if (!cached.isRecycled) {
                    return cached
                }
                spriteCache.remove(name, cached)
            }

            val frame = frameIndex[name] ?: return null
            val decoder = atlasDecoder ?: return null
            val sprite = decodeSprite(decoder, frame) ?: return null

            spriteCache[name] = sprite
            return sprite
        }
    }

    private fun decodeSprite(decoder: BitmapRegionDecoder, frame: SpriteFrame): Bitmap? {
        if (frame.width <= 0 || frame.height <= 0) {
            return null
        }

        val left = frame.x.coerceIn(0, decoder.width)
        val top = frame.y.coerceIn(0, decoder.height)
        val right = (frame.x + frame.width).coerceIn(0, decoder.width)
        val bottom = (frame.y + frame.height).coerceIn(0, decoder.height)

        if (right <= left || bottom <= top) {
            return null
        }

        val decodedRegion = decoder.decodeRegion(
            Rect(left, top, right, bottom),
            BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
        ) ?: return null

        if (!frame.rotated) {
            return decodedRegion
        }

        val rotationMatrix = Matrix().apply {
            postRotate(-90f)
        }
        val corrected = Bitmap.createBitmap(
            decodedRegion,
            0,
            0,
            decodedRegion.width,
            decodedRegion.height,
            rotationMatrix,
            true
        )

        if (corrected !== decodedRegion && !decodedRegion.isRecycled) {
            decodedRegion.recycle()
        }

        return corrected
    }

    private fun parseFrameData(frameDict: Element): SpriteFrame? {
        val frameText = findValueElement(frameDict, FRAME_KEY)?.textContent?.trim() ?: return null
        val offsetText = findValueElement(frameDict, OFFSET_KEY)?.textContent?.trim().orEmpty()
        val rotatedElement = findValueElement(frameDict, ROTATED_KEY)

        val frameMatch = FRAME_PATTERN.matchEntire(frameText) ?: return null
        val offsetMatch = OFFSET_PATTERN.matchEntire(offsetText)

        val x = frameMatch.groupValues[1].toInt()
        val y = frameMatch.groupValues[2].toInt()
        val width = frameMatch.groupValues[3].toInt()
        val height = frameMatch.groupValues[4].toInt()

        val offsetX = offsetMatch?.groupValues?.get(1)?.toFloat() ?: 0f
        val offsetY = offsetMatch?.groupValues?.get(2)?.toFloat() ?: 0f

        return SpriteFrame(
            x = x,
            y = y,
            width = width,
            height = height,
            rotated = parseBooleanValue(rotatedElement),
            offsetX = offsetX,
            offsetY = offsetY
        )
    }

    private fun createRegionDecoder(atlasBytes: ByteArray, atlasPath: String): BitmapRegionDecoder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requireNotNull(BitmapRegionDecoder.newInstance(atlasBytes, 0, atlasBytes.size)) {
                "Failed to decode atlas at $atlasPath"
            }
        } else {
            @Suppress("DEPRECATION")
            requireNotNull(BitmapRegionDecoder.newInstance(atlasBytes, 0, atlasBytes.size, false)) {
                "Failed to decode atlas at $atlasPath"
            }
        }
    }

    private fun parseBooleanValue(valueElement: Element?): Boolean {
        if (valueElement == null) {
            return false
        }

        return when (valueElement.tagName) {
            TRUE_TAG -> true
            FALSE_TAG -> false
            STRING_TAG -> {
                val value = valueElement.textContent.trim()
                value.equals("true", ignoreCase = true) || value == "1"
            }
            INTEGER_TAG -> valueElement.textContent.trim() != "0"
            else -> false
        }
    }

    private fun normalizeAssetPath(path: String): String {
        val trimmed = path.trim().removePrefix("/")
        return if (trimmed.startsWith("assets/")) {
            trimmed.removePrefix("assets/")
        } else {
            trimmed
        }
    }

    private fun findValueElement(dictElement: Element, keyName: String): Element? {
        var child: Node? = dictElement.firstChild
        while (child != null) {
            if (child.nodeType == Node.ELEMENT_NODE && child.nodeName == KEY_TAG) {
                val currentKey = child.textContent.trim()
                if (currentKey == keyName) {
                    return nextElementSibling(child)
                }
            }
            child = child.nextSibling
        }
        return null
    }

    private fun firstChildElement(node: Node, tagName: String): Element? {
        var child: Node? = node.firstChild
        while (child != null) {
            if (child.nodeType == Node.ELEMENT_NODE && child.nodeName == tagName) {
                return child as Element
            }
            child = child.nextSibling
        }
        return null
    }

    private fun nextElementSibling(node: Node): Element? {
        var sibling: Node? = node.nextSibling
        while (sibling != null) {
            if (sibling.nodeType == Node.ELEMENT_NODE) {
                return sibling as Element
            }
            sibling = sibling.nextSibling
        }
        return null
    }

    private fun releaseAtlasLocked() {
        atlasDecoder?.recycle()
        atlasDecoder = null

        spriteCache.values.forEach { bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
        spriteCache.clear()

        frameIndex = emptyMap()
        atlasView = emptyMap()
        loadedPlistPath = null
        loadedPngPath = null
    }

    private inner class LazySpriteMap(
        private val spriteNames: Set<String>
    ) : AbstractMap<String, Bitmap>() {

        override val size: Int
            get() = spriteNames.size

        override fun containsKey(key: String): Boolean {
            return spriteNames.contains(key)
        }

        override fun get(key: String): Bitmap? {
            if (!spriteNames.contains(key)) {
                return null
            }
            return getSprite(key)
        }

        override val entries: Set<Map.Entry<String, Bitmap>>
            get() = spriteNames.mapNotNull { spriteName ->
                getSprite(spriteName)?.let { bitmap ->
                    java.util.AbstractMap.SimpleImmutableEntry(spriteName, bitmap)
                }
            }.toSet()
    }

    companion object {
        private const val LEGACY_PLIST_DOCTYPE =
            "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\"/>"
        private const val DICT_TAG = "dict"
        private const val KEY_TAG = "key"
        private const val STRING_TAG = "string"
        private const val INTEGER_TAG = "integer"
        private const val TRUE_TAG = "true"
        private const val FALSE_TAG = "false"

        private const val FRAMES_KEY = "frames"
        private const val FRAME_KEY = "frame"
        private const val OFFSET_KEY = "offset"
        private const val ROTATED_KEY = "rotated"

        private val FRAME_PATTERN = Regex(
            """\{\{\s*(-?\d+)\s*,\s*(-?\d+)\s*},\s*\{\s*(\d+)\s*,\s*(\d+)\s*}}"""
        )
        private val OFFSET_PATTERN = Regex(
            """\{\s*(-?\d+(?:\.\d+)?)\s*,\s*(-?\d+(?:\.\d+)?)\s*}"""
        )
    }
}
