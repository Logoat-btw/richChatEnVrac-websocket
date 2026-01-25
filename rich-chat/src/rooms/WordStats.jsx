import PropTypes from 'prop-types';
import { useSyncExternalStore } from 'react';
import { ListGroup } from 'react-bootstrap';
import { getSnapshot, subscribe } from '../services/WordCountingWorkerLauncher';

function WordStats({ roomId, className }) {
  const allStats = useSyncExternalStore(subscribe, getSnapshot);
  const stats = allStats[roomId] ?? [];

  return (
    <>
      <h3>Les mots les plus utilisés</h3>
      <ListGroup className={className}>
        {stats?.map(({ word, count }) => (
          <ListGroup.Item
            key={word}
            as="li"
            className="d-flex justify-content-between align-items-start"
          >
            {word}
            {' '}
            (
            {count}
            )
          </ListGroup.Item>
        ))}
      </ListGroup>
    </>
  );
}

WordStats.propTypes = {
  roomId: PropTypes.string.isRequired,
  className: PropTypes.string,
};

export default WordStats;
