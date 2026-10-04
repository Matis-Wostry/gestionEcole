package com.crea.jee.ihm;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/*
 * Petite interface d'application (Swing, inclus dans le JDK) pour montrer les DAO en action, un onglet par table
 * Lancer la classe (docker-compose démarré) : base ecole par défaut, -Ddb.name=ecole_test pour la base de test
 */
public class FenetreEcole {

	private final JFrame fenetre = new JFrame("Gestion de l'école");
	private final JTabbedPane onglets = new JTabbedPane();
	private final Onglet[] contenus = { new OngletEleves(), new OngletChambres(), new OngletLivres(),
			new OngletUv(), new OngletInscriptions() };

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ex) {
				// style natif indisponible : on garde le style Swing par défaut
			}
			new FenetreEcole().afficher();
		});
	}

	/*
	 * Constructeur
	 */

	// assemble les onglets ; chaque onglet se recharge quand on l'ouvre, pour refléter les changements faits ailleurs
	public FenetreEcole() {
		String[] titres = { "Élèves", "Chambres", "Livres", "UV", "Inscriptions" };
		for (int i = 0; i < contenus.length; i++) {
			onglets.addTab(titres[i], contenus[i].getPanneau());
		}
		onglets.addChangeListener(e -> contenus[onglets.getSelectedIndex()].actualiser());
		contenus[0].actualiser();

		fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fenetre.add(onglets);
		fenetre.setSize(980, 620);
		fenetre.setLocationRelativeTo(null);
	}

	// affiche la fenêtre à l'écran
	public void afficher() {
		fenetre.setVisible(true);
	}

}
