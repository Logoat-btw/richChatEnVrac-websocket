class StorageManager {
  #simulated;
  #storage;

  constructor(storageType = 'sessionStorage') {
    this.#init(storageType);
  }

  #init(storageType) {
    try {
      this.#storage = window[storageType];
      const testedKey = '__STORAGE_TEST__';
      this.#storage.setItem(testedKey, testedKey);
      this.#storage.removeItem(testedKey);
      this.#simulated = false;
    }
    catch (e) {
      if (e instanceof DOMException
        && (e.name === 'QuotaExceededError' || e.name === 'NS_ERROR_DOM_QUOTA_REACHED')
        && this.#storage && this.#storage.length !== 0) {
        console.warn(`Max storage reached for ${storageType}. Will simulate storage.`, e);
      }
      else {
        console.warn(`Unavailable storage ${storageType}. Will simulate storage.`, e);
      }
      this.#storage = new Map();
      this.#simulated = true;
    }
  }

  set(key, value) {
    if (!key) {
      throw new Error('Missing key for storage');
    }
    if (this.#simulated) {
      this.#storage.set(key, value);
    }
    else {
      this.#storage.setItem(key, JSON.stringify(value, null, 0));
    }
    return value;
  }

  get(key) {
    if (!key) {
      throw new Error('Missing key for storage');
    }
    if (this.#simulated) {
      return this.#storage.get(key) ?? null;
    }
    else {
      return JSON.parse(this.#storage.getItem(key));
    }
  }

  delete(key) {
    if (!key) {
      throw new Error('Missing key for storage');
    }
    if (this.#simulated) {
      this.#storage.delete(key);
    }
    else {
      this.#storage.removeItem(key);
    }
  }

  clear() {
    // same method between MAp and storage
    this.#storage.clear();
  }

  keys() {
    if (this.#simulated) {
      return Array.from(this.#storage.keys());
    }
    return Array.from({ length: this._storage.length }, (_, idx) => this.#storage.key(idx));
  }
}

// Instance cache
const STORAGES = {
  local: null,
  session: null,
};

export function getLocalStorage() {
  if (!STORAGES.local) {
    STORAGES.local = new StorageManager('localStorage');
  }
  return STORAGES.local;
}

export function getSessionStorage() {
  if (!STORAGES.session) {
    STORAGES.session = new StorageManager('sessionStorage');
  }
  return STORAGES.session;
}
