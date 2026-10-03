package com.crea.jee.wrappers;

import com.crea.jee.beans.Livre;

/*
 * Cette classe enveloppe un livre renvoyé par LivreDao avec son code réponse (voir Wrapper)
 */
public class LivreWrapper extends Wrapper {

	private final Livre livre;

	/*
	 * Constructeur
	 */

	// construit le wrapper avec le livre (null s'il n'a pas été trouvé ou en cas d'erreur) et le code réponse
	public LivreWrapper(Livre livre, int codeResponse) {
		super(codeResponse);
		this.livre = livre;
	}

	/*
	 * Getter
	 */

	// retourne le livre, ou null si le code réponse n'est pas TROUVE
	public Livre getLivre() {
		return livre;
	}

}
