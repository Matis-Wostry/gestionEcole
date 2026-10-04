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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.crea.jee.beans.Eleve;
import com.crea.jee.beans.Livre;
import com.crea.jee.dao.EleveDao;
import com.crea.jee.dao.LivreDao;
import com.crea.jee.ihm.FenetreEcole;

/*
 * Tests de l'onglet Livres de l'interface, contre la base ecole_test (-Ddb.name=ecole_test)
 * [OK] = cas valide, l'action doit réussir ; [ERREUR] = cas invalide, l'interface doit l'afficher clairement
 */
@DisplayName("Interface : onglet Livres")
class OngletLivresTest {

	private static final int FILTRE_TOUS = 0;
	private static final int FILTRE_DISPONIBLES = 1;
	private static final int FILTRE_EMPRUNTES_PAR = 2;

	private FenetreEcole fenetre;
	private Object onglet;

	@BeforeEach
	void ouvrir() throws Exception {
		assumeFalse(GraphicsEnvironment.isHeadless(), "Tests d'interface ignorés : aucun écran disponible");
		BaseDeTest.viderLesTables();
		EleveDao.addEleve(new Eleve("TEST1", 0, "Alice Test", 20, "Adresse"));
		fenetre = OutilsIhm.ouvrir();
		onglet = OutilsIhm.onglet(fenetre, 2);
	}

	@AfterEach
	void fermer() throws Exception {
		OutilsIhm.fermer(fenetre);
		BaseDeTest.viderLesTables();
	}

	// crée des livres (cote, titre, cote, titre...) directement par le DAO puis recharge l'onglet
	private void creerLivres(String... cotesEtTitres) throws Exception {
		for (int i = 0; i < cotesEtTitres.length; i += 2) {
			LivreDao.addLivre(new Livre(cotesEtTitres[i], null, cotesEtTitres[i + 1], null));
		}
		cliquer(onglet, "actualiser");
	}

	@Test
	@DisplayName("[OK] Ajouter un livre : il apparaît, disponible et sélectionné (code 1)")
	void ajouter_valide() throws Exception {
		saisir(onglet, "champCote", "L1", "champTitre", "Le Petit Prince");
		cliquer(onglet, "ajouter");

		verifierStatut(onglet, "Livre L1 ajouté (code 1)");
		assertEquals("L1", selection(onglet));
		assertEquals("—", valeur(onglet, "L1", 2));
	}

	@Test
	@DisplayName("[ERREUR] Ajouter une cote déjà existante (code -2) ou sans titre (code -3)")
	void ajouter_casInvalides() throws Exception {
		creerLivres("L1", "Le Petit Prince");

		saisir(onglet, "champCote", "L1", "champTitre", "Autre");
		cliquer(onglet, "ajouter");
		verifierStatut(onglet, "(code -2)");

		saisir(onglet, "champCote", "L2", "champTitre", "");
		cliquer(onglet, "ajouter");
		verifierStatut(onglet, "(code -3)");

		assertEquals(1, lignes(onglet));
	}

	@Test
	@DisplayName("[OK] Rechercher un livre existant : fiche affichée avec le titre (code 1)")
	void rechercher_existant() throws Exception {
		creerLivres("L1", "Le Petit Prince", "L2", "Germinal");

		saisir(onglet, "champRecherche", "L1");
		cliquer(onglet, "rechercher");

		verifierStatut(onglet, "Livre L1 trouvé (code 1)");
		assertTrue(dernierDialogue().contains("Titre : Le Petit Prince"));
	}

	@Test
	@DisplayName("[ERREUR] Rechercher une cote inconnue (code 0) ou vide (code -3)")
	void rechercher_inconnuOuVide() throws Exception {
		saisir(onglet, "champRecherche", "XX");
		cliquer(onglet, "rechercher");
		verifierStatut(onglet, "(code 0)");

		saisir(onglet, "champRecherche", "");
		cliquer(onglet, "rechercher");
		verifierStatut(onglet, "(code -3)");
	}

	@Test
	@DisplayName("[OK] Prêter puis rendre : l'emprunteur et la date de prêt s'affichent puis disparaissent")
	void preterPuisRendre() throws Exception {
		creerLivres("L1", "Le Petit Prince");
		selectionner(onglet, "L1");

		saisir(onglet, "champEmprunteur", "TEST1");
		cliquer(onglet, "preter");
		verifierStatut(onglet, "prêté à TEST1 (code 1)");
		assertEquals("TEST1", valeur(onglet, "L1", 2));
		assertNotEquals("—", valeur(onglet, "L1", 3));

		cliquer(onglet, "rendre");
		verifierStatut(onglet, "Livre L1 rendu");
		assertEquals("—", valeur(onglet, "L1", 2));
		assertEquals("—", valeur(onglet, "L1", 3));
	}

	@Test
	@DisplayName("[ERREUR] Prêter sans élève saisi, ou à un élève inexistant (code -1) : le livre reste disponible")
	void preter_casInvalides() throws Exception {
		creerLivres("L1", "Le Petit Prince");
		selectionner(onglet, "L1");

		saisir(onglet, "champEmprunteur", "");
		cliquer(onglet, "preter");
		verifierStatut(onglet, "Saisissez le numéro de l'élève emprunteur");

		saisir(onglet, "champEmprunteur", "INCONNU");
		cliquer(onglet, "preter");
		verifierStatut(onglet, "l'élève INCONNU n'existe pas");
		assertEquals("—", valeur(onglet, "L1", 2));
	}

	@Test
	@DisplayName("[OK] Modifier le titre (code 1) ; [ERREUR] un titre vide est refusé")
	void modifierTitre() throws Exception {
		creerLivres("L1", "Germinal");
		selectionner(onglet, "L1");

		saisir(onglet, "champNouveauTitre", "Germinal (poche)");
		cliquer(onglet, "modifierTitre");
		verifierStatut(onglet, "Titre du livre L1 modifié (code 1)");
		assertEquals("Germinal (poche)", valeur(onglet, "L1", 1));

		selectionner(onglet, "L1");
		saisir(onglet, "champNouveauTitre", "");
		cliquer(onglet, "modifierTitre");
		verifierStatut(onglet, "Titre invalide");
		assertEquals("Germinal (poche)", valeur(onglet, "L1", 1));
	}

	@Test
	@DisplayName("[OK] Filtres « disponibles » et « empruntés par l'élève » : seuls les bons livres restent")
	void filtres() throws Exception {
		creerLivres("L1", "Le Petit Prince", "L2", "Germinal");
		LivreDao.updateEmprunteurLivre("L1", "TEST1");

		filtrer(onglet, FILTRE_DISPONIBLES);
		assertEquals(1, lignes(onglet));
		assertEquals("Germinal", valeur(onglet, "L2", 1));

		saisir(onglet, "champEleveFiltre", "TEST1");
		filtrer(onglet, FILTRE_EMPRUNTES_PAR);
		assertEquals(1, lignes(onglet));
		assertEquals("Le Petit Prince", valeur(onglet, "L1", 1));

		filtrer(onglet, FILTRE_TOUS);
		assertEquals(2, lignes(onglet));
	}

	@Test
	@DisplayName("[ERREUR] Filtre « empruntés par l'élève » sans numéro saisi : l'interface le signale")
	void filtreEmprunteur_sansNumero() throws Exception {
		saisir(onglet, "champEleveFiltre", "");

		filtrer(onglet, FILTRE_EMPRUNTES_PAR);

		verifierStatut(onglet, "Saisissez le numéro de l'élève");
	}

	@Test
	@DisplayName("[OK] Supprimer le livre sélectionné après confirmation (code 1)")
	void supprimer() throws Exception {
		creerLivres("L1", "Le Petit Prince", "L2", "Germinal");
		selectionner(onglet, "L2");

		cliquer(onglet, "supprimerSelection");

		verifierStatut(onglet, "Livre L2 supprimé (code 1)");
		assertEquals(1, lignes(onglet));
	}

}
