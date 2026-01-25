import { createRoot } from 'react-dom/client';
import RootContainer from './common/components/RootContainer';

import './bootstrap-config.scss';
import './App.scss';
import { createBrowserRouter, redirect } from 'react-router';
import { RouterProvider } from 'react-router-dom';
import CurrentUserManager from './services/CurrentUserManager';
import CurrentUserContext from './common/components/CurrentUserContext';
import { ToastProvider } from './common/components/ToastContext'; // Gardé de src
import AuthenticationWelcome from './welcome/AuthenticationWelcome';
import RoomManagementView from './rooms/RoomManagementView';
import { createRoom, deleteRoom, getRooms } from './services/restNetwork';
import RoomView from './rooms/RoomView';
import { getRoom, getRoomMessages } from './services/restNetwork';
import FatalError from './common/components/FatalError';

const USER_MANAGER = new CurrentUserManager();

const router = createBrowserRouter([
  {
    path: '/',
    ErrorBoundary: FatalError,
    Component: RootContainer,
    id: 'root',
    loader: async () => {
      await USER_MANAGER.checkUser();
      return {
        user: {
          id: USER_MANAGER.id,
          username: USER_MANAGER.username,
          authenticated: USER_MANAGER.authenticated,
          email: USER_MANAGER.email,
        },
      };
    },
    children: [
      {
        index: true,
        loader: async () => {
          await USER_MANAGER.checkUser();
          return USER_MANAGER.authenticated ? redirect('/rooms') : redirect('/authenticate');
        },
      },
      {
        path: '/authenticate', // src-2 utilise /authenticate, src utilisait authentication
        Component: AuthenticationWelcome,
        loader: async () => {
          await USER_MANAGER.checkUser();
          if (USER_MANAGER.authenticated) {
            return redirect('/');
          }
        },
        action: async ({ request }) => {
          const formData = await request.formData();
          const email = formData.get('email');
          const password = formData.get('password');
          await USER_MANAGER.login(email, password);
          return redirect('/rooms');
        },
      },
      {
        path: '/rooms',
        Component: RoomManagementView,
        loader: async () => {
          await USER_MANAGER.checkUser();
          if (!USER_MANAGER.authenticated) {
            return redirect('/');
          }
          return { rooms: await getRooms() };
        },
        action: async ({ request }) => {
          const formData = await request.formData();
          const name = formData.get('name');
          const color = formData.get('color');
          const room = await createRoom({ name, color });
          return redirect(`/rooms/${room.id}`);
        },
      },
      {
        path: '/rooms/:id',
        Component: RoomView,
        loader: async ({ params }) => {
          const roomId = params.id;
          const [room, messages] = await Promise.all([
            getRoom({ roomId }),
            getRoomMessages({ roomId }),
          ]);

          const membersById = new Map();
          membersById.set(room.owner.id, room.owner);
          room.guests?.forEach((guest) => {
            membersById.set(guest.member.id, guest.member);
          });

          messages.forEach((msg) => {
            msg.author = membersById.get(msg.authorId) ?? {
              id: msg.authorId,
              username: `Unknown author ${msg.authorId}`,
            };
          });

          return { room, messages };
        },
        action: async ({ request, params }) => {
          if (request.method === 'DELETE') {
            await deleteRoom({ roomId: params.id });
            return redirect('/rooms');
          }
        },
      },
      {
        path: 'signOff',
        loader: async () => {
          await USER_MANAGER.logout();
          return redirect('/');
        },
      },
    ],
  },
]);

const root = createRoot(document.getElementById('appMountPoint'));

root.render(
  <CurrentUserContext.Provider value={USER_MANAGER}>
    <ToastProvider>
      <RouterProvider router={router} />
    </ToastProvider>
  </CurrentUserContext.Provider>,
);
