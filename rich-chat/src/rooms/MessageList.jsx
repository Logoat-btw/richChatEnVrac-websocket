import { faTrash } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import classNames from 'classnames';
import PropTypes from 'prop-types';
import { useEffect, useRef } from 'react';
import { Button, Card } from 'react-bootstrap';

import * as style from './MessageList.scss';

function Message({ message, creation, author, deletable = false, onDelete, className }) {
  return (

    <Card border="primary" className={classNames(style.Message, className)}>
      <Card.Body>
        <Card.Title className="d-flex justify-content-between align-items-start">
          <span className="d-inline-block me-auto">
            {author.username}
          </span>
          {deletable && !!onDelete && (
            <Button variant="outline-danger" id="button-addon2" aria-label="Supprimer" onClick={onDelete} size="sm">
              <FontAwesomeIcon icon={faTrash} title="Retirer" />
            </Button>
          )}
        </Card.Title>
        <Card.Subtitle className="mb-2 text-muted">
          {creation.toLocaleString()}
        </Card.Subtitle>
        <Card.Text as="div" dangerouslySetInnerHTML={{ __html: message }}>
        </Card.Text>
      </Card.Body>
    </Card>
  );
}

Message.propTypes = {
  message: PropTypes.node.isRequired,
  creation: PropTypes.instanceOf(Date).isRequired,
  author: PropTypes.shape({
    id: PropTypes.node.isRequired,
    username: PropTypes.string.isRequired,
  }).isRequired,
  deletable: PropTypes.bool,
  onDelete: PropTypes.func,
  className: PropTypes.string,
};

function MessageList({ messages = [], isRoomOwner = false, currentUser = null, onRemoveMessage, autoscroll = false, className }) {
  const messageListRef = useRef();

  useEffect(() => {
    if (autoscroll && messages.length) {
      // Auto scroll down
      messageListRef.current.scrollTop = messageListRef.current.scrollHeight;
    }
  }, [messages, autoscroll]);

  return (
    <div ref={messageListRef} className={classNames('border border-secondary p-3', className)}>
      {messages.map(msg => (
        <Message
          key={msg.id}
          message={msg.message}
          creation={msg.creation}
          author={msg.author}
          deletable={isRoomOwner || (!!currentUser && currentUser.id === msg.author.id)}
          onDelete={() => onRemoveMessage(msg)}
          className="mt-3"
        />
      ))}
    </div>
  );
}

MessageList.propTypes = {
  messages: PropTypes.arrayOf(PropTypes.shape({
    message: PropTypes.node.isRequired,
    creation: PropTypes.instanceOf(Date).isRequired,
    author: PropTypes.shape({
      id: PropTypes.node.isRequired,
      username: PropTypes.string.isRequired,
    }).isRequired,
  })),
  isRoomOwner: PropTypes.bool,
  currentUser: PropTypes.string,
  onRemoveMessage: PropTypes.func,
  autoscroll: PropTypes.bool,
  className: PropTypes.string,
};

export default MessageList;
