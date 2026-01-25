import RoomList from './RoomList';
import { useState, useEffect } from 'react';
import { Col, Row } from 'react-bootstrap';
import RoomCreationForm from './RoomCreationForm';
import PropTypes from 'prop-types';
import { useLoaderData } from 'react-router';
import WEBSOCKET_MANAGER from '../services/WebsocketManager';

function RoomManagementView() {
  const [creating, setCreating] = useState(false);
  const { rooms: initialRooms } = useLoaderData();
  const [rooms, setRooms] = useState(initialRooms);

  useEffect(() => {
    setRooms(initialRooms);
  }, [initialRooms]);

  useEffect(() => {
    const unsubscribe = WEBSOCKET_MANAGER.subscribe({
      onUserRoomsMessage: (data) => {
        if (data.orderType === 'newRoom') {
          const newRoom = {
            id: data.roomId,
            name: data.name,
            color: data.color,
            owner: data.owner,
          };
          setRooms(prev => {
            if (prev.some(r => r.id === newRoom.id)) return prev;
            return [...prev, newRoom];
          });
        }
        else if (data.orderType === 'guestRemoved') {
          setRooms(prev => prev.filter(r => r.id !== data.roomId));
        }
        else if (data.orderType === 'roomDeleted') {
          setRooms(prev => prev.filter(r => r.id !== data.roomId));
        }
      },
    });

    return () => unsubscribe();
  }, []);

  return (
    <Row>
      <Col xs={12} md={6}>
        <RoomList
          rooms={rooms}
          creatingRoom={creating}
          onStartCreate={() => setCreating(true)}
          onCancelCreate={() => setCreating(false)}
        >
        </RoomList>
      </Col>
      {creating && (
        <Col xs={12} md={6}>
          <RoomCreationForm />
        </Col>
      )}
    </Row>
  );
}

RoomManagementView.propTypes = {
  onGoToRoom: PropTypes.func,
  onCreateRoom: PropTypes.func.isRequired,
};

export default RoomManagementView;
