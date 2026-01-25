// import classNames from 'classnames';
import PropTypes from 'prop-types';
import Modal from 'react-bootstrap/Modal';
import Button from 'react-bootstrap/Button';
import { faWarning } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

function RoomDeletionModal({
  show = true, roomName, onConfirm, onCancel,
}) {
  return (
    <Modal
      show={show}
      onHide={onCancel}
      fullscreen="md-down"
    >
      <Modal.Header closeButton>
        <Modal.Title className="text-danger">
          <FontAwesomeIcon icon={faWarning} className="me-1" title="supprimer" />
          Suppression de la room
        </Modal.Title>
      </Modal.Header>

      <Modal.Body>
        <p>
          Vous êtes sur le point de supprimer la room&nbps;
          {roomName}
          . Êtes-vous certain ?
        </p>
      </Modal.Body>

      <Modal.Footer>
        <Button variant="secondary" onClick={onCancel}>Annuler</Button>
        <Button variant="danger" onClick={onConfirm}>Confirmer la suppression</Button>
      </Modal.Footer>
    </Modal>
  );
}

RoomDeletionModal.propTypes = {
  show: PropTypes.bool,
  roomName: PropTypes.string.isRequired,
  onConfirm: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
};

export default RoomDeletionModal;
