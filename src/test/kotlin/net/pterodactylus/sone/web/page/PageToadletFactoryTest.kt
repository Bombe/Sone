package net.pterodactylus.sone.web.page

import freenet.client.HighLevelSimpleClient
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.web.Page
import net.pterodactylus.util.web.Response
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test

private val highLevelSimpleClient = mock<HighLevelSimpleClient>()
private const val pathPrefix = "/some/prefix/"

class PageToadletFactoryTest {

	private val pageToadletFactory = PageToadletFactory(highLevelSimpleClient, pathPrefix)

	@Test
	fun `page toadlet without menu name is created without menu name`() {
		val page = mock<Page<FreenetRequest>>()
		val pageToadlet = pageToadletFactory.createPageToadlet(page)
		assertThat(pageToadlet.menuName, nullValue())
	}

	@Test
	fun `page toadlet with menu name is created with menu name`() {
		val page = mock<Page<FreenetRequest>>()
		val pageToadlet = pageToadletFactory.createPageToadlet(page, "testName")
		assertThat(pageToadlet.menuName, equalTo("testName"))
	}

	@Test
	fun `path prefix is handed down correctly`() {
		val page = mock<Page<FreenetRequest>>().apply {
			whenever(path).thenReturn("path")
		}
		val pageToadlet = pageToadletFactory.createPageToadlet(page)
		assertThat(pageToadlet.path(), equalTo("/some/prefix/path"))
	}

	@Test
	fun `menu name is added from annotation when no menu name is given`() {
		val page = TestPageWithMenuName()
		val pageToadlet = pageToadletFactory.createPageToadlet(page)
		assertThat(pageToadlet.menuName, equalTo("testName"))
	}

	@Test
	fun `menu name from annotation is ignored when menu name is given`() {
		val page = TestPageWithMenuName()
		val pageToadlet = pageToadletFactory.createPageToadlet(page, "foo")
		assertThat(pageToadlet.menuName, equalTo("foo"))
	}

}

@MenuName("testName")
private class TestPageWithMenuName : Page<FreenetRequest> {

	override fun getPath() = ""
	override fun isPrefixPage() = false
	override fun handleRequest(request: FreenetRequest, response: Response) = response

}
