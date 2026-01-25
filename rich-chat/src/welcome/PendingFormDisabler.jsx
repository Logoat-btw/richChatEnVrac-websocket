import PropTypes from 'prop-types';
import { useFormStatus } from 'react-dom';

function PendingFormDisabler({ children = null }) {
  const { pending } = useFormStatus();
  return (
    <fieldset disabled={pending}>
      {children}
    </fieldset>
  );
}

PendingFormDisabler.propTypes = {
  children: PropTypes.node,
};

export default PendingFormDisabler;
