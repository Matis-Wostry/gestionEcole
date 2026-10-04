package com.crea.jee.ihm;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.crea.jee.beans.Livre;
import com.crea.jee.dao.LivreDao;
import com.crea.jee.wrappers.LivreWrapper;
import com.crea.jee.wrappers.Wrapper;

/*
 * Onglet des livres : recherche, filtres, ajout, prêt et retour, modification du titre et suppression
 */
class OngletLivres extends Onglet {

	private static final int TOUS = 0;
	private static final int DISPONIBLES = 1;
	private static final int EMPRUNTES_PAR = 2;

	private final JTextField champRecherche = new JTextField(10);
	private final JComboBox<String> filtre = new JComboBox<>(
			new String[] { "Tous les livres", "Livres disponibles", "Empruntés par l'élève" });
	private final JTextField champEleveFiltre = new JTextField(8);
	private final JTextField champCote = new JTextField(10);
	private final JTextField champTitre = new JTextField(24);
	private final JTextField champEmprunteur = new JTextField(8);
	private final JTextField champNouveauTitre = new JTextField(18);

	OngletLivres() {
		super("Cote", "Titre", "Emprunteur", "Date de prêt");

		JButton boutonRechercher = new JButton("Rechercher");
		JButton boutonActualiser = new JButton("Actualiser");
		JButton boutonSupprimer = new JButton("Supprimer la sélection");
		JButton boutonAjouter = new JButton("Ajouter");
		JButton boutonPreter = new JButton("Prêter");
		JButton boutonRendre = new JButton("Rendre");
		JButton boutonTitre = new JButton("Modifier le titre");
		boutonRechercher.addActionListener(e -> rechercher());
		champRecherche.addActionListener(e -> rechercher());
		filtre.addActionListener(e -> actualiser());
		champEleveFiltre.addActionListener(e -> actualiser());
		boutonActualiser.addActionListener(e -> actualiser());
		boutonSupprimer.addActionListener(e -> supprimerSelection());
		boutonAjouter.addActionListener(e -> ajouter());
		boutonPreter.addActionListener(e -> preter());
		boutonRendre.addActionListener(e -> rendre());
		boutonTitre.addActionListener(e -> modifierTitre());

		JPanel haut = barre();
		haut.add(new JLabel("Cote :"));
		haut.add(champRecherche);
		haut.add(boutonRechercher);
		haut.add(Box.createHorizontalStrut(24));
		haut.add(filtre);
		haut.add(champEleveFiltre);
		haut.add(boutonActualiser);
		haut.add(boutonSupprimer);

		JPanel ajout = formulaire("Ajouter un livre (cote et titre obligatoires)");
		ajout.add(new JLabel("Cote"));
		ajout.add(champCote);
		ajout.add(new JLabel("Titre"));
		ajout.add(champTitre);
		ajout.add(boutonAjouter);

		JPanel modification = formulaire("Livre sélectionné dans le tableau");
		modification.add(new JLabel("Élève (n°)"));
		modification.add(champEmprunteur);
		modification.add(boutonPreter);
		modification.add(boutonRendre);
		modification.add(Box.createHorizontalStrut(24));
		modification.add(new JLabel("Nouveau titre"));
		modification.add(champNouveauTitre);
		modification.add(boutonTitre);

		assembler(haut, ajout, modification);
	}

	// recharge le tableau selon le filtre choisi
	@Override
	void actualiser() {
		List<Livre> livres;
		switch (filtre.getSelectedIndex()) {
		case DISPONIBLES:
			livres = LivreDao.getLivresDisponibles();
			break;
		case EMPRUNTES_PAR:
			livres = LivreDao.getLivresEmpruntesByEleve(texte(champEleveFiltre));
			break;
		default:
			livres = LivreDao.getAllLivres();
		}
		modeleTable.setRowCount(0);
		for (Livre livre : livres) {
			modeleTable.addRow(new Object[] { livre.getCote(), livre.getTitre(), ouTiret(livre.getNum()),
					ouTiret(formaterDate(livre.getDatepret())) });
		}
		if (filtre.getSelectedIndex() == EMPRUNTES_PAR && texte(champEleveFiltre).isEmpty()) {
			afficherMessage("Saisissez le numéro de l'élève à côté du filtre « Empruntés par l'élève ».", ROUGE);
		} else {
			afficherBilan(livres.size(), "Aucun livre à afficher.", "livre(s)");
		}
	}

	// cherche le livre saisi et réagit selon le code réponse du wrapper
	private void rechercher() {
		String cote = texte(champRecherche);
		LivreWrapper resultat = LivreDao.getLivreByCote(cote);
		int code = resultat.getCodeResponse();
		switch (code) {
		case Wrapper.TROUVE:
			Livre livre = resultat.getLivre();
			afficherTous();
			selectionnerLigne(livre.getCote());
			afficherMessage("Livre " + livre.getCote() + " trouvé (code 1).", VERT);
			afficherFiche("Livre " + livre.getCote(), "Cote : " + livre.getCote()
					+ "\nTitre : " + livre.getTitre()
					+ "\nEmprunteur : " + (livre.getNum() == null ? "aucun (disponible)" : livre.getNum())
					+ "\nDate de prêt : " + (livre.getDatepret() == null ? "—" : formaterDate(livre.getDatepret())));
			break;
		case Wrapper.NON_TROUVE:
			afficherMessage("Aucun livre avec la cote " + cote + " (code 0).", ROUGE);
			break;
		case Wrapper.DONNEES_INVALIDES:
			afficherMessage("Saisissez une cote à rechercher (code -3).", ROUGE);
			break;
		default:
			afficherErreurBase(code);
		}
	}

	private void ajouter() {
		String cote = texte(champCote);
		int code = LivreDao.addLivre(new Livre(cote, null, texte(champTitre), null));
		switch (code) {
		case 1:
			afficherTous();
			selectionnerLigne(cote);
			champCote.setText("");
			champTitre.setText("");
			afficherMessage("Livre " + cote + " ajouté (code 1).", VERT);
			break;
		case -2:
			afficherMessage("La cote " + cote + " existe déjà (code -2).", ROUGE);
			break;
		case -3:
			afficherMessage("Données invalides : cote et titre obligatoires, 100 caractères maximum (code -3).", ROUGE);
			break;
		default:
			afficherErreurBase(code);
		}
	}

	// prête le livre sélectionné à l'élève saisi (la date de prêt est posée à l'instant présent)
	private void preter() {
		Object cote = valeurSelectionnee(0);
		if (cote == null) {
			return;
		}
		String num = texte(champEmprunteur);
		if (num.isEmpty()) {
			afficherMessage("Saisissez le numéro de l'élève emprunteur.", ROUGE);
			return;
		}
		int code = LivreDao.updateEmprunteurLivre((String) cote, num);
		if (code > 0) {
			rechargerEtSelectionner(cote);
			champEmprunteur.setText("");
			afficherMessage("Livre " + cote + " prêté à " + num + " (code 1).", VERT);
		} else if (code == 0) {
			afficherLivreDisparu(cote);
		} else {
			afficherMessage("Impossible : l'élève " + num + " n'existe pas, ou la base ne répond pas (code " + code + ").",
					ROUGE);
		}
	}

	private void rendre() {
		Object cote = valeurSelectionnee(0);
		if (cote == null) {
			return;
		}
		int code = LivreDao.updateEmprunteurLivre((String) cote, null);
		if (code > 0) {
			rechargerEtSelectionner(cote);
			afficherMessage("Livre " + cote + " rendu, il est de nouveau disponible (code 1).", VERT);
		} else if (code == 0) {
			afficherLivreDisparu(cote);
		} else {
			afficherErreurBase(code);
		}
	}

	private void modifierTitre() {
		Object cote = valeurSelectionnee(0);
		if (cote == null) {
			return;
		}
		int code = LivreDao.updateTitreLivre((String) cote, texte(champNouveauTitre));
		if (code > 0) {
			rechargerEtSelectionner(cote);
			champNouveauTitre.setText("");
			afficherMessage("Titre du livre " + cote + " modifié (code 1).", VERT);
		} else if (code == 0) {
			afficherLivreDisparu(cote);
		} else if (code == -3) {
			afficherMessage("Titre invalide : obligatoire, 100 caractères maximum (code -3).", ROUGE);
		} else {
			afficherErreurBase(code);
		}
	}

	private void supprimerSelection() {
		Object cote = valeurSelectionnee(0);
		if (cote == null || !confirmer("Supprimer le livre " + cote + " ?")) {
			return;
		}
		int code = LivreDao.deleteLivreByCote((String) cote);
		if (code > 0) {
			actualiser();
			afficherMessage("Livre " + cote + " supprimé (code 1).", VERT);
		} else if (code == 0) {
			afficherLivreDisparu(cote);
		} else {
			afficherErreurBase(code);
		}
	}

	/*
	 * Outils
	 */

	// repasse le filtre sur « tous » pour que le livre concerné soit forcément visible
	private void afficherTous() {
		filtre.setSelectedIndex(TOUS);
		actualiser();
	}

	private void rechargerEtSelectionner(Object cote) {
		actualiser();
		selectionnerLigne(cote);
	}

	private void afficherLivreDisparu(Object cote) {
		actualiser();
		afficherMessage("Le livre " + cote + " n'existe plus (code 0).", ROUGE);
	}

	private static String formaterDate(Timestamp date) {
		return date == null ? null : new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
	}

}
