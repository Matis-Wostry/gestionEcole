package com.crea.jee.junit;

import static com.crea.jee.junit.OutilsIhm.cliquer;
import static com.crea.jee.junit.OutilsIhm.deselectionner;
import static com.crea.jee.junit.OutilsIhm.dernierDialogue;
import static com.crea.jee.junit.OutilsIhm.filtrer;
import static com.crea.jee.junit.OutilsIhm.lignes;
import static com.crea.jee.junit.OutilsIhm.saisir;
import static com.crea.jee.junit.OutilsIhm.selection;
import static com.crea.jee.junit.OutilsIhm.selectionner;
import static com.crea.jee.junit.OutilsIhm.valeur;
import static com.crea.jee.junit.OutilsIhm.verifierStatut;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;

import javax.swing.JOptionPane;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.crea.jee.beans.Chambre;
import com.crea.jee.beans.Eleve;
import com.crea.jee.dao.ChambreDao;
import com.crea.jee.dao.EleveDao;
import com.crea.jee.ihm.FenetreEcole;

/*
 * Tests de l'onglet Chambres de l'interface, contre la base ecole_test (-Ddb.name=ecole_test)
 * [OK] = cas valide, l'action doit réussir ; [ERREUR] = cas invalide, l'interface doit l'afficher clairement
 */
@DisplayName("Interface : onglet Chambres")
class OngletChambresTest {

	private static final int FILTRE_TOUTES = 0;
	private static final int FILTRE_LIBRES = 1;
	private static final int FILTRE_PRIX = 2;

	private FenetreEcole fenetre;
	private Object onglet;

	@BeforeEach
	void ouvrir() throws Exception {
		assumeFalse(GraphicsEnvironment.isHeadless(), "Tests d'interface ignorés : aucun écran disponible");
		BaseDeTest.viderLesTables();
		fenetre = OutilsIhm.ouvrir();
		onglet = OutilsIhm.onglet(fenetre, 1);
	}

	@AfterEach
	void fermer() throws Exception {
		OutilsIhm.fermer(fenetre);
		BaseDeTest.viderLesTables();
	}

	// crée des chambres (numéro, prix, numéro, prix...) directement par le DAO puis recharge l'onglet
	private void creerChambres(Object... numerosEtPrix) throws Exception {
		for (int i = 0; i < numerosEtPrix.length; i += 2) {
			ChambreDao.addChambre(new Chambre((Integer) numerosEtPrix[i], null, ((Number) numerosEtPrix[i + 1]).floatValue()));
		}
		cliquer(onglet, "actualiser");
	}

	@Test
	@DisplayName("[OK] Ajouter une chambre, avec un prix saisi à la française (virgule) (code 1)")
	void ajouter_valide() throws Exception {
		saisir(onglet, "champNo", "1", "champPrix", "150,5");
		cliquer(onglet, "ajouter");

		verifierStatut(onglet, "Chambre 1 ajoutée (code 1)");
		assertEquals("150.5", valeur(onglet, 1, 2));
		assertEquals("1", selection(onglet));
	}

	@Test
	@DisplayName("[ERREUR] Ajouter un numéro de chambre déjà existant : refusé avec le code -2")
	void ajouter_doublon() throws Exception {
		creerChambres(1, 300);

		saisir(onglet, "champNo", "1", "champPrix", "100");
		cliquer(onglet, "ajouter");

		verifierStatut(onglet, "(code -2)");
		assertEquals(1, lignes(onglet));
	}

	@Test
	@DisplayName("[ERREUR] Ajouter avec un numéro à 0, un prix non numérique ou négatif : refusé avec le code -3")
	void ajouter_donneesInvalides() throws Exception {
		String[][] saisies = { { "0", "100" }, { "3", "abc" }, { "3", "-5" } };
		for (String[] saisie : saisies) {
			saisir(onglet, "champNo", saisie[0], "champPrix", saisie[1]);
			cliquer(onglet, "ajouter");
			verifierStatut(onglet, "(code -3)");
		}
		assertEquals(0, lignes(onglet));
	}

	@Test
	@DisplayName("[OK] Rechercher une chambre existante : fiche affichée avec le prix (code 1)")
	void rechercher_existante() throws Exception {
		creerChambres(1, 300, 2, 150);

		saisir(onglet, "champRecherche", "1");
		cliquer(onglet, "rechercher");

		verifierStatut(onglet, "Chambre 1 trouvée (code 1)");
		assertEquals("1", selection(onglet));
		assertTrue(dernierDialogue().contains("Prix : 300"));
	}

	@Test
	@DisplayName("[ERREUR] Rechercher un numéro inconnu (code 0) ou non numérique (code -3)")
	void rechercher_inconnueOuInvalide() throws Exception {
		saisir(onglet, "champRecherche", "99");
		cliquer(onglet, "rechercher");
		verifierStatut(onglet, "(code 0)");

		saisir(onglet, "champRecherche", "abc");
		cliquer(onglet, "rechercher");
		verifierStatut(onglet, "(code -3)");
	}

	@Test
	@DisplayName("[OK] Attribuer la chambre sélectionnée à un élève : l'occupant s'affiche (code 1)")
	void attribuer_valide() throws Exception {
		EleveDao.addEleve(new Eleve("TEST1", 0, "Alice Test", 20, "Adresse"));
		creerChambres(1, 300);
		selectionner(onglet, 1);

		saisir(onglet, "champOccupant", "TEST1");
		cliquer(onglet, "attribuer");

		verifierStatut(onglet, "attribuée à TEST1 (code 1)");
		assertEquals("TEST1", valeur(onglet, 1, 1));
	}

	@Test
	@DisplayName("[ERREUR] Attribuer sans sélection, sans élève saisi, ou à un élève inexistant (code -1)")
	void attribuer_casInvalides() throws Exception {
		creerChambres(1, 300);

		deselectionner(onglet);
		saisir(onglet, "champOccupant", "TEST1");
		cliquer(onglet, "attribuer");
		verifierStatut(onglet, "Sélectionnez d'abord");

		selectionner(onglet, 1);
		saisir(onglet, "champOccupant", "");
		cliquer(onglet, "attribuer");
		verifierStatut(onglet, "Saisissez le numéro de l'élève");

		saisir(onglet, "champOccupant", "INCONNU");
		cliquer(onglet, "attribuer");
		verifierStatut(onglet, "l'élève INCONNU n'existe pas");
		assertEquals("—", valeur(onglet, 1, 1));
	}

	@Test
	@DisplayName("[OK] Libérer une chambre occupée : l'occupant disparaît (code 1)")
	void liberer() throws Exception {
		EleveDao.addEleve(new Eleve("TEST1", 0, "Alice Test", 20, "Adresse"));
		creerChambres(1, 300);
		ChambreDao.updateOccupantChambre(1, "TEST1");
		cliquer(onglet, "actualiser");
		selectionner(onglet, 1);

		cliquer(onglet, "liberer");

		verifierStatut(onglet, "Chambre 1 libérée (code 1)");
		assertEquals("—", valeur(onglet, 1, 1));
	}

	@Test
	@DisplayName("[OK] Modifier le prix de la chambre sélectionnée (code 1)")
	void modifierPrix_valide() throws Exception {
		creerChambres(1, 300);
		selectionner(onglet, 1);

		saisir(onglet, "champNouveauPrix", "350");
		cliquer(onglet, "modifierPrix");

		verifierStatut(onglet, "Prix de la chambre 1 modifié (code 1)");
		assertEquals("350.0", valeur(onglet, 1, 2));
	}

	@Test
	@DisplayName("[ERREUR] Modifier le prix à 0 : refusé, l'ancien prix est conservé (code -3)")
	void modifierPrix_invalide() throws Exception {
		creerChambres(1, 300);
		selectionner(onglet, 1);

		saisir(onglet, "champNouveauPrix", "0");
		cliquer(onglet, "modifierPrix");

		verifierStatut(onglet, "Prix invalide");
		assertEquals("300.0", valeur(onglet, 1, 2));
	}

	@Test
	@DisplayName("[OK] Filtres « chambres libres » et « prix supérieur à » : seules les bonnes chambres restent")
	void filtres() throws Exception {
		EleveDao.addEleve(new Eleve("TEST1", 0, "Alice Test", 20, "Adresse"));
		creerChambres(1, 300, 2, 150, 3, 400);
		ChambreDao.updateOccupantChambre(3, "TEST1");

		filtrer(onglet, FILTRE_LIBRES);
		assertEquals(2, lignes(onglet));
		assertNull(valeur(onglet, 3, 0));

		saisir(onglet, "champSeuil", "200");
		filtrer(onglet, FILTRE_PRIX);
		assertEquals(2, lignes(onglet));
		assertNull(valeur(onglet, 2, 0));

		filtrer(onglet, FILTRE_TOUTES);
		assertEquals(3, lignes(onglet));
	}

	@Test
	@DisplayName("[ERREUR] Filtre « prix supérieur à » sans prix saisi : message, et aucune requête invalide")
	void filtrePrix_sansSeuil() throws Exception {
		creerChambres(1, 300);
		saisir(onglet, "champSeuil", "");

		filtrer(onglet, FILTRE_PRIX);

		verifierStatut(onglet, "Saisissez un prix");
		assertEquals(0, lignes(onglet));
	}

	@Test
	@DisplayName("[OK] Supprimer : Non annule, Oui supprime la chambre (code 1)")
	void supprimer() throws Exception {
		creerChambres(1, 300, 2, 150);
		selectionner(onglet, 2);

		OutilsIhm.reponse = JOptionPane.NO_OPTION;
		cliquer(onglet, "supprimerSelection");
		assertEquals(2, lignes(onglet));

		OutilsIhm.reponse = JOptionPane.YES_OPTION;
		cliquer(onglet, "supprimerSelection");
		verifierStatut(onglet, "Chambre 2 supprimée (code 1)");
		assertEquals(1, lignes(onglet));
	}

}
