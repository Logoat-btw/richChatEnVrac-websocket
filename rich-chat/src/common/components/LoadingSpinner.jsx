import { faSpinner } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import PropTypes from 'prop-types';

function LoadingSpinner({ className = null, As = 'h2' }) {
  return (
    <As className={className}>
      <FontAwesomeIcon icon={faSpinner} className="text-primary" pulse />
    </As>
  );
}

LoadingSpinner.propTypes = {
  className: PropTypes.string,
  As: PropTypes.elementType,
};

export default LoadingSpinner;
