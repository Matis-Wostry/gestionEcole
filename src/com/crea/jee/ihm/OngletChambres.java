package com.crea.jee.ihm;

import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.crea.jee.beans.Chambre;
import com.crea.jee.dao.ChambreDao;
import com.crea.jee.wrappers.ChambreWrapper;
import com.crea.jee.wrappers.Wrapper;

/*
 * Onglet des chambres : recherche, filtres, ajout, modification du prix, attribution à un élève et suppression
 */
class OngletChambres extends Onglet {

	private static final int TOUTES = 0;
	private static final int LIBRES = 1;
	private static final int PRIX_SUPERIEUR = 2;

	private final JTextField champRecherche = new JTextField(6);
	private final JComboBox<String> filtre = new JComboBox<>(
			new String[] { "Toutes les chambres", "Chambres libres", "Prix supérieur à" });
	private final JTextField champSeuil = new JTextField(6);
	private final JTextField champNo = new JTextField(5);
	private final JTextField champPrix = new JTextField(7);
	private final JTextField champNouveauPrix = new JTextField(7);
	private final JTextField champOccupant = new JTextField(10);

	OngletChambres() {
		super("N°", "Occupant", "Prix");

		JButton boutonRechercher = new JButton("Rechercher");
		JButton boutonActualiser = new JButton("Actualiser");
		JButton boutonSupprimer = new JButton("Supprimer la sélection");
		JButton boutonAjouter = new JButton("Ajouter");
		JButton boutonPrix = new JButton("Modifier le prix");
		JButton boutonAttribuer = new JButton("Attribuer");
		JButton boutonLiberer = new JButton("Libérer");
		boutonRechercher.addActionListener(e -> rechercher());
		champRecherche.addActionListener(e -> rechercher());
		filtre.addActionListener(e -> actualiser());
		champSeuil.addActionListener(e -> actualiser());
		boutonActualiser.addActionListener(e -> actualiser());
		boutonSupprimer.addActionListener(e -> supprimerSelection());
		boutonAjouter.addActionListener(e -> ajouter());
		boutonPrix.addActionListener(e -> modifierPrix());
		boutonAttribuer.addActionListener(e -> attribuer());
		boutonLiberer.addActionListener(e -> liberer());

		JPanel haut = barre();
		haut.add(new JLabel("N° :"));
		haut.add(champRecherche);
		haut.add(boutonRechercher);
		haut.add(Box.createHorizontalStrut(24));
		haut.add(filtre);
		haut.add(champSeuil);
		haut.add(boutonActualiser);
		haut.add(boutonSupprimer);

		JPanel ajout = formulaire("Ajouter une chambre (numéro et prix obligatoires)");
		ajout.add(new JLabel("N°"));
		ajout.add(champNo);
		ajout.add(new JLabel("Prix"));
		ajout.add(champPrix);
		ajout.add(boutonAjouter);

		JPanel modification = formulaire("Chambre sélectionnée dans le tableau");
		modification.add(new JLabel("Nouveau prix"));
		modification.add(champNouveauPrix);
		modification.add(boutonPrix);
		modification.add(Box.createHorizontalStrut(24));
		modification.add(new JLabel("Occupant (n° d'élève)"));
		modification.add(champOccupant);
		modification.add(boutonAttribuer);
		modification.add(boutonLiberer);

		assembler(haut, ajout, modification);
	}

	// recharge le tableau selon le filtre choisi
	@Override
	void actualiser() {
		List<Chambre> chambres;
		switch (filtre.getSelectedIndex()) {
		case LIBRES:
			chambres = ChambreDao.getChambresNonOccupees();
			break;
		case PRIX_SUPERIEUR:
			float seuil = decimal(champSeuil);
			chambres = Float.isNaN(seuil) ? List.of() : ChambreDao.getChambresPrixSuperieur(seuil);
			break;
		default:
			chambres = ChambreDao.getAllChambres();
		}
		modeleTable.setRowCount(0);
		for (Chambre chambre : chambres) {
			modeleTable.addRow(new Object[] { chambre.getNo(), ouTiret(chambre.getNum()), chambre.getPrix() });
		}
		if (filtre.getSelectedIndex() == PRIX_SUPERIEUR && Float.isNaN(decimal(champSeuil))) {
			afficherMessage("Saisissez un prix à côté du filtre « Prix supérieur à ».", ROUGE);
		} else {
			afficherBilan(chambres.size(), "Aucune chambre à afficher.", "chambre(s)");
		}
	}

	// cherche la chambre saisie et réagit selon le code réponse du wrapper
	private void rechercher() {
		int no = entier(champRecherche);
		ChambreWrapper resultat = ChambreDao.getChambreByNo(no);
		int code = resultat.getCodeResponse();
		switch (code) {
		case Wrapper.TROUVE:
			Chambre chambre = resultat.getChambre();
			afficherToutes();
			selectionnerLigne(chambre.getNo());
			afficherMessage("Chambre " + chambre.getNo() + " trouvée (code 1).", VERT);
			afficherFiche("Chambre " + chambre.getNo(), "N° : " + chambre.getNo()
					+ "\nOccupant : " + (chambre.getNum() == null ? "aucun" : chambre.getNum())
					+ "\nPrix : " + chambre.getPrix());
			break;
		case Wrapper.NON_TROUVE:
			afficherMessage("Aucune chambre n° " + no + " (code 0).", ROUGE);
			break;
		case Wrapper.DONNEES_INVALIDES:
			afficherMessage("Saisissez un numéro de chambre supérieur à 0 (code -3).", ROUGE);
			break;
		default:
			afficherErreurBase(code);
		}
	}

	private void ajouter() {
		int no = entier(champNo);
		int code = ChambreDao.addChambre(new Chambre(no, null, decimal(champPrix)));
		switch (code) {
		case 1:
			afficherToutes();
			selectionnerLigne(no);
			champNo.setText("");
			champPrix.setText("");
			afficherMessage("Chambre " + no + " ajoutée (code 1).", VERT);
			break;
		case -2:
			afficherMessage("La chambre n° " + no + " existe déjà (code -2).", ROUGE);
			break;
		case -3:
			afficherMessage("Données invalides : numéro et prix supérieurs à 0 (code -3).", ROUGE);
			break;
		default:
			afficherErreurBase(code);
		}
	}

	private void modifierPrix() {
		Object no = valeurSelectionnee(0);
		if (no == null) {
			return;
		}
		int code = ChambreDao.updatePrixChambre((Integer) no, decimal(champNouveauPrix));
		if (code > 0) {
			rechargerEtSelectionner(no);
			champNouveauPrix.setText("");
			afficherMessage("Prix de la chambre " + no + " modifié (code 1).", VERT);
		} else {
			afficherEchecModification(no, code, "Prix invalide : il doit être supérieur à 0 (code -3).");
		}
	}

	// attribue la chambre sélectionnée à l'élève saisi
	private void attribuer() {
		Object no = valeurSelectionnee(0);
		if (no == null) {
			return;
		}
		String num = texte(champOccupant);
		if (num.isEmpty()) {
			afficherMessage("Saisissez le numéro de l'élève à loger.", ROUGE);
			return;
		}
		int code = ChambreDao.updateOccupantChambre((Integer) no, num);
		if (code > 0) {
			rechargerEtSelectionner(no);
			champOccupant.setText("");
			afficherMessage("Chambre " + no + " attribuée à " + num + " (code 1).", VERT);
		} else if (code == 0) {
			afficherEchecModification(no, code, null);
		} else {
			afficherMessage("Impossible : l'élève " + num + " n'existe pas, ou la base ne répond pas (code " + code + ").",
					ROUGE);
		}
	}

	private void liberer() {
		Object no = valeurSelectionnee(0);
		if (no == null) {
			return;
		}
		int code = ChambreDao.updateOccupantChambre((Integer) no, null);
		if (code > 0) {
			rechargerEtSelectionner(no);
			afficherMessage("Chambre " + no + " libérée (code 1).", VERT);
		} else {
			afficherEchecModification(no, code, null);
		}
	}

	private void supprimerSelection() {
		Object no = valeurSelectionnee(0);
		if (no == null || !confirmer("Supprimer la chambre " + no + " ?\nSon éventuel occupant en sera détaché.")) {
			return;
		}
		int code = ChambreDao.deleteChambreByNo((Integer) no);
		if (code > 0) {
			actualiser();
			afficherMessage("Chambre " + no + " supprimée (code 1).", VERT);
		} else {
			afficherEchecModification(no, code, null);
		}
	}

	/*
	 * Outils
	 */

	// repasse le filtre sur « toutes » pour que la chambre concernée soit forcément visible
	private void afficherToutes() {
		filtre.setSelectedIndex(TOUTES);
		actualiser();
	}

	private void rechargerEtSelectionner(Object no) {
		actualiser();
		selectionnerLigne(no);
	}

	// message d'échec commun aux modifications : chambre disparue (0), données invalides (-3) ou base injoignable
	private void afficherEchecModification(Object no, int code, String messageDonneesInvalides) {
		if (code == 0) {
			actualiser();
			afficherMessage("La chambre " + no + " n'existe plus (code 0).", ROUGE);
		} else if (code == -3 && messageDonneesInvalides != null) {
			afficherMessage(messageDonneesInvalides, ROUGE);
		} else {
			afficherErreurBase(code);
		}
	}

}
