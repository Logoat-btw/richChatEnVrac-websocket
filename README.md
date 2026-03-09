💬 Rich Chat
Un petit chat enrichi et interactif, pensé pour la communication en temps réel.

📖 À propos du projet
Rich Chat est une application full-stack de messagerie instantanée développée dans le cadre de la formation MMI - BUT 3 DevWeb de l'IUT de Laval (modules R5.05 et R5.09). Conçu par Rémi Venant, ce projet pédagogique et technique a pour objectif d'offrir une plateforme de discussion performante.

La valeur ajoutée de cette application réside dans sa capacité à lier un éditeur de texte riche (permettant un formatage avancé des messages) à une infrastructure temps réel robuste, le tout sécurisé par des standards modernes de l'industrie.

✨ Fonctionnalités clés
Temps réel réactif : Envoi et réception instantanée des messages dans les différents salons de discussion grâce à l'utilisation des WebSockets.

Édition de texte riche : Les utilisateurs ne se contentent pas de texte brut ; ils peuvent formater leurs messages de manière complexe grâce à l'intégration d'un éditeur dédié (Draft.js).

Sécurité & Authentification : Protection des routes, gestion des sessions et sécurisation des échanges assurées par Spring Security et la cryptographie (BouncyCastle).

Persistance des données : Sauvegarde fiable des historiques de chat, des utilisateurs et des salons grâce à une base de données NoSQL.

🛠️ Technologies et outils
🎨 Frontend (Client)
React 19 & React Router : Pour une interface utilisateur sous forme de Single Page Application (SPA) fluide.

Bootstrap 5 & Sass : Pour le design, le layout et la responsivité.

WebSockets (SockJS & STOMP.js) : Pour la communication bidirectionnelle avec le serveur.

Axios : Pour la consommation de l'API REST.

Draft.js & DOMPurify : Pour l'édition de texte riche et la prévention des failles XSS.

⚙️ Backend (Serveur)
Java 21 : Langage principal du serveur.

Spring Boot 3.5 : Framework cœur gérant l'API REST, les WebSockets et la sécurité.

Spring Data MongoDB : Pour la gestion de la base de données orientée documents.

🏗️ Architecture / Comment ça marche
Le fonctionnement de Rich Chat repose sur une séparation claire entre l'interface utilisateur et la logique serveur :

La couche Client (Frontend) : Construite et empaquetée avec Webpack, l'application React s'occupe du rendu visuel. Lorsqu'un utilisateur navigue ou s'authentifie, l'application effectue des requêtes HTTP (REST) traditionnelles vers le serveur.

La couche Serveur (Backend) : Le serveur Spring Boot reçoit ces requêtes REST, valide les données (grâce au validateur intégré) et interroge MongoDB pour retourner les informations nécessaires (historique, listes de salons).

Le moteur Temps Réel : Une fois l'utilisateur dans un salon, le client ouvre une connexion WebSocket persistante. Le protocole STOMP (Simple Text Oriented Messaging Protocol) est utilisé par-dessus cette connexion. Cela permet au serveur de "pousser" activement et instantanément les nouveaux messages vers tous les utilisateurs abonnés à un salon spécifique, sans que le navigateur n'ait besoin de rafraîchir ou de relancer des requêtes.
