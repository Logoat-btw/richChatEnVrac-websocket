import PropTypes from 'prop-types';
import RoomView from '../../rooms/RoomView';
import RoomManagementView from '../../rooms/RoomManagementView';
import AuthenticationWelcome from '../../welcome/AuthenticationWelcome';
import { useContext } from 'react';
import CurrentUserContext from './CurrentUserContext';
import { createRoom, deleteRoom } from '../../services/restNetwork';
import LoadingSpinner from './LoadingSpinner';

function ViewHolder({ view, roomId, onChangeView }) {
  const userManager = useContext(CurrentUserContext);

  const doSignin = async (email, password) => {
    // Tentative de connexion
    await userManager.login(email, password);
    // Ok : affichage de la page des rooms
    onChangeView({ view: 'rooms' });
  };

  const goToRoom = (roomId) => {
    onChangeView({ view: 'room', roomId });
  };

  const doCreateRoom = async (name, color) => {
    // Tentative de creation
    const room = await createRoom({ name, color });
    onChangeView({ view: 'room', roomId: room.id });
  };

  const doDeleteRoom = async (roomId) => {
    await deleteRoom({ roomId });
    onChangeView({ view: 'rooms' });
  };

  if (view === 'loading') {
    return <LoadingSpinner As="h1" />;
  }
  else if (view === 'welcome') {
    return <AuthenticationWelcome onSignin={(email, password) => doSignin(email, password)} />;
  }
  else if (view === 'rooms') {
    return <RoomManagementView onGoToRoom={goToRoom} onCreateRoom={doCreateRoom} />;
  }
  else if (view === 'room') {
    return <RoomView roomId={roomId} onDeleteRoom={doDeleteRoom} />;
  }
  else {
    throw new Error(`Unmanaged page ${view}`);
  }
}

ViewHolder.propTypes = {
  view: PropTypes.oneOf(['welcome', 'rooms', 'room', 'loading']),
  onChangeView: PropTypes.func.isRequired,
  roomId: PropTypes.node,
};

export default ViewHolder;
