package com.crea.jee.ihm;

import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.crea.jee.beans.Uv;
import com.crea.jee.dao.UvDao;
import com.crea.jee.wrappers.UvWrapper;
import com.crea.jee.wrappers.Wrapper;

/*
 * Onglet des UV : recherche, filtre, ajout, modification des heures et du coordinateur, suppression
 */
class OngletUv extends Onglet {

	private static final int TOUTES = 0;
	private static final int HEURES_SUPERIEURES = 1;

	private final JTextField champRecherche = new JTextField(12);
	private final JComboBox<String> filtre = new JComboBox<>(new String[] { "Toutes les UV", "Plus d'heures que" });
	private final JTextField champSeuil = new JTextField(4);
	private final JTextField champCode = new JTextField(12);
	private final JTextField champHeures = new JTextField(4);
	private final JTextField champCoordinateur = new JTextField(16);
	private final JTextField champNouvellesHeures = new JTextField(4);
	private final JTextField champNouveauCoordinateur = new JTextField(16);

	OngletUv() {
		super("Code", "Heures", "Coordinateur");

		JButton boutonRechercher = new JButton("Rechercher");
		JButton boutonActualiser = new JButton("Actualiser");
		JButton boutonSupprimer = new JButton("Supprimer la sélection");
		JButton boutonAjouter = new JButton("Ajouter");
		JButton boutonHeures = new JButton("Modifier les heures");
		JButton boutonCoordinateur = new JButton("Modifier le coordinateur");
		boutonRechercher.addActionListener(e -> rechercher());
		champRecherche.addActionListener(e -> rechercher());
		filtre.addActionListener(e -> actualiser());
		champSeuil.addActionListener(e -> actualiser());
		boutonActualiser.addActionListener(e -> actualiser());
		boutonSupprimer.addActionListener(e -> supprimerSelection());
		boutonAjouter.addActionListener(e -> ajouter());
		boutonHeures.addActionListener(e -> modifierHeures());
		boutonCoordinateur.addActionListener(e -> modifierCoordinateur());

		JPanel haut = barre();
		haut.add(new JLabel("Code :"));
		haut.add(champRecherche);
		haut.add(boutonRechercher);
		haut.add(Box.createHorizontalStrut(24));
		haut.add(filtre);
		haut.add(champSeuil);
		haut.add(boutonActualiser);
		haut.add(boutonSupprimer);

		JPanel ajout = formulaire("Ajouter une UV (code et nombre d'heures obligatoires)");
		ajout.add(new JLabel("Code"));
		ajout.add(champCode);
		ajout.add(new JLabel("Heures"));
		ajout.add(champHeures);
		ajout.add(new JLabel("Coordinateur"));
		ajout.add(champCoordinateur);
		ajout.add(boutonAjouter);

		JPanel modification = formulaire("UV sélectionnée dans le tableau");
		modification.add(new JLabel("Heures"));
		modification.add(champNouvellesHeures);
		modification.add(boutonHeures);
		modification.add(Box.createHorizontalStrut(24));
		modification.add(new JLabel("Coordinateur"));
		modification.add(champNouveauCoordinateur);
		modification.add(boutonCoordinateur);

		assembler(haut, ajout, modification);
	}

	// recharge le tableau selon le filtre choisi
	@Override
	void actualiser() {
		boolean filtreHeures = filtre.getSelectedIndex() == HEURES_SUPERIEURES;
		List<Uv> uvs = filtreHeures ? UvDao.getUvsNbhSuperieur(entier(champSeuil)) : UvDao.getAllUvs();
		modeleTable.setRowCount(0);
		for (Uv uv : uvs) {
			modeleTable.addRow(new Object[] { uv.getCode(), uv.getNbh(), ouTiret(uv.getCoord()) });
		}
		afficherBilan(uvs.size(), "Aucune UV à afficher.", "UV");
	}

	// cherche l'uv saisie et réagit selon le code réponse du wrapper
	private void rechercher() {
		String code = texte(champRecherche);
		UvWrapper resultat = UvDao.getUvByCode(code);
		int codeReponse = resultat.getCodeResponse();
		switch (codeReponse) {
		case Wrapper.TROUVE:
			Uv uv = resultat.getUv();
			afficherToutes();
			selectionnerLigne(uv.getCode());
			afficherMessage("UV " + uv.getCode() + " trouvée (code 1).", VERT);
			afficherFiche("UV " + uv.getCode(), "Code : " + uv.getCode()
					+ "\nNombre d'heures : " + uv.getNbh()
					+ "\nCoordinateur : " + uv.getCoord());
			break;
		case Wrapper.NON_TROUVE:
			afficherMessage("Aucune UV avec le code " + code + " (code 0).", ROUGE);
			break;
		case Wrapper.DONNEES_INVALIDES:
			afficherMessage("Saisissez un code d'UV à rechercher (code -3).", ROUGE);
			break;
		default:
			afficherErreurBase(codeReponse);
		}
	}

	private void ajouter() {
		String code = texte(champCode);
		int codeRetour = UvDao.addUv(new Uv(code, entier(champHeures), texte(champCoordinateur)));
		switch (codeRetour) {
		case 1:
			afficherToutes();
			selectionnerLigne(code);
			champCode.setText("");
			champHeures.setText("");
			champCoordinateur.setText("");
			afficherMessage("UV " + code + " ajoutée (code 1).", VERT);
			break;
		case -2:
			afficherMessage("Le code " + code + " existe déjà (code -2).", ROUGE);
			break;
		case -3:
			afficherMessage("Données invalides : code obligatoire, heures entre 1 et 127 (code -3).", ROUGE);
			break;
		default:
			afficherErreurBase(codeRetour);
		}
	}

	private void modifierHeures() {
		Object code = valeurSelectionnee(0);
		if (code == null) {
			return;
		}
		int codeRetour = UvDao.updateNbhUv((String) code, entier(champNouvellesHeures));
		if (codeRetour > 0) {
			rechargerEtSelectionner(code);
			champNouvellesHeures.setText("");
			afficherMessage("Nombre d'heures de l'UV " + code + " modifié (code 1).", VERT);
		} else {
			afficherEchecModification(code, codeRetour, "Nombre d'heures invalide : entre 1 et 127 (code -3).");
		}
	}

	private void modifierCoordinateur() {
		Object code = valeurSelectionnee(0);
		if (code == null) {
			return;
		}
		int codeRetour = UvDao.updateCoordUv((String) code, texte(champNouveauCoordinateur));
		if (codeRetour > 0) {
			rechargerEtSelectionner(code);
			champNouveauCoordinateur.setText("");
			afficherMessage("Coordinateur de l'UV " + code + " modifié (code 1).", VERT);
		} else {
			afficherEchecModification(code, codeRetour, "Coordinateur invalide : 255 caractères maximum (code -3).");
		}
	}

	private void supprimerSelection() {
		Object code = valeurSelectionnee(0);
		if (code == null || !confirmer("Supprimer l'UV " + code + " ?\nToutes les inscriptions à cette UV seront supprimées.")) {
			return;
		}
		int codeRetour = UvDao.deleteUvByCode((String) code);
		if (codeRetour > 0) {
			actualiser();
			afficherMessage("UV " + code + " supprimée avec ses inscriptions (code 1).", VERT);
		} else {
			afficherEchecModification(code, codeRetour, null);
		}
	}

	/*
	 * Outils
	 */

	// repasse le filtre sur « toutes » pour que l'uv concernée soit forcément visible
	private void afficherToutes() {
		filtre.setSelectedIndex(TOUTES);
		actualiser();
	}

	private void rechargerEtSelectionner(Object code) {
		actualiser();
		selectionnerLigne(code);
	}

	// message d'échec commun : uv disparue (0), données invalides (-3) ou base injoignable
	private void afficherEchecModification(Object code, int codeRetour, String messageDonneesInvalides) {
		if (codeRetour == 0) {
			actualiser();
			afficherMessage("L'UV " + code + " n'existe plus (code 0).", ROUGE);
		} else if (codeRetour == -3 && messageDonneesInvalides != null) {
			afficherMessage(messageDonneesInvalides, ROUGE);
		} else {
			afficherErreurBase(codeRetour);
		}
	}

}
