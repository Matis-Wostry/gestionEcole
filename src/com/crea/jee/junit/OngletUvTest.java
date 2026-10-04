package com.crea.jee.junit;

import static com.crea.jee.junit.OutilsIhm.cliquer;
import static com.crea.jee.junit.OutilsIhm.dernierDialogue;
import static com.crea.jee.junit.OutilsIhm.filtrer;
import static com.crea.jee.junit.OutilsIhm.lignes;
import static com.crea.jee.junit.OutilsIhm.saisir;
import static com.crea.jee.junit.OutilsIhm.selection;
import static com.crea.jee.junit.OutilsIhm.selectionner;
import static com.crea.jee.junit.OutilsIhm.valeur;
import static com.crea.jee.junit.OutilsIhm.verifierStatut;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.crea.jee.beans.Uv;
import com.crea.jee.dao.UvDao;
import com.crea.jee.ihm.FenetreEcole;

/*
 * Tests de l'onglet UV de l'interface, contre la base ecole_test (-Ddb.name=ecole_test)
 * [OK] = cas valide, l'action doit réussir ; [ERREUR] = cas invalide, l'interface doit l'afficher clairement
 */
@DisplayName("Interface : onglet UV")
class OngletUvTest {

	private static final int FILTRE_TOUTES = 0;
	private static final int FILTRE_HEURES = 1;

	private FenetreEcole fenetre;
	private Object onglet;

	@BeforeEach
	void ouvrir() throws Exception {
		assumeFalse(GraphicsEnvironment.isHeadless(), "Tests d'interface ignorés : aucun écran disponible");
		BaseDeTest.viderLesTables();
		fenetre = OutilsIhm.ouvrir();
		onglet = OutilsIhm.onglet(fenetre, 3);
	}

	@AfterEach
	void fermer() throws Exception {
		OutilsIhm.fermer(fenetre);
		BaseDeTest.viderLesTables();
	}

	private void creerUv(String code, int heures, String coordinateur) throws Exception {
		UvDao.addUv(new Uv(code, heures, coordinateur));
		cliquer(onglet, "actualiser");
	}

	@Test
	@DisplayName("[OK] Ajouter une UV, avec ou sans coordinateur (code 1)")
	void ajouter_valide() throws Exception {
		saisir(onglet, "champCode", "UV1", "champHeures", "30", "champCoordinateur", "Mr Test");
		cliquer(onglet, "ajouter");
		verifierStatut(onglet, "UV UV1 ajoutée (code 1)");
		assertEquals("UV1", selection(onglet));

		saisir(onglet, "champCode", "UV2", "champHeures", "12", "champCoordinateur", "");
		cliquer(onglet, "ajouter");
		verifierStatut(onglet, "(code 1)");
		assertEquals(2, lignes(onglet));
	}

	@Test
	@DisplayName("[ERREUR] Ajouter un code déjà existant (code -2), un code vide, 0 ou 200 heures (code -3)")
	void ajouter_casInvalides() throws Exception {
		creerUv("UV1", 30, "Mr Test");

		saisir(onglet, "champCode", "UV1", "champHeures", "10", "champCoordinateur", "");
		cliquer(onglet, "ajouter");
		verifierStatut(onglet, "(code -2)");

		String[][] saisies = { { "", "10" }, { "UV3", "0" }, { "UV3", "200" } };
		for (String[] saisie : saisies) {
			saisir(onglet, "champCode", saisie[0], "champHeures", saisie[1], "champCoordinateur", "");
			cliquer(onglet, "ajouter");
			verifierStatut(onglet, "(code -3)");
		}
		assertEquals(1, lignes(onglet));
	}

	@Test
	@DisplayName("[OK] Rechercher une UV existante (code 1) ; [ERREUR] une UV inconnue (code 0)")
	void rechercher() throws Exception {
		creerUv("UV1", 30, "Mr Test");

		saisir(onglet, "champRecherche", "UV1");
		cliquer(onglet, "rechercher");
		verifierStatut(onglet, "UV UV1 trouvée (code 1)");
		assertTrue(dernierDialogue().contains("Nombre d'heures : 30"));

		saisir(onglet, "champRecherche", "INCONNUE");
		cliquer(onglet, "rechercher");
		verifierStatut(onglet, "(code 0)");
	}

	@Test
	@DisplayName("[OK] Modifier les heures (code 1) ; [ERREUR] 0 heure est refusé")
	void modifierHeures() throws Exception {
		creerUv("UV1", 30, "Mr Test");
		selectionner(onglet, "UV1");

		saisir(onglet, "champNouvellesHeures", "45");
		cliquer(onglet, "modifierHeures");
		verifierStatut(onglet, "Nombre d'heures de l'UV UV1 modifié (code 1)");
		assertEquals("45", valeur(onglet, "UV1", 1));

		selectionner(onglet, "UV1");
		saisir(onglet, "champNouvellesHeures", "0");
		cliquer(onglet, "modifierHeures");
		verifierStatut(onglet, "Nombre d'heures invalide");
		assertEquals("45", valeur(onglet, "UV1", 1));
	}

	@Test
	@DisplayName("[OK] Modifier le coordinateur (code 1) ; [ERREUR] plus de 255 caractères est refusé")
	void modifierCoordinateur() throws Exception {
		creerUv("UV1", 30, "Mr Test");
		selectionner(onglet, "UV1");

		saisir(onglet, "champNouveauCoordinateur", "Mme Nouvelle");
		cliquer(onglet, "modifierCoordinateur");
		verifierStatut(onglet, "Coordinateur de l'UV UV1 modifié (code 1)");
		assertEquals("Mme Nouvelle", valeur(onglet, "UV1", 2));

		selectionner(onglet, "UV1");
		saisir(onglet, "champNouveauCoordinateur", "x".repeat(256));
		cliquer(onglet, "modifierCoordinateur");
		verifierStatut(onglet, "Coordinateur invalide");
		assertEquals("Mme Nouvelle", valeur(onglet, "UV1", 2));
	}

	@Test
	@DisplayName("[OK] Filtre « plus d'heures que » : seules les UV au-dessus du seuil restent")
	void filtreHeures() throws Exception {
		creerUv("UV1", 30, "Mr Test");
		creerUv("UV2", 12, "Mme Test");

		saisir(onglet, "champSeuil", "20");
		filtrer(onglet, FILTRE_HEURES);
		assertEquals(1, lignes(onglet));
		assertEquals("30", valeur(onglet, "UV1", 1));

		filtrer(onglet, FILTRE_TOUTES);
		assertEquals(2, lignes(onglet));
	}

	@Test
	@DisplayName("[OK] Supprimer l'UV sélectionnée après confirmation (code 1)")
	void supprimer() throws Exception {
		creerUv("UV1", 30, "Mr Test");
		selectionner(onglet, "UV1");

		cliquer(onglet, "supprimerSelection");

		verifierStatut(onglet, "UV UV1 supprimée avec ses inscriptions (code 1)");
		assertEquals(0, lignes(onglet));
		assertTrue(dernierDialogue().contains("Toutes les inscriptions à cette UV seront supprimées"));
	}

}
