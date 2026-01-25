import { getMyself, signin, signoff } from './restNetwork';

class CurrentUserManager {
  #init = false;
  #id = null;
  #email = null;
  #username = null;
  #checkingPromise = null;

  get authenticated() {
    return !!this.#id;
  }

  get id() {
    return this.#id;
  }

  get email() {
    return this.#email;
  }

  get username() {
    return this.#username;
  }

  async checkUser(force = false) {
    if (this.#checkingPromise) {
      return this.#checkingPromise;
    }
    if (this.#init && !force) {
      return this;
    }
    this.#checkingPromise = this.internalCheckUser(force);
    return this.#checkingPromise;
  }

  async login(email, password) {
    if (!email || !password) {
      throw new Error('mail ou password manquant');
    }
    const userData = await signin({ email, password });
    this.#id = userData.id;
    this.#email = userData.mail;
    this.#username = userData.username;
  }

  async logout() {
    if (!this.authenticated) {
      return;
    }
    try {
      await signoff();
    }
    finally {
      this.#id = null;
      this.#email = null;
      this.#username = null;
    }
  }

  async internalCheckUser(force = false) {
    if (!this.#init || force) {
      this.#id = null;
      this.#email = null;
      this.#username = null;
      try {
        const userData = await getMyself();
        if (userData) {
          this.#id = userData.id;
          this.#email = userData.mail;
          this.#username = userData.username;
        }
      }
      finally {
        this.#init = true;
        this.#checkingPromise = null;
      }
    }
    return this;
  }
}

export default CurrentUserManager;
