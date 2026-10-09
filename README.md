# MonTour API — File d'attente virtuelle en temps réel

[![CI](https://github.com/diongrace/montour-api/actions/workflows/ci.yml/badge.svg)](https://github.com/diongrace/montour-api/actions/workflows/ci.yml)

Backend de MonTour : on scanne un QR code, on reçoit un ticket sur son téléphone et on suit son tour en direct.

**Frontend** : https://github.com/diongrace/montour-web
**Démo en ligne** : bientôt — compte agent : `agent@montour.ci` / `MonTour2026!`

## Stack

Java 17 · Spring Boot 4 · Spring Security (JWT) · WebSocket STOMP · PostgreSQL · Flyway · JUnit 5

## Lancer en local

Prérequis : Java 17+, PostgreSQL avec une base `montour`.

1. Crée un fichier `secrets.properties` à la racine (il n'est jamais envoyé sur GitHub) :
   ```properties
   spring.datasource.username=postgres
   spring.datasource.password=<ton mot de passe>
   ```
2. Lance l'application :
   ```bash
   ./mvnw spring-boot:run
   ```
   Flyway crée les tables et les données de démo (« Centre de santé Démo »).
3. Lance les tests (base H2 en mémoire, pas besoin de PostgreSQL) :
   ```bash
   ./mvnw test
   ```

## Choix techniques

- Les transitions de statut (EN_ATTENTE → APPELE → SERVI / ABSENT) vivent dans l'entité `Ticket` : aucune transition interdite n'est possible (409).
- Un verrou par service et une contrainte d'unicité garantissent des numéros sans doublon.
- Les messages WebSocket partent seulement après le commit de la transaction (`@TransactionalEventListener`).
- Le client n'a pas de compte : un jeton UUID aléatoire protège son ticket.

## Et ensuite (V2)

Notifications SMS, rendez-vous à l'avance, application Flutter, plusieurs établissements.
