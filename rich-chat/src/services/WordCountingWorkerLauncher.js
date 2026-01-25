import DOMPurify from 'dompurify';

const listeners = new Set();
let statsByRoomId = {};
let worker;

export function subscribe(lst) {
  listeners.add(lst);
  return () => {
    listeners.delete(lst);
  };
}

export function getSnapshot() {
  return statsByRoomId;
}

export function onNewMessage(roomId, messages) {
  if (!worker) {
    worker = initWorker();
  }
  const msgs = messages.map(m => DOMPurify.sanitize(m, { USE_PROFILES: { ALLOWED_TAGS: [] } }));

  worker.postMessage({ roomId, messages: msgs });
}

export default function initWorker() {
  const wk = new Worker(
    new URL('./WordCountingWorker.js', import.meta.url),
    {
      type: 'classic',
      credentials: 'same-origin',
      name: 'Student session entry worker',
    },
  );
  wk.onerror = (error) => {
    console.warn('Worker error', error);
  };
  wk.onmessage = (msg) => {
    const { roomId, stats } = msg.data;
    statsByRoomId = { ...statsByRoomId, [roomId]: stats };
    for (let lst of listeners) {
      lst ();
    }
  };
  return wk;
}
