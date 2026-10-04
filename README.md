# Gestion d'école

Application Java de gestion d'une école : élèves, chambres, livres, UV et inscriptions.

Les données sont stockées dans une base MySQL et manipulées en JDBC. Une interface graphique permet de les consulter, d'en ajouter, de les modifier et de les supprimer.

## Documentation

Le dossier [`docs/`](docs/README.md) contient un README plus détaillé, avec :

- l'**architecture** du projet et le rôle de chaque package ;
- le **diagramme de la base de données** ;
- les **diagrammes de classes** (beans, wrappers, DAO) ;
- la **liste des fonctionnalités** et la méthode qui réalise chacune ;
- les **codes retour** des DAO et les **règles de contrôle** des données ;
- le détail des **tests** ;
- l'**installation** et les commandes de lancement.

## Démarrage rapide

1. Démarrer MySQL (bases `ecole` et `ecole_test`) et phpMyAdmin :

   ```bash
   docker compose up -d
   ```

2. Ouvrir le projet :
   - **Eclipse** : *File > Import > General > Existing Projects into Workspace*, puis choisir ce dossier ;
   - **IntelliJ IDEA** : ouvrir directement ce dossier.

   Dans les deux cas, les bibliothèques de `lib/` sont déjà déclarées.

3. Lancer les tests : clic droit sur le package `com.crea.jee.junit` puis *Run*, avec `-Ddb.name=ecole_test` dans les *VM options*.

4. Lancer l'application de démonstration **depuis la classe `FenetreEcole`** (`src/com/crea/jee/ihm/FenetreEcole.java`) : clic droit sur ce fichier, puis *Run 'FenetreEcole.main()'*. Une fenêtre s'ouvre avec un onglet par table (Élèves, Chambres, Livres, UV, Inscriptions).

   > **Important :** `FenetreEcole` est la **seule** classe à lancer, c'est elle qui contient le `main`. Les classes `Onglet…` du même dossier ne sont que des morceaux de la fenêtre : elles n'ont pas de `main` et ne peuvent pas être exécutées seules.

## Contenu

| Dossier / fichier | Rôle |
|---|---|
| `src/com/crea/jee/beans` | Une classe par table |
| `src/com/crea/jee/dao` | Accès aux données (spécification fonctionnelle) |
| `src/com/crea/jee/wrappers` | Objet renvoyé + code réponse |
| `src/com/crea/jee/utils` | Connexion à la base, contrôle des données |
| `src/com/crea/jee/junit` | Tests unitaires JUnit 5, de la couche persistance et de l'interface |
| `src/com/crea/jee/test` | Tests manuels (affichage console) |
| `src/com/crea/jee/ihm` | Application de démonstration (fenêtre Swing à onglets), à lancer depuis `FenetreEcole` |
| `docker-compose.yml` | MySQL 8.4 et phpMyAdmin |
| `lib/` | Driver MySQL et JUnit |
| `data/` | Scripts SQL de création et de remplissage des bases (`ecole.sql`, `ecole_test.sql`) |
| `docs/` | Document de synthèse |
