package com.crea.jee.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.crea.jee.beans.Uv;
import com.crea.jee.utils.DBAction;

/*
 * Cette classe regroupe les accès en base de données liés à la table uv
 */
public class UvDao {

	// construit un objet Uv à partir d'une ligne du ResultSet
	private static Uv mapResultSet(ResultSet rs) throws SQLException {
		return new Uv(rs.getString("code"), rs.getInt("nbh"), rs.getString("coord"));
	}

	// récupère une uv à partir de son code
	public static Uv getUvByCode(String code) {
		Uv u = null;
		String request = "SELECT * FROM uv WHERE code = ?";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setString(1, code);
			try (ResultSet response = ps.executeQuery()) {
				if (response.next()) {
					u = mapResultSet(response);
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			DBAction.DBClose();
		}
		return u;
	}

	/*
	 * supprime une uv à partir de son code, retourne le nombre de lignes supprimées
	 * une uv est référencée par inscrit.code (contrainte inscrit_fk2, sans ON DELETE) :
	 * on supprime donc d'abord les inscriptions liées à cette uv avant de la supprimer
	 * elle-même, les deux requêtes formant une seule transaction
	 */
	public static int deleteUvByCode(String code) {
		int result = -1;
		DBAction.DBConnexion();
		try {
			DBAction.getCon().setAutoCommit(false);
			try (PreparedStatement supprimeInscriptions = DBAction.getCon()
					.prepareStatement("DELETE FROM inscrit WHERE code = ?");
					PreparedStatement supprimeUv = DBAction.getCon()
							.prepareStatement("DELETE FROM uv WHERE code = ?")) {
				supprimeInscriptions.setString(1, code);
				supprimeInscriptions.executeUpdate();

				supprimeUv.setString(1, code);
				result = supprimeUv.executeUpdate();

				DBAction.getCon().commit();
			} catch (SQLException ex) {
				DBAction.getCon().rollback();
				System.out.println(ex.getMessage());
			} finally {
				DBAction.getCon().setAutoCommit(true);
			}
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return result;
	}

	// met à jour le nombre d'heure de cours d'une uv identifiée par son code
	public static int updateNbhUv(String code, int nbh) {
		int result = -1;
		String request = "UPDATE uv SET nbh = ? WHERE code = ?";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setInt(1, nbh);
			ps.setString(2, code);
			result = ps.executeUpdate();
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return result;
	}

	// met à jour le coordinateur d'une uv identifiée par son code
	public static int updateCoordUv(String code, String coord) {
		int result = -1;
		String request = "UPDATE uv SET coord = ? WHERE code = ?";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setString(1, coord);
			ps.setString(2, code);
			result = ps.executeUpdate();
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return result;
	}

	// récupère la liste de toutes les uv
	public static List<Uv> getAllUvs() {
		List<Uv> liste = new ArrayList<>();
		String request = "SELECT * FROM uv";
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

	// récupère la liste des uv ayant un nombre d'heure supérieur à une valeur donnée
	public static List<Uv> getUvsNbhSuperieur(int valeur) {
		List<Uv> liste = new ArrayList<>();
		String request = "SELECT * FROM uv WHERE nbh > ?";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setInt(1, valeur);
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

}
