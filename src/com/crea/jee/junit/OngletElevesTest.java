package com.crea.jee.junit;

import static com.crea.jee.junit.OutilsIhm.cliquer;
import static com.crea.jee.junit.OutilsIhm.deselectionner;
import static com.crea.jee.junit.OutilsIhm.dernierDialogue;
import static com.crea.jee.junit.OutilsIhm.lignes;
import static com.crea.jee.junit.OutilsIhm.saisir;
import static com.crea.jee.junit.OutilsIhm.selection;
import static com.crea.jee.junit.OutilsIhm.selectionner;
import static com.crea.jee.junit.OutilsIhm.texte;
import static com.crea.jee.junit.OutilsIhm.verifierStatut;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;

import javax.swing.JOptionPane;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.crea.jee.beans.Eleve;
import com.crea.jee.dao.EleveDao;
import com.crea.jee.ihm.FenetreEcole;

/*
 * Tests de l'onglet Élèves de l'interface, contre la base ecole_test (-Ddb.name=ecole_test)
 * Chaque test ouvre la vraie fenêtre (hors de l'écran), agit comme un utilisateur puis vérifie l'affichage
 * [OK] = cas valide, l'action doit réussir ; [ERREUR] = cas invalide, l'interface doit l'afficher clairement
 */
@DisplayName("Interface : onglet Élèves")
class OngletElevesTest {

	private FenetreEcole fenetre;
	private Object onglet;

	@BeforeEach
	void ouvrir() throws Exception {
		assumeFalse(GraphicsEnvironment.isHeadless(), "Tests d'interface ignorés : aucun écran disponible");
		BaseDeTest.viderLesTables();
		fenetre = OutilsIhm.ouvrir();
		onglet = OutilsIhm.onglet(fenetre, 0);
	}

	@AfterEach
	void fermer() throws Exception {
		OutilsIhm.fermer(fenetre);
		BaseDeTest.viderLesTables();
	}

	// ajoute un élève directement par le DAO puis recharge l'onglet
	private void creerEleve(String num, String nom) throws Exception {
		EleveDao.addEleve(new Eleve(num, 0, nom, 20, "Adresse"));
		cliquer(onglet, "actualiser");
	}

	@Test
	@DisplayName("[OK] Ouverture sur une base vide : tableau vide et message explicite")
	void ouverture_baseVide() throws Exception {
		assertEquals(0, lignes(onglet));
		verifierStatut(onglet, "Aucun élève");
	}

	@Test
	@DisplayName("[OK] Ajouter : l'élève apparaît, il est sélectionné et le formulaire est vidé (code 1)")
	void ajouter_valide() throws Exception {
		saisir(onglet, "champNum", "TEST1", "champNom", "Alice Test", "champAge", "20", "champAdresse", "1 rue A");
		cliquer(onglet, "ajouter");

		verifierStatut(onglet, "Élève TEST1 ajouté (code 1)");
		assertEquals(1, lignes(onglet));
		assertEquals("TEST1", selection(onglet));
		assertTrue(texte(onglet, "champNum").isEmpty() && texte(onglet, "champNom").isEmpty());
	}

	@Test
	@DisplayName("[ERREUR] Ajouter un numéro déjà pris : refusé avec le code -2")
	void ajouter_numeroDejaPris() throws Exception {
		creerEleve("TEST1", "Alice Test");

		saisir(onglet, "champNum", "TEST1", "champNom", "Autre", "champAge", "30", "champAdresse", "");
		cliquer(onglet, "ajouter");

		verifierStatut(onglet, "déjà pris (code -2)");
		assertEquals(1, lignes(onglet));
	}

	@Test
	@DisplayName("[ERREUR] Ajouter avec un nom vide ou un âge non numérique : refusé avec le code -3")
	void ajouter_donneesInvalides() throws Exception {
		saisir(onglet, "champNum", "TEST1", "champNom", "", "champAge", "20", "champAdresse", "");
		cliquer(onglet, "ajouter");
		verifierStatut(onglet, "(code -3)");

		saisir(onglet, "champNum", "TEST1", "champNom", "Alice", "champAge", "abc", "champAdresse", "");
		cliquer(onglet, "ajouter");
		verifierStatut(onglet, "(code -3)");

		assertEquals(0, lignes(onglet));
	}

	@Test
	@DisplayName("[OK] Rechercher un élève existant : fiche affichée et ligne sélectionnée (code 1)")
	void rechercher_existant() throws Exception {
		creerEleve("TEST1", "Alice Test");
		creerEleve("TEST2", "Bob Test");

		saisir(onglet, "champRecherche", "TEST1");
		cliquer(onglet, "rechercher");

		verifierStatut(onglet, "Élève TEST1 trouvé (code 1)");
		assertEquals("TEST1", selection(onglet));
		assertTrue(dernierDialogue().contains("Nom : Alice Test"));
	}

	@Test
	@DisplayName("[ERREUR] Rechercher un numéro inconnu (code 0) ou vide (code -3)")
	void rechercher_inconnuOuVide() throws Exception {
		saisir(onglet, "champRecherche", "INCONNU");
		cliquer(onglet, "rechercher");
		verifierStatut(onglet, "(code 0)");

		saisir(onglet, "champRecherche", "");
		cliquer(onglet, "rechercher");
		verifierStatut(onglet, "(code -3)");
	}

	@Test
	@DisplayName("[ERREUR] Supprimer sans avoir sélectionné de ligne : l'interface le signale")
	void supprimer_sansSelection() throws Exception {
		creerEleve("TEST1", "Alice Test");
		deselectionner(onglet);

		cliquer(onglet, "supprimerSelection");

		verifierStatut(onglet, "Sélectionnez d'abord");
		assertEquals(1, lignes(onglet));
	}

	@Test
	@DisplayName("[OK] Supprimer puis répondre Non à la confirmation : rien n'est supprimé")
	void supprimer_annule() throws Exception {
		creerEleve("TEST1", "Alice Test");
		selectionner(onglet, "TEST1");
		OutilsIhm.reponse = JOptionPane.NO_OPTION;

		cliquer(onglet, "supprimerSelection");

		assertEquals(1, lignes(onglet));
		assertTrue(dernierDialogue().contains("Supprimer l'élève TEST1"));
	}

	@Test
	@DisplayName("[OK] Supprimer puis confirmer : l'élève disparaît du tableau (code 1)")
	void supprimer_confirme() throws Exception {
		creerEleve("TEST1", "Alice Test");
		selectionner(onglet, "TEST1");

		cliquer(onglet, "supprimerSelection");

		verifierStatut(onglet, "Élève TEST1 supprimé (code 1)");
		assertEquals(0, lignes(onglet));
	}

}
