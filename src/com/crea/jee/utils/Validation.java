package com.crea.jee.utils;

/*
 * Cette classe regroupe les contrôles de validité appliqués aux données des beans
 * avant tout accès à la base de données (blindage des saisies)
 */
public class Validation {

	private Validation() {
	}

	// vérifie qu'une chaîne est renseignée (non nulle et non vide une fois les espaces retirés)
	public static boolean estRenseigne(String valeur) {
		return valeur != null && !valeur.trim().isEmpty();
	}

	// vérifie qu'une chaîne ne dépasse pas la longueur maximale autorisée en base (varchar(longueurMax))
	public static boolean longueurValide(String valeur, int longueurMax) {
		return valeur != null && valeur.length() <= longueurMax;
	}

	// vérifie qu'une chaîne est renseignée et ne dépasse pas la longueur maximale autorisée en base
	public static boolean estValide(String valeur, int longueurMax) {
		return estRenseigne(valeur) && longueurValide(valeur, longueurMax);
	}

	// vérifie qu'un entier est strictement positif
	public static boolean estPositif(int valeur) {
		return valeur > 0;
	}

	// vérifie qu'un nombre décimal est strictement positif
	public static boolean estPositif(float valeur) {
		return valeur > 0;
	}

	// vérifie qu'un entier tient dans la plage d'un tinyint MySQL (-128 à 127)
	public static boolean estDansPlageTinyint(int valeur) {
		return valeur >= -128 && valeur <= 127;
	}

}
