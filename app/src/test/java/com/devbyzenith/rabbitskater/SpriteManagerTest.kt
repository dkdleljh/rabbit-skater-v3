package com.devbyzenith.rabbitskater

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpriteManagerTest {
    @Test
    fun `all bundled legacy atlases parse without modifying original files`() {
        val assetsDir = File(requireNotNull(System.getProperty("rabbitSkater.assetsDir")))
        val atlases = requireNotNull(File(assetsDir, "atlases").listFiles())
            .filter { it.extension == "plist" }
        assertEquals(5, atlases.size)
        for (atlas in atlases) {
            val frames = SpriteManager().parsePlist(atlas.inputStream())
            assertFalse("No sprite frames in ${atlas.name}", frames.isEmpty())
        }

        val mainFrames = SpriteManager().parsePlist(
            File(assetsDir, "atlases/atlas_ID107.plist").inputStream()
        )
        assertEquals(151, mainFrames.size)
        for (id in listOf(2014, 2029, 2506, 2511, 2519, 2020)) {
            assertTrue("Missing game sprite $id", mainFrames.containsKey("PTModelSprite_ID$id"))
        }
        val background = requireNotNull(mainFrames["PTModelSprite_ID2014"])
        assertEquals(1136, background.width)
        assertEquals(640, background.height)
    }

    @Test
    fun `ordinary XML plist still parses`() {
        val xml = """<?xml version="1.0" encoding="UTF-8"?>
            <plist><dict><key>frames</key><dict><key>sprite</key><dict>
            <key>frame</key><string>{{2, 3}, {10, 20}}</string>
            <key>rotated</key><true/>
            </dict></dict></dict></plist>""".trimIndent()
        val frame = requireNotNull(SpriteManager().parsePlist(xml.byteInputStream())["sprite"])
        assertEquals(2, frame.x)
        assertEquals(20, frame.height)
        assertTrue(frame.rotated)
    }
}
