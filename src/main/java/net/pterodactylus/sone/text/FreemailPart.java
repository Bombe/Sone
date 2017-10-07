package net.pterodactylus.sone.text;

/**
 * {@link Part} implementation that holds a freemail address.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FreemailPart implements Part {

	private final String emailLocalPart;
	private final String freemailId;
	private final String identityId;

	public FreemailPart(String emailLocalPart, String freemailId, String identityId) {
		this.emailLocalPart = emailLocalPart;
		this.freemailId = freemailId;
		this.identityId = identityId;
	}

	@Override
	public String getText() {
		return String.format("%s@%s.freemail", emailLocalPart, freemailId);
	}

	public String getEmailLocalPart() {
		return emailLocalPart;
	}

	public String getFreemailId() {
		return freemailId;
	}

	public String getIdentityId() {
		return identityId;
	}

}
