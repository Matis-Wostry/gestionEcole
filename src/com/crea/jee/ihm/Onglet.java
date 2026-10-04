package com.crea.jee.ihm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/*
 * Base commune des onglets de l'application : un tableau, une barre d'état et quelques outils de saisie
 * Chaque onglet fournit sa barre du haut (recherche, filtres, boutons) et ses formulaires du bas
 */
abstract class Onglet {

	protected static final Color VERT = new Color(20, 110, 50);
	protected static final Color ROUGE = new Color(170, 30, 30);

	protected final JPanel panneau = new JPanel(new BorderLayout());
	protected final DefaultTableModel modeleTable;
	protected final JTable table;
	private final JLabel barreStatut = new JLabel(" ");

	/*
	 * Constructeur
	 */

	// prépare un tableau non modifiable à la main, avec une seule ligne sélectionnable à la fois
	protected Onglet(String... colonnes) {
		modeleTable = new DefaultTableModel(colonnes, 0);
		table = new JTable(modeleTable);
		table.setDefaultEditor(Object.class, null);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(24);
		barreStatut.setBorder(BorderFactory.createEmptyBorder(4, 8, 6, 8));
	}

	// recharge le tableau depuis la base
	abstract void actualiser();

	// retourne le panneau à placer dans l'onglet
	JPanel getPanneau() {
		return panneau;
	}

	// place la barre du haut, le tableau au centre, puis les formulaires et la barre d'état en bas
	protected void assembler(JPanel haut, JPanel... formulaires) {
		JPanel blocFormulaires = new JPanel();
		blocFormulaires.setLayout(new BoxLayout(blocFormulaires, BoxLayout.Y_AXIS));
		for (JPanel formulaire : formulaires) {
			blocFormulaires.add(formulaire);
		}
		JPanel bas = new JPanel(new BorderLayout());
		bas.add(blocFormulaires, BorderLayout.CENTER);
		bas.add(barreStatut, BorderLayout.SOUTH);

		panneau.add(haut, BorderLayout.NORTH);
		panneau.add(new JScrollPane(table), BorderLayout.CENTER);
		panneau.add(bas, BorderLayout.SOUTH);
	}

	/*
	 * Messages et dialogues
	 */

	protected void afficherMessage(String message, Color couleur) {
		barreStatut.setText(message);
		barreStatut.setForeground(couleur);
	}

	protected void afficherErreurBase(int code) {
		afficherMessage("La base de données ne répond pas (code " + code + ").", ROUGE);
	}

	// affiche le nombre de lignes chargées, ou prévient si le tableau est vide
	protected void afficherBilan(int nombre, String vide, String unite) {
		if (nombre == 0) {
			afficherMessage(vide, ROUGE);
		} else {
			afficherMessage(nombre + " " + unite + " chargé(s).", Color.DARK_GRAY);
		}
	}

	protected void afficherFiche(String titre, String fiche) {
		JOptionPane.showMessageDialog(panneau, fiche, titre, JOptionPane.INFORMATION_MESSAGE);
	}

	protected boolean confirmer(String message) {
		return JOptionPane.showConfirmDialog(panneau, message, "Confirmer", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
	}

	/*
	 * Tableau
	 */

	// retourne la valeur de la colonne demandée pour la ligne sélectionnée, ou null (avec un message) si rien n'est sélectionné
	protected Object valeurSelectionnee(int colonne) {
		int ligne = table.getSelectedRow();
		if (ligne < 0) {
			afficherMessage("Sélectionnez d'abord une ligne dans le tableau.", ROUGE);
			return null;
		}
		return modeleTable.getValueAt(table.convertRowIndexToModel(ligne), colonne);
	}

	// sélectionne et fait défiler jusqu'à la ligne dont la première colonne vaut la clé donnée
	protected void selectionnerLigne(Object cle) {
		for (int i = 0; i < table.getRowCount(); i++) {
			if (String.valueOf(cle).equals(String.valueOf(table.getValueAt(i, 0)))) {
				table.setRowSelectionInterval(i, i);
				table.scrollRectToVisible(table.getCellRect(i, 0, true));
				return;
			}
		}
	}

	// valeur affichée dans le tableau pour une donnée absente (NULL en base)
	protected static Object ouTiret(Object valeur) {
		return valeur == null ? "—" : valeur;
	}

	/*
	 * Saisie
	 */

	protected static JPanel barre() {
		return new JPanel(new FlowLayout(FlowLayout.LEFT));
	}

	protected static JPanel formulaire(String titre) {
		JPanel formulaire = barre();
		formulaire.setBorder(BorderFactory.createTitledBorder(titre));
		return formulaire;
	}

	protected static String texte(JTextField champ) {
		return champ.getText().trim();
	}

	// entier saisi, ou 0 si la saisie n'est pas un nombre (0 est refusé par la validation des DAO)
	protected static int entier(JTextField champ) {
		try {
			return Integer.parseInt(texte(champ));
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	// décimal saisi (virgule ou point acceptés), ou NaN si la saisie n'est pas un nombre
	protected static float decimal(JTextField champ) {
		try {
			return Float.parseFloat(texte(champ).replace(',', '.'));
		} catch (NumberFormatException ex) {
			return Float.NaN;
		}
	}

}
