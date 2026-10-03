package com.crea.jee.wrappers;

import com.crea.jee.beans.Eleve;

/*
 * Cette classe enveloppe un élève renvoyé par EleveDao avec son code réponse (voir Wrapper)
 */
public class EleveWrapper extends Wrapper {

	private final Eleve eleve;

	/*
	 * Constructeur
	 */

	// construit le wrapper avec l'élève (null s'il n'a pas été trouvé ou en cas d'erreur) et le code réponse
	public EleveWrapper(Eleve eleve, int codeResponse) {
		super(codeResponse);
		this.eleve = eleve;
	}

	/*
	 * Getter
	 */

	// retourne l'élève, ou null si le code réponse n'est pas TROUVE
	public Eleve getEleve() {
		return eleve;
	}

}
