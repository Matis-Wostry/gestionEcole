package com.crea.jee.wrappers;

/*
 * Base commune des wrappers renvoyés par les DAO lors de la lecture d'un objet unique
 * Un DAO qui renverrait directement un bean ne pourrait pas distinguer "objet non trouvé" (null)
 * de "erreur côté serveur" (null aussi) : le wrapper accompagne l'objet d'un code réponse
 */
public abstract class Wrapper {

	/*
	 * Codes réponse
	 * TROUVE = l'objet a été trouvé, il est disponible dans le wrapper
	 * NON_TROUVE = aucun objet ne correspond, l'objet du wrapper est null
	 * ERREUR_BASE = erreur côté serveur (connexion impossible ou requête en échec), l'objet est null
	 * DONNEES_INVALIDES = paramètre de recherche invalide, refusé avant tout accès à la base, l'objet est null
	 * (le -2 est réservé au doublon de clé, qui ne peut pas se produire lors d'une lecture)
	 */
	public static final int TROUVE = 1;
	public static final int NON_TROUVE = 0;
	public static final int ERREUR_BASE = -1;
	public static final int DONNEES_INVALIDES = -3;

	private final int codeResponse;

	/*
	 * Constructeur
	 */

	// construit un wrapper avec son code réponse
	protected Wrapper(int codeResponse) {
		this.codeResponse = codeResponse;
	}

	/*
	 * Getter
	 */

	// retourne le code réponse
	public int getCodeResponse() {
		return codeResponse;
	}

}
