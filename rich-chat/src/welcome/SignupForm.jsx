import PropTypes from 'prop-types';
import { Alert, Button, Form } from 'react-bootstrap';
import { useState } from 'react';
import { signup } from '../services/restNetwork';
import PendingFormDisabler from './PendingFormDisabler';

function SignupForm({ className = null }) {
  const [creationState, setCreationState] = useState({
    userCreated: null,
    error: null,
  });

  const createUser = async (formData) => {
    setCreationState(st => ({ ...st, userCreated: null }));
    const email = formData.get('email');
    const username = formData.get('username');
    const password = formData.get('password');
    const password2 = formData.get('password-copy');
    if (password !== password2) {
      setCreationState(st => ({ ...st, error: 'les mots de passe ne correspondent pas.' }));
      return;
    }
    return signup({ email, username, password }).then((userCreated) => {
      setCreationState(st => ({ ...st, userCreated, error: null }));
    }, (error) => {
      setCreationState(st => ({ ...st, error: error.message }));
    });
  };

  return (
    <>
      <Form action={createUser} className={className}>
        <PendingFormDisabler>
          <Form.Group className="mb-3" controlId="signinFormUsername">
            <Form.Label>Nom d&lsquo;utilisateur</Form.Label>
            <Form.Control type="email" placeholder="Votre adresse mél" name="email" required />
          </Form.Group>

          <Form.Group className="mb-3" controlId="signinFormUsername">
            <Form.Label>Nom d&lsquo;utilisateur</Form.Label>
            <Form.Control type="username" placeholder="Votre nom d'utilisateur" name="username" required />
          </Form.Group>

          <Form.Group className="mb-3" controlId="signinFormPassword">
            <Form.Label>Mot de passe</Form.Label>
            <Form.Control type="password" placeholder="Password" name="password" required />
          </Form.Group>

          <Form.Group className="mb-3" controlId="signinFormPassword">
            <Form.Label>Confirmer votre mot de passe</Form.Label>
            <Form.Control type="password" placeholder="Password" name="password-copy" required />
          </Form.Group>

          <Button variant="primary" type="submit">
            Créer son compte
          </Button>
        </PendingFormDisabler>
      </Form>
      {creationState.userCreated && (
        <Alert variant="success" className="mt-3">Votre compte a bien été créé.</Alert>
      )}
      {creationState.error && (
        <Alert variant="danger" className="mt-3">
          Une erreur est survenue:&nbsp;
          {creationState.error}
        </Alert>
      )}
    </>
  );
}

SignupForm.propTypes = {
  className: PropTypes.string,
};

export default SignupForm;
