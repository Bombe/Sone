package net.pterodactylus.sone.web.ajax

import net.pterodactylus.sone.data.Album
import net.pterodactylus.sone.data.Image
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.data.impl.ImageImpl
import net.pterodactylus.sone.template.ParserFilter
import net.pterodactylus.sone.template.RenderFilter
import net.pterodactylus.sone.template.ShortenFilter
import net.pterodactylus.sone.test.argumentCaptor
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.verify

/**
 * Unit test for [EditImageAjaxPage].
 */
class EditImageAjaxPageTest : JsonPageTest("editImage.ajax") {

	private val parserFilter = mock<ParserFilter>()
	private val shortenFilter = mock<ShortenFilter>()
	private val renderFilter = mock<RenderFilter>()
	override val page: JsonPage get() = EditImageAjaxPage(webInterface, parserFilter, shortenFilter, renderFilter)

	@Test
	fun `request without image results in invalid-image-id`() {
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-image-id"))
	}

	@Test
	fun `request with non-local image results in not-authorized`() {
		val image = mock<Image>()
		val sone = mock<Sone>()
		whenever(image.sone).thenReturn(sone)
		addImage(image, "image-id")
		addRequestParameter("image", "image-id")
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("not-authorized"))
	}

	@Test
	fun `moving an image to the left returns the correct values`() {
		val image = mock<Image>().apply { whenever(id).thenReturn("image-id") }
		val sone = mock<Sone>().apply { whenever(isLocal).thenReturn(true) }
		whenever(image.sone).thenReturn(sone)
		val swapped = mock<Image>().apply { whenever(id).thenReturn("swapped") }
		val album = mock<Album>()
		whenever(album.moveImageUp(image)).thenReturn(swapped)
		whenever(image.album).thenReturn(album)
		addImage(image)
		addRequestParameter("image", "image-id")
		addRequestParameter("moveLeft", "true")
		assertThat(json.isSuccess, equalTo(true))
		assertThat(json["sourceImageId"]?.asText(), equalTo("image-id"))
		assertThat(json["destinationImageId"]?.asText(), equalTo("swapped"))
		verify(core).touchConfiguration()
	}

	@Test
	fun `moving an image to the right returns the correct values`() {
		val image = mock<Image>().apply { whenever(id).thenReturn("image-id") }
		val sone = mock<Sone>().apply { whenever(isLocal).thenReturn(true) }
		whenever(image.sone).thenReturn(sone)
		val swapped = mock<Image>().apply { whenever(id).thenReturn("swapped") }
		val album = mock<Album>()
		whenever(album.moveImageDown(image)).thenReturn(swapped)
		whenever(image.album).thenReturn(album)
		addImage(image)
		addRequestParameter("image", "image-id")
		addRequestParameter("moveRight", "true")
		assertThat(json.isSuccess, equalTo(true))
		assertThat(json["sourceImageId"]?.asText(), equalTo("image-id"))
		assertThat(json["destinationImageId"]?.asText(), equalTo("swapped"))
		verify(core).touchConfiguration()
	}

	@Test
	fun `request with empty title results in invalid-image-title`() {
		val image = mock<Image>().apply { whenever(id).thenReturn("image-id") }
		val sone = mock<Sone>().apply { whenever(isLocal).thenReturn(true) }
		whenever(image.sone).thenReturn(sone)
		addImage(image)
		addRequestParameter("image", "image-id")
		assertThat(json.isSuccess, equalTo(false))
		assertThat(json.error, equalTo("invalid-image-title"))
	}

	@Test
	fun `request with title and description returns correct values`() {
		val sone = mock<Sone>().apply { whenever(isLocal).thenReturn(true) }
		val image = ImageImpl("image-id").modify().setSone(sone).update()
		val parsed = Object()
		val shortened = Object()
		val rendered = "rendered description"
		whenever(parserFilter.format(any(), eq("some KSK@foo link"), any())).thenReturn(parsed)
		whenever(shortenFilter.format(any(), eq(parsed), any())).thenReturn(shortened)
		whenever(renderFilter.format(any(), eq(shortened), any())).thenReturn(rendered)
		addImage(image)
		addRequestParameter("image", "image-id")
		addRequestParameter("title", "some title")
		addRequestParameter("description", "some http://127.0.0.1:8888/KSK@foo link")
		addRequestHeader("Host", "127.0.0.1:8888")
		assertThat(json.isSuccess, equalTo(true))
		assertThat(json["title"]?.asText(), equalTo("some title"))
		assertThat(json["description"]?.asText(), equalTo("some KSK@foo link"))
		assertThat(json["parsedDescription"]?.asText(), equalTo("rendered description"))
		verify(core).touchConfiguration()
		val parameterCaptor = argumentCaptor<MutableMap<String, Any?>>()
		verify(parserFilter).format(any(), any(), parameterCaptor.capture())
		assertThat(parameterCaptor.value["sone"], equalTo<Any>(sone))
	}

}
