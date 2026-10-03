package com.crea.jee.junit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.crea.jee.utils.Validation;

/*
 * Tests unitaires de Validation (aucun accès à la base)
 * [OK] = valeur valide, le contrôle doit l'accepter ; [ERREUR] = valeur invalide, le contrôle doit la refuser
 */
@DisplayName("Validation")
class ValidationTest {

	@Test
	@DisplayName("[OK] estRenseigne : un texte non vide est accepté")
	void estRenseigne_valide() {
		assertTrue(Validation.estRenseigne("abc"));
	}

	@Test
	@DisplayName("[ERREUR] estRenseigne : null, chaîne vide ou uniquement des espaces sont refusés")
	void estRenseigne_invalide() {
		assertFalse(Validation.estRenseigne(null));
		assertFalse(Validation.estRenseigne(""));
		assertFalse(Validation.estRenseigne("   "));
	}

	@Test
	@DisplayName("[OK] longueurValide : un texte jusqu'à la longueur maximale (vide compris) est accepté")
	void longueurValide_valide() {
		assertTrue(Validation.longueurValide("", 5));
		assertTrue(Validation.longueurValide("abcde", 5));
	}

	@Test
	@DisplayName("[ERREUR] longueurValide : un texte trop long ou null est refusé")
	void longueurValide_invalide() {
		assertFalse(Validation.longueurValide("abcdef", 5));
		assertFalse(Validation.longueurValide(null, 5));
	}

	@Test
	@DisplayName("[OK] estValide : un texte renseigné et pas trop long est accepté")
	void estValide_valide() {
		assertTrue(Validation.estValide("abc", 5));
	}

	@Test
	@DisplayName("[ERREUR] estValide : un texte vide, trop long ou null est refusé")
	void estValide_invalide() {
		assertFalse(Validation.estValide("", 5));
		assertFalse(Validation.estValide("abcdef", 5));
		assertFalse(Validation.estValide(null, 5));
	}

	@Test
	@DisplayName("[OK] estPositif (entier) : un entier strictement positif est accepté")
	void estPositifEntier_valide() {
		assertTrue(Validation.estPositif(1));
	}

	@Test
	@DisplayName("[ERREUR] estPositif (entier) : 0 et les négatifs sont refusés")
	void estPositifEntier_invalide() {
		assertFalse(Validation.estPositif(0));
		assertFalse(Validation.estPositif(-1));
	}

	@Test
	@DisplayName("[OK] estPositif (décimal) : un décimal strictement positif est accepté")
	void estPositifDecimal_valide() {
		assertTrue(Validation.estPositif(0.01f));
	}

	@Test
	@DisplayName("[ERREUR] estPositif (décimal) : 0 et les négatifs sont refusés")
	void estPositifDecimal_invalide() {
		assertFalse(Validation.estPositif(0f));
		assertFalse(Validation.estPositif(-0.5f));
	}

	@Test
	@DisplayName("[OK] estDansPlageTinyint : les bornes -128 et 127 sont acceptées")
	void estDansPlageTinyint_valide() {
		assertTrue(Validation.estDansPlageTinyint(-128));
		assertTrue(Validation.estDansPlageTinyint(127));
	}

	@Test
	@DisplayName("[ERREUR] estDansPlageTinyint : -129 et 128, juste hors des bornes, sont refusés")
	void estDansPlageTinyint_invalide() {
		assertFalse(Validation.estDansPlageTinyint(-129));
		assertFalse(Validation.estDansPlageTinyint(128));
	}

}
