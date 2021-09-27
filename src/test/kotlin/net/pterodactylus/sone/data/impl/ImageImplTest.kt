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

}
