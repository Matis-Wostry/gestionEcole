package com.crea.jee.junit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.crea.jee.ihm.FenetreEcole;

/*
 * Outils communs aux tests de l'interface : ils pilotent la vraie fenêtre FenetreEcole comme un utilisateur
 * Les onglets et leurs actions ne sont pas publics : on y accède par réflexion, sans modifier le code de l'interface
 * Toutes les manipulations passent par le thread de Swing, comme les vrais clics
 */
final class OutilsIhm {

	// réponse donnée automatiquement aux demandes de confirmation (Oui par défaut, Non pour tester l'annulation)
	static volatile int reponse = JOptionPane.YES_OPTION;

	private static final List<String> dialogues = new ArrayList<>();
	private static Timer repondeur;

	private OutilsIhm() {
	}

	/*
	 * Fenêtre et onglets
	 */

	// ouvre la fenêtre hors de l'écran ; les boîtes de dialogue qu'elle affichera seront fermées automatiquement
	static FenetreEcole ouvrir() throws Exception {
		reponse = JOptionPane.YES_OPTION;
		synchronized (dialogues) {
			dialogues.clear();
		}
		return surEdt(() -> {
			demarrerRepondeur();
			FenetreEcole application = new FenetreEcole();
			JFrame fenetre = (JFrame) lire(application, "fenetre");
			fenetre.setLocation(-3000, -3000);
			fenetre.setVisible(true);
			return application;
		});
	}

	static void fermer(FenetreEcole application) throws Exception {
		if (application != null) {
			surEdt(() -> {
				((JFrame) lire(application, "fenetre")).dispose();
				return null;
			});
		}
	}

	// ouvre l'onglet demandé (ce qui le recharge) et le renvoie : 0 Élèves, 1 Chambres, 2 Livres, 3 UV, 4 Inscriptions
	static Object onglet(FenetreEcole application, int index) throws Exception {
		return surEdt(() -> {
			JTabbedPane onglets = (JTabbedPane) lire(application, "onglets");
			Object onglet = ((Object[]) lire(application, "contenus"))[index];
			if (onglets.getSelectedIndex() == index) {
				methode(onglet, "actualiser").invoke(onglet);
			} else {
				onglets.setSelectedIndex(index);
			}
			return onglet;
		});
	}

	// titres des onglets, dans l'ordre d'affichage
	static List<String> titresOnglets(FenetreEcole application) throws Exception {
		return surEdt(() -> {
			JTabbedPane onglets = (JTabbedPane) lire(application, "onglets");
			List<String> titres = new ArrayList<>();
			for (int i = 0; i < onglets.getTabCount(); i++) {
				titres.add(onglets.getTitleAt(i));
			}
			return titres;
		});
	}

	/*
	 * Actions de l'utilisateur
	 */

	// remplit des champs de saisie : saisir(onglet, "champNum", "TEST1", "champNom", "Alice", ...)
	static void saisir(Object onglet, String... champsEtValeurs) throws Exception {
		surEdt(() -> {
			for (int i = 0; i < champsEtValeurs.length; i += 2) {
				((JTextField) lire(onglet, champsEtValeurs[i])).setText(champsEtValeurs[i + 1]);
			}
			return null;
		});
	}

	// déclenche une action de l'onglet, comme un clic sur le bouton correspondant (ajouter, rechercher...)
	static void cliquer(Object onglet, String action) throws Exception {
		surEdt(() -> {
			methode(onglet, action).invoke(onglet);
			return null;
		});
	}

	// sélectionne la ligne dont la première colonne vaut la clé
	static void selectionner(Object onglet, Object cle) throws Exception {
		surEdt(() -> {
			methode(onglet, "selectionnerLigne", Object.class).invoke(onglet, cle);
			return null;
		});
	}

	// sélectionne la ligne dont les deux premières colonnes valent les clés données (inscriptions)
	static void selectionner(Object onglet, String cle1, String cle2) throws Exception {
		surEdt(() -> {
			JTable table = table(onglet);
			for (int i = 0; i < table.getRowCount(); i++) {
				if (cle1.equals(table.getValueAt(i, 0)) && cle2.equals(table.getValueAt(i, 1))) {
					table.setRowSelectionInterval(i, i);
				}
			}
			return null;
		});
	}

	static void deselectionner(Object onglet) throws Exception {
		surEdt(() -> {
			table(onglet).clearSelection();
			return null;
		});
	}

	// choisit un filtre dans la liste déroulante de l'onglet
	static void filtrer(Object onglet, int index) throws Exception {
		surEdt(() -> {
			((JComboBox<?>) lire(onglet, "filtre")).setSelectedIndex(index);
			methode(onglet, "actualiser").invoke(onglet);
			return null;
		});
	}

	/*
	 * Lecture de l'écran
	 */

	static String statut(Object onglet) throws Exception {
		return surEdt(() -> ((JLabel) lire(onglet, "barreStatut")).getText());
	}

	// vérifie que la barre d'état contient le texte attendu, en affichant le message réel en cas d'échec
	static void verifierStatut(Object onglet, String attendu) throws Exception {
		String statut = statut(onglet);
		assertTrue(statut.contains(attendu), "Message affiché : « " + statut + " », attendu : « " + attendu + " »");
	}

	static int lignes(Object onglet) throws Exception {
		return surEdt(() -> table(onglet).getRowCount());
	}

	static String texte(Object onglet, String champ) throws Exception {
		return surEdt(() -> ((JTextField) lire(onglet, champ)).getText());
	}

	// valeur affichée dans la colonne donnée, sur la ligne dont la première colonne vaut la clé (null si absente)
	static String valeur(Object onglet, Object cle, int colonne) throws Exception {
		return surEdt(() -> {
			JTable table = table(onglet);
			for (int i = 0; i < table.getRowCount(); i++) {
				if (String.valueOf(cle).equals(String.valueOf(table.getValueAt(i, 0)))) {
					return String.valueOf(table.getValueAt(i, colonne));
				}
			}
			return null;
		});
	}

	// première colonne de la ligne sélectionnée, ou null si aucune
	static String selection(Object onglet) throws Exception {
		return surEdt(() -> {
			JTable table = table(onglet);
			return table.getSelectedRow() < 0 ? null : String.valueOf(table.getValueAt(table.getSelectedRow(), 0));
		});
	}

	// texte de la dernière boîte de dialogue affichée (fiche ou demande de confirmation)
	static String dernierDialogue() {
		synchronized (dialogues) {
			return dialogues.isEmpty() ? "" : dialogues.get(dialogues.size() - 1);
		}
	}

	/*
	 * Mécanique interne
	 */

	private static JTable table(Object onglet) throws Exception {
		return (JTable) lire(onglet, "table");
	}

	// exécute dans le thread de Swing et attend la fin ; une erreur de l'interface fait échouer le test
	private static <T> T surEdt(Callable<T> traitement) throws Exception {
		List<T> resultat = new ArrayList<>();
		List<Throwable> erreur = new ArrayList<>();
		SwingUtilities.invokeAndWait(() -> {
			try {
				resultat.add(traitement.call());
			} catch (InvocationTargetException ex) {
				erreur.add(ex.getCause());
			} catch (Throwable ex) {
				erreur.add(ex);
			}
		});
		if (!erreur.isEmpty()) {
			throw new AssertionError("Erreur dans l'interface : " + erreur.get(0), erreur.get(0));
		}
		return resultat.get(0);
	}

	// toutes les 100 ms, répond aux boîtes de dialogue ouvertes et note leur contenu
	private static void demarrerRepondeur() {
		if (repondeur != null) {
			return;
		}
		repondeur = new Timer(100, e -> {
			for (Window fenetre : Window.getWindows()) {
				if (fenetre instanceof JDialog && fenetre.isShowing()) {
					JOptionPane dialogue = chercher((Container) fenetre, JOptionPane.class);
					if (dialogue != null && dialogue.getValue() == JOptionPane.UNINITIALIZED_VALUE) {
						synchronized (dialogues) {
							dialogues.add(String.valueOf(dialogue.getMessage()));
						}
						dialogue.setValue(dialogue.getOptionType() == JOptionPane.YES_NO_OPTION ? reponse
								: JOptionPane.OK_OPTION);
					}
				}
			}
		});
		repondeur.start();
	}

	private static Object lire(Object cible, String nom) throws Exception {
		for (Class<?> classe = cible.getClass(); classe != null; classe = classe.getSuperclass()) {
			try {
				Field champ = classe.getDeclaredField(nom);
				champ.setAccessible(true);
				return champ.get(cible);
			} catch (NoSuchFieldException ex) {
				// champ déclaré dans une classe parente : on remonte
			}
		}
		throw new NoSuchFieldException(nom);
	}

	private static Method methode(Object cible, String nom, Class<?>... parametres) throws Exception {
		for (Class<?> classe = cible.getClass(); classe != null; classe = classe.getSuperclass()) {
			try {
				Method methode = classe.getDeclaredMethod(nom, parametres);
				methode.setAccessible(true);
				return methode;
			} catch (NoSuchMethodException ex) {
				// méthode déclarée dans une classe parente : on remonte
			}
		}
		throw new NoSuchMethodException(nom);
	}

	private static <T> T chercher(Container conteneur, Class<T> type) {
		for (Component composant : conteneur.getComponents()) {
			if (type.isInstance(composant)) {
				return type.cast(composant);
			}
			if (composant instanceof Container) {
				T trouve = chercher((Container) composant, type);
				if (trouve != null) {
					return trouve;
				}
			}
		}
		return null;
	}

}
