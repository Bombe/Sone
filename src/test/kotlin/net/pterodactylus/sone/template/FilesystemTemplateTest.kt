package net.pterodactylus.sone.template

import net.pterodactylus.util.template.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.junit.rules.*
import java.io.*
import java.lang.Thread.*
import kotlin.test.Test

/**
 * Unit test for [FilesystemTemplate].
 */
class FilesystemTemplateTest() {

	@Rule
	@JvmField
	val expectedException: ExpectedException = ExpectedException.none()

	private val tempFile = File.createTempFile("template-", ".dat")
	private val filesystemTemplate: FilesystemTemplate
	private val templateContext = TemplateContext()

	private val renderedString: String
		get() =
			StringWriter().use { stringWriter ->
				filesystemTemplate.render(templateContext, stringWriter)
				stringWriter
			}.toString()

	init {
		writeTemplate("Text")
		filesystemTemplate = FilesystemTemplate(tempFile.absolutePath)
	}

	private fun writeTemplate(text: String) {
		tempFile.writer().use {
			it.write("$text.<%foreach values value><% value><%/foreach>")
		}
	}

	@Before
	fun setupTemplateContext() {
		templateContext.set("values", listOf("a", 1))
	}

	@Test
	fun `loading template from non existing file throws exception`() {
		val filesystemTemplate = FilesystemTemplate("/a/b/c.dat")
		expectedException.expect(FilesystemTemplate.TemplateFileNotFoundException::class.java)
		filesystemTemplate.initialContext
	}

	@Test
	fun `template can be loaded from the filesystem`() {
		assertThat(renderedString, equalTo("Text.a1"))
	}

	@Test
	fun `template can be reloaded`() {
		assertThat(renderedString, equalTo("Text.a1"))
		sleep(1000)
		writeTemplate("New")
		assertThat(renderedString, equalTo("New.a1"))
	}

	@Test
	fun `template is not reloaded if not changed`() {
		assertThat(renderedString, equalTo("Text.a1"))
		assertThat(renderedString, equalTo("Text.a1"))
	}

	@Test
	fun `initial context is copied to reloaded templates`() {
		filesystemTemplate.initialContext.set("values", "test")
		sleep(1000)
		writeTemplate("New")
		assertThat(filesystemTemplate.initialContext.get("values"), equalTo("test" as Any))
	}

	@Test
	fun `parts are copied to currently loaded templates`() {
		writeTemplate("New")
		renderedString
		filesystemTemplate.add { _, writer ->
			writer.write(".Test")
		}
		assertThat(renderedString, equalTo("New.a1.Test"))
	}

	@Test
	fun `parts are copied to reloaded templates`() {
		filesystemTemplate.add { _, writer ->
			writer.write(".Test")
		}
		sleep(1000)
		writeTemplate("New")
		assertThat(renderedString, equalTo("New.a1.Test"))
	}

	@Test
	fun `column of returned template is returned as zero`() {
		assertThat(filesystemTemplate.column, equalTo(0))
	}

	@Test
	fun `line of returned template is returned as zero`() {
		assertThat(filesystemTemplate.line, equalTo(0))
	}

	@Test
	fun `template can be iterated over`() {
		assertThat<Iterator<Part>>(filesystemTemplate.iterator(), notNullValue())
	}

}
