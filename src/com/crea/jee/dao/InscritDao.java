package com.crea.jee.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.crea.jee.beans.Inscrit;
import com.crea.jee.utils.DBAction;

/*
 * Cette classe regroupe les accès en base de données liés à la table inscrit
 */
public class InscritDao {

	// construit un objet Inscrit à partir d'une ligne du ResultSet
	private static Inscrit mapResultSet(ResultSet rs) throws SQLException {
		return new Inscrit(rs.getString("code"), rs.getString("num"), rs.getFloat("note"));
	}

	// supprime une inscription d'un élève à une uv particulière, retourne le nombre de lignes supprimées
	public static int deleteInscription(String code, String num) {
		int result = -1;
		String request = "DELETE FROM inscrit WHERE code = ? AND num = ?";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setString(1, code);
			ps.setString(2, num);
			result = ps.executeUpdate();
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return result;
	}

	// récupère la liste de toutes les inscriptions à des uv
	public static List<Inscrit> getAllInscriptions() {
		List<Inscrit> liste = new ArrayList<>();
		String request = "SELECT * FROM inscrit";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			try (ResultSet response = ps.executeQuery()) {
				while (response.next()) {
					liste.add(mapResultSet(response));
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			DBAction.DBClose();
		}
		return liste;
	}

	// met à jour la note d'un élève inscrit à une uv particulière
	public static int updateNoteInscrit(String code, String num, float note) {
		int result = -1;
		String request = "UPDATE inscrit SET note = ? WHERE code = ? AND num = ?";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setFloat(1, note);
			ps.setString(2, code);
			ps.setString(3, num);
			result = ps.executeUpdate();
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return result;
	}

}
