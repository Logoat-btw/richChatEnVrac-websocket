import { faPlus, faQuestionCircle, faTrash } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import classNames from 'classnames';
import PropTypes from 'prop-types';
import { useState } from 'react';
import { Button, Form, InputGroup, ListGroup } from 'react-bootstrap';

function AddGuestInput({ onAddGuest, className }) {
  const [guestEmail, setGuestEmail] = useState('');

  const addGuest = () => {
    if (!guestEmail) {
      return;
    }
    onAddGuest(guestEmail).then((res) => {
      if (res) {
        setGuestEmail('');
      }
    });
  };

  return (
    <InputGroup className={classNames(className)}>
      <Form.Control
        placeholder="Ajouter un invité par son adresse mél"
        aria-label="Adresse Mél de l'invité"
        type="email"
        value={guestEmail}
        onChange={e => setGuestEmail(e.target.value)}
      />
      <Button variant="outline-secondary" id="button-addon2" aria-label="Ajouter" onClick={addGuest} disabled={!guestEmail}>
        <FontAwesomeIcon icon={faPlus} title="Ajouter" />
      </Button>
    </InputGroup>
  );
}

AddGuestInput.propTypes = {
  onAddGuest: PropTypes.func.isRequired,
  className: PropTypes.string,
};

function GuestList({ guests = [], isRoomOwner = false, onAddGuest, onRemoveGuest, className }) {
  return (
    <>
      <h3>Invités </h3>
      <ListGroup className={className}>
        {isRoomOwner && (
          <ListGroup.Item><AddGuestInput onAddGuest={onAddGuest} /></ListGroup.Item>
        )}
        {guests?.map(guest => (
          <ListGroup.Item
            key={guest.member.id}
            as="li"
            className="d-flex justify-content-between align-items-start"
          >
            <div className="ms-2 me-auto">
              {guest.member.username}
              {guest.pending && (
                <FontAwesomeIcon icon={faQuestionCircle} className="ms-1 text-info" aria-hidden="true" />
              )}
            </div>
            {isRoomOwner && (
              <Button variant="outline-danger" id="button-addon2" aria-label="Ajouter" onClick={() => onRemoveGuest(guest.member)}>
                <FontAwesomeIcon icon={faTrash} title="Retirer" />
              </Button>
            )}
          </ListGroup.Item>
        ))}
      </ListGroup>
    </>
  );
}

GuestList.propTypes = {
  guests: PropTypes.arrayOf(PropTypes.shape({
    member: PropTypes.shape({
      id: PropTypes.node.isRequired,
      username: PropTypes.string.isRequired,
    }).isRequired,
    pending: PropTypes.bool,
  })),
  isRoomOwner: PropTypes.bool,
  onAddGuest: PropTypes.func.isRequired,
  onRemoveGuest: PropTypes.func.isRequired,
  className: PropTypes.string,
};

export default GuestList;
