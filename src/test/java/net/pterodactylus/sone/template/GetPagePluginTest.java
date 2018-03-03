package net.pterodactylus.sone.template;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.template.TemplateContext;

import freenet.support.api.HTTPRequest;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link GetPagePlugin}.
 */
public class GetPagePluginTest {

	private final GetPagePlugin plugin = new GetPagePlugin();
	private final TemplateContext context = mock(TemplateContext.class);
	private final FreenetRequest request = mock(FreenetRequest.class);
	private final Map<String, String> parameters =
			new HashMap<String, String>();
	private HTTPRequest httpRequest = mock(HTTPRequest.class);

	@Before
	public void setupTemplateContext() {
		when(context.get("request")).thenReturn(request);
		when(request.getHttpRequest()).thenReturn(httpRequest);
		when(httpRequest.getParam("page")).thenReturn("1");
	}

	@Test
	public void fullySpecifiedPluginCallSetsCorrectValue() {
		parameters.put("request", "request");
		parameters.put("parameter", "page");
		parameters.put("key", "page-key");
		plugin.execute(context, parameters);
		verify(context).set("page-key", 1);
	}

	@Test
	public void missingRequestParameterStillSetsCorrectValue() {
		parameters.put("parameter", "page");
		parameters.put("key", "page-key");
		plugin.execute(context, parameters);
		verify(context).set("page-key", 1);
	}

	@Test
	public void missingParameterParameterStillSetsCorrectValue() {
		parameters.put("request", "request");
		parameters.put("key", "page-key");
		plugin.execute(context, parameters);
		verify(context).set("page-key", 1);
	}

	@Test
	public void missingKeyParameterStillSetsCorrectValue() {
		parameters.put("request", "request");
		parameters.put("parameter", "page");
		plugin.execute(context, parameters);
		verify(context).set("page", 1);
	}

	@Test
	public void unparseablePageSetsPageZero() {
		parameters.put("parameter", "wrong-parameter");
		plugin.execute(context, parameters);
		verify(context).set("page", 0);
	}

}
