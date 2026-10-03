package com.crea.jee.wrappers;

import com.crea.jee.beans.Chambre;

/*
 * Cette classe enveloppe une chambre renvoyée par ChambreDao avec son code réponse (voir Wrapper)
 */
public class ChambreWrapper extends Wrapper {

	private final Chambre chambre;

	/*
	 * Constructeur
	 */

	// construit le wrapper avec la chambre (null si elle n'a pas été trouvée ou en cas d'erreur) et le code réponse
	public ChambreWrapper(Chambre chambre, int codeResponse) {
		super(codeResponse);
		this.chambre = chambre;
	}

	/*
	 * Getter
	 */

	// retourne la chambre, ou null si le code réponse n'est pas TROUVE
	public Chambre getChambre() {
		return chambre;
	}

}
