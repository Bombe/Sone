package net.pterodactylus.sone.data.impl

import net.pterodactylus.sone.data.Image
import org.junit.Test

/**
 * Unit test for [ImageImpl].
 */
class ImageImplTest {

    private val image = ImageImpl()

    @Test(expected = Image.Modifier.ImageTitleMustNotBeEmpty::class)
    fun `modifier does not allow title to be empty`() {
        image.modify().setTitle("").update()
    }

    @Test(expected = IllegalStateException::class)
    fun `album cannot be changed to album of different Sone`() {
        val sone1 = IdOnlySone("Sone1")
        val sone2 = IdOnlySone("Sone2")
        image.modify().setSone(sone1).update()
        val album = AlbumImpl(sone2)
        image.album = album
     }

}
