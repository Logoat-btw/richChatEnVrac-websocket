/* eslint-disable no-console */

const slashMatcher = /\/$/g;

const ROOT_API_URL = `${APP_ENV_API_PATH.replace(slashMatcher, '')}`;

let lastSuccessfullCheckDate = null;

async function checkActivityAndNotify() {
  console.log('Check activity...');

  let baseUrl = `${ROOT_API_URL}/rest/rooms/recent-activity`;

  if (lastSuccessfullCheckDate) {
    baseUrl += `?last=${lastSuccessfullCheckDate.toISOString()}`;
  }

  try {
    const answer = await fetch(baseUrl, {

      credentials: 'include',

    });

    if (!answer.ok) {
      throw new Error(`Unable to check activity. Server status: ${answer.status}.`);
    }

    const data = await answer.json();

    lastSuccessfullCheckDate = new Date();

    if (data?.newMessages === true) {
      console.log('Messages nuveaux !!!');

      await self.registration.showNotification('Chat-App', {

        body: 'Nouveaux message !',

      });
    }
  }

  catch (error) {
    console.warn('Erreur de check activity', error.message);
  }
}

let pollingInterval = null;

function startCheckPolling() {
  if (pollingInterval) {
    clearInterval(pollingInterval);
  }

  pollingInterval = setInterval(() => {
    checkActivityAndNotify();
  }, 5 * 1000);
}

function getContentToCache() {
  return [

    '/favicon.ico',

    '/index.html',

    '/',

  ];
}

const HTTP_SCHEME_REGEX = /^https?:\/\//;

const HOT_RELOAD_REGEX = /\.hot-update\./;

function canRequestBeCached(request) {
  try {
    // Ne pas gérer autre chose que de l'http(s)

    if (!request.url.match(HTTP_SCHEME_REGEX)) {
      return false;
    }

    // Ne pas gérer les requête à l'API REST

    if (request.url.startsWith(ROOT_API_URL)) {
      console.log('ressource depuis l\'api rest');

      return false;
    }

    // Si on est en dev, le fichier *.hot-update.js ne doit pas être géré par le cache

    if (request.url.match(HOT_RELOAD_REGEX)) {
      return false;
    }

    // Otherwise, we can cache

    return true;
  }

  catch {
    return false;
  }
}

try {
  const CACHE_NAME = 'mycache-v1.0';

  // Installation hook

  self.addEventListener('install', (e) => {
    // Open the cache for the ongoing version and add all resources

    // that should be cached at installation

    e.waitUntil((async () => {
      const cache = await caches.open(CACHE_NAME);

      console.log('Cache ouvert, prêt à être utilisé');

      await cache.addAll(getContentToCache());
    })());
  });

  // Activation hook: stalled caches cleanup

  self.addEventListener('activate', (e) => {
    e.waitUntil(

      // Async remove all caches that are not the ongoing cache

      caches.keys().then(keyList => Promise.all(

        keyList.map((key) => {
          if (key === CACHE_NAME) {
            return undefined;
          }

          return caches.delete(key);
        }),

      )).then(() => {
        startCheckPolling();
      }),

    );
  });

  async function retrieveAndCacheResource(request) {
    const response = await fetch(request);

    const cache = await caches.open(CACHE_NAME);

    cache.put(request, response.clone());

    return response;
  }

  // Fetching cache management

  self.addEventListener('fetch', (e) => {
    console.log('fetch intercepté');

    if (e.request.mode === 'navigate') {
      console.log('navige à la racine');

      // Return to the index.html page

      e.respondWith(caches.match('/'));

      return;
    }

    // On ne gère pas le cache pour les requête qui n'ont pas à l'être

    if (!canRequestBeCached(e.request)) {
      console.log('la ressource ne peut pas être mise en cache', e.request.url);

      return;
    }

    e.respondWith((async () => {
      // On récupère la ressource depuis le cache et on la sert si présente

      console.log('Récupere la ressource du cache ' + e.request.url);

      const r = await caches.match(e.request);

      if (r) {
        // Si j'ai pas accès au réseau : je ne fais rien d'autre que retourne la réponse

        // Sinon je demande sa dernière version le cas échant pour la prochaine fois

        retrieveAndCacheResource(e.request);

        return r;
      }

      // Si la ressource n'est pas présente, on effectue la requête

      console.log('Récupere la ressource du réseau ' + e.request.url);

      return retrieveAndCacheResource(e.request);
    })()); // notez bien l'appel de notre fonction asynchrone anonyme: e.respondWith prend en param une promesse de ressource
  });

  // Gestion de la mise en place de synchro périodique

  // self.addEventListener('sync', (event) => {

  // if (event.tag == 'get-activity') {

  // console.log('mise en place polling');

  // startCheckPolling();

  // }

  // });
}

catch (e) {
  console.warn('CANNOT START SERVICE WORKER', e);

  throw e;
}
