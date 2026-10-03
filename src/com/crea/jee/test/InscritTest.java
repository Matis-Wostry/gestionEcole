package com.crea.jee.test;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.crea.jee.beans.Inscrit;
import com.crea.jee.dao.InscritDao;
import com.crea.jee.utils.DBAction;

/*
 * Test manuel de InscritDao : appelle chaque méthode et affiche le résultat dans la console
 * Prévu pour les données de démo de la base ecole (JAVA_Grp1, AGUE001) : l'inscription de test
 * est supprimée à la fin, la base revient à son état initial
 * Version automatisée avec vérifications : com.crea.jee.wrappers.InscritDaoTest
 */
public class InscritTest {
	public static void main(String[] args) {

		/*
		 * pas de méthode "ajouter une inscription" dans la spec : on insère une
		 * inscription de test directement en SQL pour pouvoir tester le reste
		 */
		insererInscriptionDeTest();

		System.out.println("=== getAllInscriptions ===");
		List<Inscrit> toutes = InscritDao.getAllInscriptions();
		toutes.forEach(Inscrit::affiche);

		System.out.println("=== updateNoteInscrit ===");
		int maj = InscritDao.updateNoteInscrit("JAVA_Grp1", "AGUE001", 15.5f);
		System.out.println("Lignes mises à jour : " + maj);
		InscritDao.getAllInscriptions().forEach(Inscrit::affiche);

		System.out.println("=== deleteInscription ===");
		int suppr = InscritDao.deleteInscription("JAVA_Grp1", "AGUE001");
		System.out.println("Lignes supprimées : " + suppr);
		System.out.println("Nombre d'inscriptions restantes : " + InscritDao.getAllInscriptions().size());
	}

	// insère une inscription de test directement en SQL (pas de méthode addInscrit dans la spec)
	private static void insererInscriptionDeTest() {
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon()
				.prepareStatement("INSERT INTO inscrit (code, num, note) VALUES ('JAVA_Grp1', 'AGUE001', 12.5)")) {
			ps.executeUpdate();
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
	}
}
