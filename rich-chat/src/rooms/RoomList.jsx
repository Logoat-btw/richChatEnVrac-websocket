import classNames from 'classnames/bind';
import { ListGroup } from 'react-bootstrap';
import CurrentUserContext from '../common/components/CurrentUserContext';
import PropTypes from 'prop-types';
import { useContext } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faAdd, faCancel } from '@fortawesome/free-solid-svg-icons';
import { Link } from 'react-router';

function CreationRoomItem({ creatingRoom = false, onStartCreate, onCancelCreate }) {
  return creatingRoom
    ? (
        <ListGroup.Item variant="secondary" action onClick={onCancelCreate}>
          <FontAwesomeIcon icon={faCancel} className="me-1" />
          Annuler
        </ListGroup.Item>
      )
    : (
        <ListGroup.Item variant="success" action onClick={onStartCreate}>
          <FontAwesomeIcon icon={faAdd} className="me-1" />
          Créer une room
        </ListGroup.Item>
      );
}

CreationRoomItem.propTypes = {
  creatingRoom: PropTypes.bool,
  onStartCreate: PropTypes.func.isRequired,
  onCancelCreate: PropTypes.func.isRequired,
};

function RoomList({ rooms = [], creatingRoom = false, onStartCreate, onCancelCreate }) {
  const currentUser = useContext(CurrentUserContext);
  return (
    <ListGroup>
      {!!onStartCreate && !!onCancelCreate && (
        <CreationRoomItem creatingRoom={creatingRoom} onStartCreate={onStartCreate} onCancelCreate={onCancelCreate} />
      )}
      {rooms?.map(room => (
        <ListGroup.Item
          key={room.id}
          action
          as={Link}
          to={room.id}
        >
          <div className="fw-bold">{room.name}</div>
          <span className={classNames(room.owner.id === currentUser.id ? 'text-secondary' : 'text-info')}>{room.owner.username}</span>
        </ListGroup.Item>
      ))}
    </ListGroup>
  );
}

RoomList.propTypes = {
  rooms: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.node.isRequired,
    name: PropTypes.string.isRequired,
    owner: PropTypes.shape({
      id: PropTypes.node.isRequired,
      username: PropTypes.string.isRequired,
    }),
  })),
  creatingRoom: PropTypes.bool,
  onGoToRoom: PropTypes.func,
  onStartCreate: PropTypes.func,
  onCancelCreate: PropTypes.func,
};

export default RoomList;
