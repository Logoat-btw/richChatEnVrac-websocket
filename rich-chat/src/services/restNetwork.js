/* eslint-disable no-console */
import axios from 'axios';
import cleanMesssage from './messageCleaner';

const slashMatcher = /\/$/g;
export const ROOT_URL = `${APP_ENV_API_PATH.replace(slashMatcher, '')}`;

export const TIME_BEFORE_REFRESH_MS = 10 * 1000;

export const ROOT_AX = axios.create({
  baseURL: `${ROOT_URL}/rest`,
  timeout: 90000,
  withCredentials: true,
  csrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
});

ROOT_AX.interceptors.request.use(function (config) {
  if (config.headers.get(config.xsrfHeaderName)) {
    return config;
  }
  // Test si cookice XSTF est présent
  const token = document.cookie
    .split(/\s*;\s*/)
    .find(row => row.startsWith('XSRF-TOKEN='))
    ?.split('=')[1];
  // Ajout du token dans l'entête
  if (token) {
    config.headers.set(config.xsrfHeaderName, token);
  }
  return config;
});

ROOT_AX.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

export async function signup({ email, username, password }) {
  if (!email || !username || !password) {
    throw new Error('Mail ou username ou password manquant');
  }
  const res = await ROOT_AX.post(`accounts`, { email, username, password });
  return res.data;
}

export async function signoff() {
  await ROOT_AX.post(`logout`);
  return null;
}

export async function getMyself() {
  console.log('fetch myself');
  const res = await ROOT_AX.get(`accounts/myself`);
  return res.data;
}

export async function signin({ email, password }) {
  console.log('fetch signin');
  if (!email || !password) {
    throw new Error('Mail ou password manquant');
  }
  const res = await ROOT_AX.post(`login`, {
    email,
    password,
  });
  return res.data;
}

export async function getRooms({ debugSilent = false } = {}) {
  if (!debugSilent) {
    console.log('fetch rooms');
  }
  const res = await ROOT_AX.get(`rooms`);
  return res.data;
}

export async function getRoom({ roomId }) {
  if (!roomId) {
    throw new Error('Id de room manquant');
  }
  const res = await ROOT_AX.get(`rooms/${roomId}`);
  return res.data;
}

export async function createRoom({ name, color = null }) {
  if (!name) {
    throw new Error('Nom de room manquant');
  }
  const res = await ROOT_AX.post(`rooms`, {
    name,
    color,
  });
  return res.data;
}

export async function deleteRoom({ roomId }) {
  if (!roomId) {
    throw new Error('Id de room manquant');
  }
  await ROOT_AX.delete(`rooms/${roomId}`);
  return true;
}

export async function inviteUserToRoom({ roomId, email }) {
  if (!roomId || !email) {
    throw new Error('Id de room ou email manquant');
  }
  const res = await ROOT_AX.post(`rooms/${roomId}/guests`, { email });
  return res.data;
}

export async function removeUserFromRoom({ roomId, guestId }) {
  if (!roomId || !guestId) {
    throw new Error('Id de room ou id d\'invité manquant');
  }
  await ROOT_AX.delete(`rooms/${roomId}/guests/${guestId}`);
  return true;
}

export async function getRoomMessages({ roomId }) {
  if (!roomId) {
    throw new Error('Id de room ou message manquant');
  }
  const res = await ROOT_AX.get(`rooms/${roomId}/messages`);
  return res.data.map(obj => ({
    ...obj,
    message: cleanMesssage(obj.message),
  }));
}

export async function sendRoomMessage({ roomId, message }) {
  if (!roomId || !message) {
    throw new Error('Id de room ou message manquant');
  }
  const cleanedMessage = cleanMesssage(message);
  const res = await ROOT_AX.post(`rooms/${roomId}/messages`, {
    message: cleanedMessage,
  });
  return res.data;
}

export async function deleteRoomMessage({
  roomId,
  messageId,
  isOwner = false,
}) {
  if (!roomId || !messageId) {
    throw new Error('Id de room ou id de message manquant');
  }
  await ROOT_AX.delete(`rooms/${roomId}/messages/${messageId}`, {
    params: isOwner ? { owner: true } : {},
  });
  return true;
}

export async function getCSRFToken() {
  const cookies = document.cookie.split(';');
  for (const cookie of cookies) {
    const [name, value] = cookie.trim().split('=');
    if (name === 'XSRF-TOKEN') {
      return { headerName: 'X-XSRF-TOKEN', token: decodeURIComponent(value) };
    }
  }
  throw new Error('CSRF token not found');
}
