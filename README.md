# Projet ÉCOLE : couche persistance Java & MySQL

Couche **Model** (persistance) d'une application de gestion d'école : beans, DAO JDBC, wrappers et tests unitaires JUnit, sur une base MySQL.

DEV9 CREA, année 2026-2027 · Encadrant : Taha RIDENE

**Document de synthèse complet (sujet, diagramme de BD, diagramme de classes, tests) : [`docs/`](docs/README.md)**

## Démarrage rapide

1. Démarrer MySQL (bases `ecole` et `ecole_test`) et phpMyAdmin :

   ```bash
   docker compose up -d
   ```

2. Ouvrir le projet dans IntelliJ IDEA (ou Eclipse, en ajoutant les `.jar` de `lib/` au *Build Path*).

3. Lancer les tests : clic droit sur le package `com.crea.jee.junit` puis *Run*, avec `-Ddb.name=ecole_test` dans les *VM options*.

## Contenu

| Dossier / fichier | Rôle |
|---|---|
| `src/com/crea/jee/beans` | Une classe par table |
| `src/com/crea/jee/dao` | Accès aux données (spécification fonctionnelle) |
| `src/com/crea/jee/wrappers` | Objet renvoyé + code réponse |
| `src/com/crea/jee/utils` | Connexion à la base, contrôle des données |
| `src/com/crea/jee/junit` | Tests unitaires JUnit 5 |
| `src/com/crea/jee/test` | Tests manuels (affichage console) |
| `docker-compose.yml` | MySQL 8.4 et phpMyAdmin |
| `lib/` | Driver MySQL et JUnit |
| `data/` | Scripts SQL de création et de remplissage des bases (`ecole.sql`, `ecole_test.sql`) |
| `docs/` | Document de synthèse |
