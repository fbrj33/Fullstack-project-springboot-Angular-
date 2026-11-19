# Aura Hub - Backend (Spring Boot 3 / Java 17)

API sécurisée (JWT, rôles ADMIN / CONSEILLER) de la plateforme de gestion de la maison de mode **Maison Aura** :
collections, pièces (multi-images), boutiques, stocks par taille, clients privés, commandes et statistiques,
plus un lookbook public sans prix.

## Prérequis
- JDK 17+ et Maven 3.9+
- MySQL 8 (la base `aurahub` est créée automatiquement si l'utilisateur a le droit `CREATE`)

## Installation
```bash
# 1. (optionnel) variables d'environnement - valeurs par défaut dans application.properties
export DB_USER=root DB_PASSWORD=monmotdepasse
export JWT_SECRET="une-longue-chaine-aleatoire-d-au-moins-32-caracteres"
export MAIL_USER=... MAIL_PASSWORD=...      # Mailtrap ou mot de passe d'application Gmail

# 2. lancer (premier démarrage : tables + rôles + compte admin créés automatiquement)
mvn spring-boot:run

# 3. charger le jeu de données fictif
mysql -u root -p aurahub < sql/aurahub_seed.sql
```
API : http://localhost:8080 - Comptes de test :

| Rôle | Email | Mot de passe |
|---|---|---|
| ADMIN | admin@aura.com | Admin123! |
| CONSEILLER | marie@aura.com | Conseil123! |
| CONSEILLER | youssef@aura.com | Conseil123! |

> Un compte créé par `/api/auth/register` est **désactivé** : l'admin reçoit un email puis l'active
> (`PUT /api/utilisateurs/{id}/activer`). Si l'email n'est pas configuré, l'inscription fonctionne quand même
> (l'échec d'envoi est simplement loggé).

## Build du jar
```bash
mvn clean package -DskipTests      # produit target/aurahub.jar
java -jar target/aurahub.jar
```
Le build Angular copié dans `src/main/resources/static/` avant `mvn package` est servi par Spring Boot
(repli automatique sur `index.html` pour les routes Angular). Fichiers de déploiement VPS : dossier `deploy/`.

## Architecture
```
com.sip.aurahub
 ├─ entity/      entités JPA + énumérations
 ├─ repository/  Spring Data JPA (requêtes JPQL de stats, verrous pessimistes)
 ├─ dto/         DTO (records) + Mappers
 ├─ service/     logique métier (CommandeService = règles RG1..RG7)
 ├─ controller/  REST
 ├─ security/    JWT (JwtService, JwtAuthFilter), UserDetailsService, CurrentUser
 ├─ config/      SecurityConfig, WebConfig (uploads + SPA), DataInitializer
 └─ exception/   @RestControllerAdvice + BusinessException (409)
```

## Règles de gestion : où elles sont implémentées
| RG | Où |
|---|---|
| RG1 stock jamais négatif | `CommandeService.create/confirmer` (+ verrou `findForUpdate`), `@Min(0)` sur `StockDto` |
| RG2 tirage + n° d'exemplaire | `CommandeService.confirmer` -> `prochainNumero` (plus petit numéro libre <= tirageMax) ; 1 exemplaire par ligne |
| RG3 collection EN_VENTE | `CommandeService.verifierCommandable` |
| RG4 accès anticipé 48 h | `verifierCommandable` + `CollectionMode.dateMiseEnVente` (posée au passage EN_VENTE) ; durée : `app.early-access-hours` |
| RG5 conseiller = ses clients | `ClientService` (`checkAccess`, `findAll`) et `CommandeService.findAll` |
| RG6 décrément / restitution | `confirmer` / `annuler` (`@Transactional`) |
| RG7 niveau client | `ClientService.recalculerNiveau` (cumul 12 mois) ; seuils : `app.niveau.prestige` / `app.niveau.elite` ; recalcul nocturne `@Scheduled` |

## Endpoints principaux
| Méthode | URL | Accès |
|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | public |
| GET | `/api/public/collections/en-cours`, `/api/public/collections/{id}/pieces`, `/api/public/boutiques` | public (sans prix) |
| GET | `/api/me` | connecté |
| CRUD | `/api/collections`, `/api/pieces`, `/api/boutiques`, `/api/stocks` | lecture : tous - écriture : ADMIN |
| POST | `/api/pieces/{id}/images` (multipart, champ `files`) | ADMIN |
| GET | `/api/pieces/{id}/disponibilite` | connecté |
| GET | `/api/stocks/faible?seuil=3` | connecté |
| CRUD | `/api/clients` (+ `/{id}/historique`) | ADMIN : tous - CONSEILLER : les siens |
| POST / PUT | `/api/commandes`, `/{id}/confirmer`, `/{id}/annuler`, `/{id}/livrer` | connecté |
| GET | `/api/stats` | ADMIN |
| CRUD | `/api/utilisateurs` (+ `/{id}/activer`) | ADMIN |

Codes d'erreur : 400 validation - 401 non authentifié - 403 rôle insuffisant / compte non validé /
client d'un autre conseiller - 404 introuvable - 409 règle de gestion violée.

## Tests (Postman)
Importer `postman/AuraHub.postman_collection.json`, charger le seed, lancer **Login ADMIN** et **Login CONSEILLER**
(les tokens sont mémorisés), puis exécuter le dossier voulu ou toute la collection (Runner). Le dossier
« 10 - Sécurité » vérifie les 403 d'un CONSEILLER sur les écritures des collections et pièces, les ajustements de
stock et l'accès utilisateurs ; les consultations autorisées restent accessibles selon le tableau des rôles.

## Livrables du dossier
`sql/aurahub_seed.sql` (jeu de données), `postman/` (collection), `docs/class-diagram.md` (diagramme Mermaid à exporter en image), `deploy/` (systemd + Nginx).
