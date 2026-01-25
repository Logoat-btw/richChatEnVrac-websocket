import PropTypes from 'prop-types';
import { Button, Form } from 'react-bootstrap';
import PendingFormDisabler from './PendingFormDisabler';
import { Form as RRF } from 'react-router';

function SigninForm({ className = null }) {
  return (
    <Form as={RRF} action="" method="post" className={className}>
      <PendingFormDisabler>
        <Form.Group className="mb-3" controlId="signinFormUsername">
          <Form.Label>Adresse mail</Form.Label>
          <Form.Control type="email" placeholder="Votre adresse mail" name="email" required />
        </Form.Group>

        <Form.Group className="mb-3" controlId="signinFormPassword">
          <Form.Label>Mot de passe</Form.Label>
          <Form.Control type="password" placeholder="Password" name="password" required />
        </Form.Group>

        <Button variant="primary" type="submit">
          S&lsquo;authentifier
        </Button>
      </PendingFormDisabler>
    </Form>
  );
}

SigninForm.propTypes = {
  onSignin: PropTypes.func.isRequired,
  className: PropTypes.string,
};

export default SigninForm;
