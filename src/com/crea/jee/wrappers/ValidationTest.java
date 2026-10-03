package com.crea.jee.wrappers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.crea.jee.utils.Validation;

/*
 * Tests unitaires de Validation (aucun accès à la base)
 */
class ValidationTest {

	@Test
	void estRenseigne() {
		assertTrue(Validation.estRenseigne("abc"));
		assertFalse(Validation.estRenseigne(null));
		assertFalse(Validation.estRenseigne(""));
		assertFalse(Validation.estRenseigne("   "));
	}

	@Test
	void longueurValide() {
		assertTrue(Validation.longueurValide("", 5));
		assertTrue(Validation.longueurValide("abcde", 5));
		assertFalse(Validation.longueurValide("abcdef", 5));
		assertFalse(Validation.longueurValide(null, 5));
	}

	@Test
	void estValide() {
		assertTrue(Validation.estValide("abc", 5));
		assertFalse(Validation.estValide("", 5));
		assertFalse(Validation.estValide("abcdef", 5));
		assertFalse(Validation.estValide(null, 5));
	}

	@Test
	void estPositif_entier() {
		assertTrue(Validation.estPositif(1));
		assertFalse(Validation.estPositif(0));
		assertFalse(Validation.estPositif(-1));
	}

	@Test
	void estPositif_decimal() {
		assertTrue(Validation.estPositif(0.01f));
		assertFalse(Validation.estPositif(0f));
		assertFalse(Validation.estPositif(-0.5f));
	}

	@Test
	void estDansPlageTinyint() {
		assertTrue(Validation.estDansPlageTinyint(-128));
		assertTrue(Validation.estDansPlageTinyint(127));
		assertFalse(Validation.estDansPlageTinyint(-129));
		assertFalse(Validation.estDansPlageTinyint(128));
	}

}
