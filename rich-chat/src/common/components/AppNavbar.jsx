import { useContext, useEffect, useState } from 'react';
import { Navbar } from 'react-bootstrap';
import logoPict from '../../assets/logo.png';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSignOut, faUser } from '@fortawesome/free-solid-svg-icons';
import CurrentUserContext from './CurrentUserContext';
import { Link, useLocation } from 'react-router';

import * as style from './AppNavbar.scss';
import PropTypes from 'prop-types';

function AppNavbar() {
  const currentUser = useContext(CurrentUserContext);
  const location = useLocation();
  const [userState, setUserState] = useState({
    authenticated: currentUser.authenticated,
    username: currentUser.username,
  });

  useEffect(() => {
    setUserState({
      authenticated: currentUser.authenticated,
      username: currentUser.username,
    });
  }, [location, currentUser]);

  useEffect(() => {
    document.body.classList.add(style.bodyNavbar);
    return () => {
      document.body.classList.remove(style.bodyNavbar);
    };
  }, []);

  return (
    <Navbar
      expand="sm"
      fixed="top"
      bg="dark"
      variant="dark"
      className="py-1 px-2"
    >
      {/* onClick={() => onChangeView({ view: 'welcome' })} */}
      <Navbar.Brand>
        <img
          src={logoPict}
          width="30"
          height="30"
          className="d-inline-block align-top"
          alt={`${APP_ENV_APP_TITLE} Logo`}
        />
        {' '}
        {APP_ENV_APP_TITLE}
      </Navbar.Brand>
      <Navbar.Toggle aria-controls="RichChatNavbar" />
      <Navbar.Collapse id="RichChatNavbar">
        {userState.authenticated && (
          <Link to="/rooms">Mes salons</Link>
        )}
        {userState.authenticated
          ? (
              <Link to="/signOff" className="ms-auto text-danger">
                {userState.username}
                <FontAwesomeIcon icon={faSignOut} className="ms-1" />
              </Link>
            )
          : (
              <Link to="/authenticate" className="ms-auto text-secondary">
                <FontAwesomeIcon icon={faUser} className="me-2" />
                S&lsquo;authentifier
              </Link>
            )}
      </Navbar.Collapse>
    </Navbar>
  );
}

AppNavbar.propTypes = {
  onLogout: PropTypes.func.isRequired,
  onChangeView: PropTypes.func.isRequired,
};

export default AppNavbar;
