package com.crea.jee.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.crea.jee.beans.Uv;
import com.crea.jee.utils.DBAction;
import com.crea.jee.utils.Validation;
import com.crea.jee.wrappers.UvWrapper;
import com.crea.jee.wrappers.Wrapper;

/*
 * Cette classe regroupe les accès en base de données liés à la table uv
 * Les méthodes d'écriture retournent le nombre de lignes modifiées, ou un code d'erreur :
 * -1 = erreur SQL, -3 = données invalides (refusées avant tout accès à la base)
 * Les lectures d'une seule uv retournent un UvWrapper (l'uv + un code réponse, voir Wrapper)
 * Les lectures de listes retournent une liste vide si rien n'est trouvé ou en cas d'erreur
 */
public class UvDao {

	// construit un objet Uv à partir d'une ligne du ResultSet
	private static Uv mapResultSet(ResultSet rs) throws SQLException {
		return new Uv(rs.getString("code"), rs.getInt("nbh"), rs.getString("coord"));
	}

	// récupère une uv à partir de son code, avec le code réponse de la recherche
	public static UvWrapper getUvByCode(String code) {
		if (!Validation.estValide(code, 100)) {
			return new UvWrapper(null, Wrapper.DONNEES_INVALIDES);
		}
		if (DBAction.DBConnexion() != null) {
			return new UvWrapper(null, Wrapper.ERREUR_BASE);
		}
		String request = "SELECT * FROM uv WHERE code = ?";
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setString(1, code);
			try (ResultSet response = ps.executeQuery()) {
				if (response.next()) {
					return new UvWrapper(mapResultSet(response), Wrapper.TROUVE);
				}
				return new UvWrapper(null, Wrapper.NON_TROUVE);
			}
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
			return new UvWrapper(null, Wrapper.ERREUR_BASE);
		} finally {
			DBAction.DBClose();
		}
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

	// met à jour le nombre d'heuresde cours d'une uv identifiée par son code
	public static int updateNbhUv(String code, int nbh) {
		if (!Validation.estPositif(nbh) || !Validation.estDansPlageTinyint(nbh)) {
			return -3;
		}
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
		if (!Validation.longueurValide(coord, 255)) {
			return -3;
		}
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
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return liste;
	}

	// récupère la liste des uv ayant un nombre d'heuressupérieur à une valeur donnée
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
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return liste;
	}

}
