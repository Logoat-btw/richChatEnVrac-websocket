import { faTrash } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useContext, useEffect, useReducer } from 'react';
import { Button, Col, Row } from 'react-bootstrap';
import { useLoaderData, useSubmit, useNavigate } from 'react-router';
import CurrentUserContext from '../common/components/CurrentUserContext';
import { useToast } from '../common/components/ToastContext';
import LoadingSpinner from '../common/components/LoadingSpinner';
import { onNewMessage } from '../services/WordCountingWorkerLauncher';
import { deleteRoomMessage, inviteUserToRoom, removeUserFromRoom, sendRoomMessage } from '../services/restNetwork';
import WEBSOCKET_MANAGER from '../services/WebsocketManager';
import GuestList from './GuestList';
import MessageEditor from './MessageEditor';
import MessageList from './MessageList';
import RoomDeletionModal from './RoomDeletionModal';
import WordStats from './WordStats';

function createState(room = null, messages = null) {
  if (room) {
    return { ...room, messages: messages ?? [], aboutToDelete: false };
  }
  else {
    return { name: '', owner: { username: '?', id: '?' }, guests: [], messages: [], aboutToDelete: false };
  }
}

function reduce(state, action) {
  switch (action.type) {
    case 'reset': {
      const texts = action.messages.map(msg => msg.message);
      onNewMessage(action.room.id, texts);
      return createState(action.room, action.messages);
    }
    case 'add-guest':
      if (state.guests.some(g => g.member.id === action.value.member.id)) {
        return state;
      }
      return { ...state, guests: [...state.guests, action.value] };
    case 'remove-guest':
      return { ...state, guests: state.guests.filter(g => g.member.id !== action.value) };
    case 'add-message': {
      if (state.messages.some(m => m.id === action.value.id)) {
        return state;
      }
      const texts = [...state.messages, action.value].map(msg => msg.message);
      onNewMessage(state.id, texts);
      return { ...state, messages: [...state.messages, action.value] };
    }
    case 'remove-message':
      return { ...state, messages: state.messages.filter(m => m.id !== action.value) };
    case 'start-deletion':
      return { ...state, aboutToDelete: true };
    case 'cancel-deletion':
      return { ...state, aboutToDelete: false };
    default:
      throw new Error(`Illegal action type: ${action.type}`);
  }
}

function RoomView() {
  const { room, messages } = useLoaderData();
  const submit = useSubmit();
  const navigate = useNavigate();

  const currentUser = useContext(CurrentUserContext);
  const { addToast } = useToast();
  const [state, dispatch] = useReducer(reduce, createState());

  useEffect(() => {
    dispatch({ type: 'reset', room, messages });
  }, [room, messages]);

  useEffect(() => {
    if (!state.id) return;

    const unsubscribe = WEBSOCKET_MANAGER.subscribe({
      roomId: state.id,
      onTopicRoomMessage: (roomId, data) => {
        if (data.orderType === 'messageAdded' && data.message) {
          dispatch({ type: 'add-message', value: data.message });
        }
        else if (data.orderType === 'messageRemoved' && data.messageId) {
          dispatch({ type: 'remove-message', value: data.messageId });
        }
        else if (data.orderType === 'guestAdded' && data.member) {
          dispatch({ type: 'add-guest', value: { member: data.member, pending: data.pending } });
          addToast(`${data.member.username} a été ajouté à la room`, 'success');
        }
        else if (data.orderType === 'guestRemoved' && data.member) {
          dispatch({ type: 'remove-guest', value: data.member.id });
          addToast(`${data.member.username} a été retiré de la room`, 'warning');
        }
      },
      onUserRoomsMessage: (data) => {
        if (data.orderType === 'roomDeleted' && data.roomId === state.id) {
          navigate('/rooms');
        }
      },
    });

    return () => unsubscribe();
  }, [state.id, navigate, addToast]);

  const isOwner = state.owner.id === currentUser.id;

  const addGuest = (email) => {
    if (!isOwner) {
      console.warn('Cannot add guest if owner!');
      return;
    }
    return inviteUserToRoom({ roomId: state.id, email }).then((guest) => {
      dispatch({ type: 'add-guest', value: guest });
      return true;
    }).catch(() => false);
  };

  const removeGuest = ({ id }) => {
    if (!isOwner) {
      console.warn('Cannot remove guest if not room owner!');
      return;
    }
    return removeUserFromRoom({ roomId: state.id, guestId: id }).then(() => {
      dispatch({ type: 'remove-guest', value: id });
      return true;
    }).catch(() => false);
  };

  const removeMessage = ({ id, author }) => {
    if (!isOwner && author.id !== currentUser.id) {
      console.warn('Cannot remove message if not room owner nor author of message!');
      return;
    }
    return deleteRoomMessage({ roomId: state.id, messageId: id, isOwner }).then(() => {
      dispatch({ type: 'remove-message', value: id });
      return true;
    }).catch(() => false);
  };

  const addMessage = (message) => {
    if (!message) {
      console.warn('Cannot add nullish message!');
      return;
    }
    return sendRoomMessage({ roomId: state.id, message }).then((message) => {
      dispatch({ type: 'add-message', value: message });
      return true;
    }).catch(() => false);
  };

  const deleteRoom = () => {
    if (!isOwner) {
      return;
    }
    submit({ roomId: state.id }, { method: 'DELETE', action: `./` });
  };

  if (!state.name) {
    return (
      <Row className="justify-content-center mb-3">
        <Col xs="auto"><LoadingSpinner As="h1" /></Col>
      </Row>
    );
  }

  return (
    <>
      <RoomDeletionModal
        show={state.aboutToDelete}
        roomName={state.name}
        onConfirm={deleteRoom}
        onCancel={() => dispatch({ type: 'cancel-deletion' })}
      />
      <div className="h-view d-flex flex-column">
        <Row className="flex-shrink-1 justify-content-center mb-3">
          <Col xs={12} md={8} xl={6} className="d-flex justify-content-between align-items-center">
            <h1 className="text-primary">{state.name}</h1>
            {isOwner && (
              <Button variant="outline-danger" onClick={() => dispatch({ type: 'start-deletion' })}>
                <FontAwesomeIcon icon={faTrash} className="me-1" title="supprimer" />
                <span className="sr-only">supprimer la room</span>
              </Button>
            )}
          </Col>
        </Row>
        <Row className="flex-grow-1 flex-shrink-1 overflow-y-auto">
          <Col xs={12} md={6} xl={8} className="d-flex flex-column h-100">
            <MessageList
              messages={state.messages}
              isRoomOwner={isOwner}
              currentUser={currentUser}
              onRemoveMessage={removeMessage}
              autoscroll
              className="flex-grow-1 flex-shrink-1 overflow-y-auto"
            />
          </Col>
          <Col xs={12} md={6} xl={4} className="d-flex flex-column h-100">
            {!isOwner && (
              <div className="mb-2 flex-shrink-1">
                <h3>Propriétaire</h3>
                <span className="fw-bold text-primary">{state.owner.username}</span>
              </div>
            )}
            <GuestList
              guests={state.guests}
              isRoomOwner={isOwner}
              onAddGuest={addGuest}
              onRemoveGuest={removeGuest}
              className="flex-grow-1 flex-shrink-1 overflow-y-auto"
            />
            <WordStats
              roomId={state.id}
              className="flex-grow-1 flex-shrink-1 overflow-y-auto"
            />
          </Col>
        </Row>
        <Row className="flex-shrink-1">
          <Col xs={12}>
            <MessageEditor onAddMessage={addMessage} />
          </Col>
        </Row>
      </div>
    </>
  );
}

RoomView.propTypes = {
};

export default RoomView;
