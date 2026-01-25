import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { getCSRFToken } from './restNetwork';

/**
 * Gestionnaire de client Websocket Stomp over SockJS pour rich-chat
 */
class WebsocketManager {
  #endpoint; // URL de connexion
  #extraClientOptions; // options supplémentaires à transmettre à la connexion (optionnel)
  #client; // client stomp;

  // abonnés, comprenant tout ou partie des fonctions de notifications suivantes
  // et comprennant optionnellement un roomId pour être notifié des messages du topic de la room (onTopicRoomMessage)
  // listener : {onChangeState:func(state), onUserErrorMessage(message), onUserRoomsMessage(message), onTopicRoomMessage(roomId, message), roomId}
  #listeners = new Set();

  // topic listeners internes actifs de roomId
  // map de type <roomId, {listener: listener ws interne, count: nb listeners externes associé}>
  #activeTopicWSListeners = new Map();

  constructor(endpoint, extraClientOptions = {}) {
    this.#endpoint = endpoint;
    this.#extraClientOptions = extraClientOptions;
    this.#createClient();
  }

  /**
   * Retourne l'état du client stomp
   * 0: ACTIVE 1: DEACTIVATING, 2:INACTIVE
   */
  get state() {
    return this.#client.state;
  }

  /**
   * Retourne true si le client stomp est actif (connecté)
   */
  get isActive() {
    return this.#client.connected;
  }

  /**
   * Active le client stomp. La fonction retourne sans attendre l'activation effective
   * @returns le gestionnaire
   */
  connect() {
    // Si déjà connecté, passe
    if (this.isActive) {
      return this;
    }
    this.#client.activate();
    return this;
  }

  /**
   * Desactive le client. La fonction asynchrone retourne une promesse toujours résolue
   * une fois que le client est désactivé (le cas échéant)
   * @returns
   */
  async disconnect() {
    try {
      await this.#client.deactivate();
    }
    catch (e) {
      console.warn(`STOMP Client Discconect error: ${e.message}`);
    }
    return this; // Juste pour retourner une information de base à la promesse
  }

  /**
   * Ajout un abonné pouvant être associé ou non à une room, et présentant tout ou partie des fonctions
   * de notification selon les messages reçue
   * listener : {onChangeState:func(state), onUserErrorMessage(message), onUserRoomsMessage(message), onTopicRoomMessage(roomId, message), roomId}
   * @param {Object} listener
   * @returns une fonction de désabonnement
   */
  subscribe(listener) {
    // {onChangeState:func(state)?, onUserErrorMessage(message), onUserRoomsMessage(message), onTopicRoomMessage(roomId, message), roomId}
    // Tente d'ajouter l'abonné
    const oldSize = this.#listeners.size;
    this.#listeners.add(listener);
    if (listener.roomId && this.#listeners.size > oldSize && this.isActive) {
      // Si l'abonné présente un id de room, qu'il est nouveau et que le client est actif
      // active la souscription au topic de la room
      this.#subscribeToRoomTopic(listener.roomId);
    }
    if (listener.onChangeState) {
      // Si l'abonné présente une fonction de notification en cas de changement d'état,
      // envoie une première notification pour informer de l'état actuel
      listener.onChangeState(this.isActive, this.state);
    }
    return () => {
      // Désabonnement
      // Retire l'abonné
      this.#listeners.delete(listener);
      if (listener.roomId) {
        // Si l'abonné avait une roomId, décompte le nombre d'abonné pour le listener interne au topic
        const topicLst = this.#activeTopicWSListeners.get(listener.roomId);
        topicLst.count -= 1;
        if (topicLst.count <= 0) {
          // Si le listener interne n'a plus d'abonné, se désabonne du topic
          topicLst.listener.unsubscribe();
          // Retire le listener au topic de la room, qui n'est plus actif
          this.#activeTopicWSListeners.delete(listener.roomId);
        }
      }
    };
  }

  /**
   * Envoie un message de room
   * @param {String} roomId
   * @param {Object} message
   */
  sendRoomMessage(roomId, message) {
    this.#publish(`/app/rooms/${roomId}`, {
      orderType: 'addMessage',
      roomId,
      message,
    });
  }

  /**
   * Supprime un message de room
   * @param {String} roomId
   * @param {Object} message
   * @param {boolean} isOwner si la demande vient du propriétaire de la room
   */
  removeRoomMessage(roomId, messageId, isOwner) {
    this.#publish(`/app/rooms/${roomId}`, {
      orderType: 'removeMessage',
      roomId,
      messageId,
      owner: isOwner,
    });
  }

  /**
   * Créer un client Stomp over SockJS avec un relai de messages vers les listenenrs
   */
  async #createClient() {
    // Création du client Stomp over SockJS
    // Le client est configuré par défaut pour se reconnecter automatiquement en cas
    // de déconnection après la première tenative de connexion et tant que l'on ne lui
    // demande pas explicitement de se connecter
    this.#client = new Client({
      ...this.#extraClientOptions, // injection des options supplémentaire
      webSocketFactory: () => new SockJS(this.#endpoint), // Fabrique de websocket: SockJS
      beforeConnect: async (client) => {
        // Traitement avant de se connecter : récupérer le token CSRF et l'injecter le cas échéant
        try {
          const { headerName, token } = await getCSRFToken();
          client.connectHeaders = { [headerName]: token };
        }
        catch {
          console.warn('Unable to fetch CSRF token!');
        }
      },
      onChangeState: (/* s */) => {
        // Invoqué dès que le client stomp change d'état
        // Do nothing
      },
      onConnect: () => {
        // Invoqué à chaque fois que le client passe à l'état actif
        for (const listener of this.#listeners) {
          // Informe tous les abonnés qui le souhaitent du passage à l'état actif
          if (listener.onChangeState) {
            listener.onChangeState(this.isActive, this.state);
          }
        }
        // S'inscrire à la file de message pour l'utilisateur et pour les erreurs
        this.#subscribeToUserErrorAndRoomsQueues();
        // Souscrire à tous les topics de rooms possiblement présent
        this.#reconnectListenersToRoomTopics();
      },
      onDisconnect: (/* iMessage */) => {
        // nothing to do
      },
      onStompError: (iMessage) => {
        // Invoqué lorsqu'une erreur stomp survient
        // cette erreur entrainera toujours la deconnection
        console.warn('Stomp error', iMessage);
      },
      onWebSocketClose: (/* evt */) => {
        // Invoqué lorsque le client est désactivé
        // nothing to do
      },
      onWebSocketError: (evt) => {
        // Invoqué lorsqu'une erreur de websocket (stomp, sockjs ou techno sous-jacente) survient
        // une erreur n'entraine pas forcément la deconnexion
        console.warn('Websocket error', evt);
      },
      // debug à activer si l'on veut les message de débug de stomp dans la console
      // debug: (str) => {
      //   console.log('debug', str);
      // },
    });
  }

  /**
   * Souscription interne à la file de message de l'utilisateur (concernant les rooms) et à la file d'erreur de l'utilisateur
   */
  #subscribeToUserErrorAndRoomsQueues() {
    this.#subscribe(`/user/queue/errors`, (error) => {
      // Informe tous les abonnés qui le souhaitent de l'erreur applicative
      for (const listener of this.#listeners) {
        if (listener.onUserErrorMessage) {
          listener.onUserErrorMessage(error);
        }
      }
    });
    this.#subscribe(`/user/queue/rooms`, (data) => {
      // Informe tous les abonnés qui le souhaitent du message reçue sur la file utilisateur
      for (const listener of this.#listeners) {
        if (listener.onUserRoomsMessage) {
          listener.onUserRoomsMessage(data);
        }
      }
    });
  }

  /**
   * Souscription interne à tous les topics de rooms nécessaire d'après les abonnés
   */
  #reconnectListenersToRoomTopics() {
    // Créer une nouvelle map de listener WS de topics
    this.#activeTopicWSListeners = new Map();
    // Procdèe à la souscription des rooms d'après les abonnés qui en ont spécifié une
    for (const listener of this.#listeners) {
      if (listener.roomId) {
        this.#subscribeToRoomTopic(listener.roomId);
      }
    }
  }

  /**
   * Souscription interne à un topic de room si ça n'est pas déjà le cas et incrémentation
   * du compteur d'abonnés liés à ce topic
   * @param {*} roomId
   */
  #subscribeToRoomTopic(roomId) {
    if (this.#activeTopicWSListeners.has(roomId)) {
      // Si le topic est déjà actif, incrémente simplement le compteur d'abonnés liés.
      this.#activeTopicWSListeners.get(roomId).count += 1;
    }
    else {
      // Effectue la souscription effective au topic
      const wsListener = this.#subscribe(`/topic/rooms/${roomId}`, (data) => {
        // Informe tous les abonnés en lien avec ce topic de room du message reçue
        for (const listener of this.#listeners) {
          if (listener.roomId === roomId) {
            listener.onTopicRoomMessage(roomId, data);
          }
        }
      });
      // Ajoute le listener de topic à la map de listeners WS de topic
      this.#activeTopicWSListeners.set(roomId, { listener: wsListener, count: 1 });
    }
  }

  /**
   * Envoie un message sur à un canal de destination de la websocket.
   * S'occupe de la serialisation en Json du message à envoyer
   * @param {string} destination le canal de destination
   * @param {object} message le message à transmettre
   */
  #publish(destination, message) {
    if (!this.isActive) {
      throw new Error('Socket not connected or inactive');
    }
    this.#client.publish({ destination, body: JSON.stringify(message, null, 0) });
  }

  /**
   * souscrit à un canal de destination de la websocket.
   * S'occupe de la deserialisation Json à la reception d'un message
   * @param {string} destination
   * @param {*} listener le callback interne à invoquer à la reception d'un message
   * @returns le listener stomp, objet ayant une methode de désabonnement (unsubscribe)
   */
  #subscribe(destination, listener) {
    return this.#client.subscribe(destination, (iMessage) => {
      try {
        const data = JSON.parse(iMessage.body);
        listener(data, iMessage, this);
      }
      catch (msgError) {
        console.warn('Error while handling incoming message.', msgError);
      }
    });
  }
}

const WEBSOCKET_MANAGER = new WebsocketManager(`${APP_ENV_API_PATH}/websocket`);

export default WEBSOCKET_MANAGER;
