package com.crea.jee.test;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.crea.jee.beans.Uv;
import com.crea.jee.dao.UvDao;
import com.crea.jee.utils.DBAction;

/*
 * Test manuel de UvDao : appelle chaque méthode et affiche le résultat dans la console
 * Prévu pour les données de démo de la base ecole (JAVA_Grp1...) : l'uv de test TEST_UV
 * est supprimée à la fin, la base revient à son état initial
 * Version automatisée avec vérifications : com.crea.jee.wrappers.UvDaoTest
 */
public class UvTest {
	public static void main(String[] args) {

		System.out.println("=== getUvByCode ===");
		Uv u1 = UvDao.getUvByCode("JAVA_Grp1");
		if (u1 != null) {
			u1.affiche();
		} else {
			System.out.println("Aucune uv JAVA_Grp1");
		}

		System.out.println("=== getAllUvs ===");
		List<Uv> toutes = UvDao.getAllUvs();
		toutes.forEach(Uv::affiche);

		System.out.println("=== getUvsNbhSuperieur ===");
		List<Uv> longues = UvDao.getUvsNbhSuperieur(20);
		longues.forEach(Uv::affiche);

		/*
		 * pas de méthode "ajouter une uv" dans la spec : on insère une uv de test
		 * directement en SQL pour tester update/delete sans toucher aux uv fournies
		 */
		insererUvDeTest();

		System.out.println("=== updateNbhUv ===");
		int majNbh = UvDao.updateNbhUv("TEST_UV", 15);
		System.out.println("Lignes mises à jour : " + majNbh);
		Uv u2 = UvDao.getUvByCode("TEST_UV");
		if (u2 != null) {
			u2.affiche();
		}

		System.out.println("=== updateCoordUv ===");
		int majCoord = UvDao.updateCoordUv("TEST_UV", "Mme TEST");
		System.out.println("Lignes mises à jour : " + majCoord);
		Uv u3 = UvDao.getUvByCode("TEST_UV");
		if (u3 != null) {
			u3.affiche();
		}

		System.out.println("=== deleteUvByCode ===");
		int suppr = UvDao.deleteUvByCode("TEST_UV");
		System.out.println("Lignes supprimées : " + suppr);
		Uv u4 = UvDao.getUvByCode("TEST_UV");
		if (u4 == null) {
			System.out.println("TEST_UV a bien été supprimée");
		}
	}

	// insère une uv de test directement en SQL (pas de méthode addUv dans la spec)
	private static void insererUvDeTest() {
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon()
				.prepareStatement("INSERT INTO uv (code, nbh, coord) VALUES ('TEST_UV', 10, 'Mr TEST')")) {
			ps.executeUpdate();
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
	}
}
