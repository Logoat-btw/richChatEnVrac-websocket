import PropTypes from 'prop-types';
import { Button, Form } from 'react-bootstrap';
import { useFormStatus } from 'react-dom';
import { Form as RRF } from 'react-router';

function RoomCreationForm({ className = null }) {
  const { pending } = useFormStatus();

  return (
    <Form as={RRF} method="post" className={className}>
      <fieldset disabled={pending}>
        <Form.Group className="mb-3" controlId="creatingRoomFormName">
          <Form.Label>Nom de la room</Form.Label>
          <Form.Control type="text" placeholder="Votre nom de room" name="name" required />
        </Form.Group>

        <Form.Group className="mb-3" controlId="creatingRoomFormColor">
          <Form.Label>Couleur de la room</Form.Label>
          <Form.Control type="color" name="color" />
        </Form.Group>

        <Button variant="primary" type="submit">
          Créer la room
        </Button>
      </fieldset>
    </Form>
  );
}

RoomCreationForm.propTypes = {
  className: PropTypes.string,
};

export default RoomCreationForm;
