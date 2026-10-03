package com.crea.jee.wrappers;

import com.crea.jee.beans.Uv;

/*
 * Cette classe enveloppe une uv renvoyée par UvDao avec son code réponse (voir Wrapper)
 */
public class UvWrapper extends Wrapper {

	private final Uv uv;

	/*
	 * Constructeur
	 */

	// construit le wrapper avec l'uv (null si elle n'a pas été trouvée ou en cas d'erreur) et le code réponse
	public UvWrapper(Uv uv, int codeResponse) {
		super(codeResponse);
		this.uv = uv;
	}

	/*
	 * Getter
	 */

	// retourne l'uv, ou null si le code réponse n'est pas TROUVE
	public Uv getUv() {
		return uv;
	}

}
