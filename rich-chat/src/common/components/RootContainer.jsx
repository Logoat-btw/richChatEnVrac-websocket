import { Container } from 'react-bootstrap';
import AppNavbar from './AppNavbar';
import { Outlet, useLoaderData } from 'react-router'; //
import CurrentUserContext from './CurrentUserContext';
import { useEffect } from 'react';
import initServiceWorker from '../../services/serviceWorkerLauncher';

function RootContainer() {
  // const [userManager] = useState(new CurrentUserManager());
  // const [viewConfig, setViewConfig] = useState({ view: 'welcome' });

  // useEffect(() => {
  //   userManager.checkUser().then(() => {
  //     if (userManager.authenticated && viewConfig.view === 'welcome') {
  //       // An authenticated user should not be on the welcome view: redirect to rooms
  //       setViewConfig({ view: 'rooms' });
  //     }
  //     else if (!userManager.authenticated && (viewConfig.view === 'rooms' || viewConfig.view === 'room')) {
  //       // A unauthenticated user should not be on the rooms or room views: redirect to welcome
  //       setViewConfig({ view: 'welcome' });
  //     }
  //   });
  // }, [userManager, viewConfig]);

  // const doLogout = async () => {
  //   await userManager.logout();
  //   setViewConfig({ view: 'welcome' });
  // };

  useEffect(() => {
    initServiceWorker();
  }, []);

  return (
    <>
      <AppNavbar />
      <main>
        <Container fluid />
        <Outlet />
      </main>
    </>

  );
}

export default RootContainer;
