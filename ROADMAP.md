# 📋 API Documentation

## 🔐 Authentification

- `POST /api/auth/login` - Connexion
- `POST /api/auth/logout` - Déconnexion
- `POST /api/auth/register` - Inscription
- `POST /api/auth/refresh` - Rafraîchir token
- `GET /api/auth/reset` - Reset password
- `GET /api/auth/forgot` - Forgot password `TODO - DOING`

## 👤 Utilisateurs

- `GET /api/users` - Liste utilisateurs (admin)
- `POST /api/users` - Crée un nouvel utilisateur (admin)
- `GET /api/users/{id}` - Récupérer son user (admin & user)
- `PUT /api/users/{id}` - Modifier son user (admin & user)
- `DELETE /api/users/{id}` - Supprimer user (admin)
- `GET /api/users/email` - get user by email (admin)
- `GET /api/users/count` - Total d'utilisateurs (admin
- `PUT /api/users/:id/role` - Modifier rôle (admin) `TODO`

## 📊 Sondages `TODO`

- `GET /api/surveys` - Lister mes sondages (owner)
- `POST /api/surveys` - Créer un sondage
- `GET /api/surveys/:id` - Détails sondage
- `PUT /api/surveys/:id` - Modifier sondage
- `DELETE /api/surveys/:id` - Supprimer sondage
- `GET /api/survey/count` - Total de sondage (admin)

### TODO

- `POST /api/surveys/:id/publish` - Publier sondage
- `POST /api/surveys/:id/unpublish` - Dépublier sondage
- `POST /api/surveys/:id/clone` - Dupliquer sondage

## ❓ Questions `TODO`

- `GET /api/surveys/:id/questions` - Questions d'un sondage
- `POST /api/surveys/:id/questions` - Ajouter question
- `PUT /api/questions/:id` - Modifier question
- `DELETE /api/questions/:id` - Supprimer question
- `PUT /api/surveys/:id/questions/reorder` - Réordonner questions

## 📝 Réponses `TODO`

- `POST /api/surveys/:id/responses` - Soumettre réponses
- `GET /api/surveys/:id/responses` - Voir réponses (owner)
- `GET /api/surveys/:id/responses/analytics` - Statistiques
- `GET /api/surveys/:id/responses/export` - Exporter données

## 🎯 Quiz (Spécifique) `TODO`

- `GET /api/quiz/:id/results` - Résultats détaillés
- `GET /api/quiz/:id/leaderboard` - Classement
- `GET /api/quiz/:id/correction` - Correction

## 📚 Historique `TODO`

- `GET /api/surveys/:id/history` - Historique modifications
- `POST /api/surveys/:id/history/:id/restore` - Restaurer version

## 🔗 Partage & Accès `TODO`

- `GET /api/surveys/public/:token` - Accès public sondage
- `POST /api/surveys/:id/share` - Générer lien partage

## ⚙️ Administration

- `GET /api/admin/surveys` - Tous les sondages (admin) `TODO`
- `GET /api/admin/health` - Etat de l'api (admin)
- `GET /api/admin/metric` - Stats global de l'api (admin)
- `GET /api/swagger-ui` - Swagger de l'api (public)
- `GET /api/openapi.json` - Download json Swagger de l'api (public)
- `GET /api/openapi.yaml` - Download yaml Swagger de l'api (public)
