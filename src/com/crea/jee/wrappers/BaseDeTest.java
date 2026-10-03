package com.crea.jee.wrappers;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.crea.jee.utils.DBAction;

/*
 * Outils communs aux tests des DAO, exécutés contre la base ecole_test (-Ddb.name=ecole_test)
 */
final class BaseDeTest {

	private static final String BASE_DE_TEST = "ecole_test";

	private BaseDeTest() {
	}

	// les tests vident les tables : on refuse de tourner ailleurs que sur la base de test
	private static void verifierBaseDeTest() {
		String base = System.getProperty("db.name");
		if (!BASE_DE_TEST.equals(base)) {
			throw new IllegalStateException("Tests lancés sur la base '" + (base == null ? "ecole" : base)
					+ "' : ajouter -Ddb.name=" + BASE_DE_TEST + " aux VM options pour ne pas effacer les vraies données");
		}
	}

	/*
	 * vide toutes les tables pour repartir d'un état connu
	 * eleve.no et chambre.num se référencent mutuellement (FK sans ON DELETE) : on les remet
	 * à NULL avant de supprimer quoi que ce soit, puis on supprime les tables filles avant les parentes
	 */
	static void viderLesTables() throws SQLException {
		verifierBaseDeTest();
		DBAction.DBConnexion();
		try (Statement stm = DBAction.getCon().createStatement()) {
			stm.executeUpdate("UPDATE eleve SET no = NULL");
			stm.executeUpdate("UPDATE chambre SET num = NULL");
			stm.executeUpdate("DELETE FROM inscrit");
			stm.executeUpdate("DELETE FROM livre");
			stm.executeUpdate("DELETE FROM chambre");
			stm.executeUpdate("DELETE FROM eleve");
			stm.executeUpdate("DELETE FROM uv");
		} finally {
			DBAction.DBClose();
		}
	}

	// exécute une requête de mise en place des données qu'aucune méthode DAO ne permet (ajout d'uv, d'inscription...)
	static void executer(String sql, Object... parametres) throws SQLException {
		verifierBaseDeTest();
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(sql)) {
			for (int i = 0; i < parametres.length; i++) {
				ps.setObject(i + 1, parametres[i]);
			}
			ps.executeUpdate();
		} finally {
			DBAction.DBClose();
		}
	}

}
