export default async function initServiceWorker() {
  if ('serviceWorker' in navigator) {
    await navigator.serviceWorker.register(
      /* webpackChunkName: "AppServiceWorker" */ new URL('./serviceWorker.js', import.meta.url),
      {
        type: 'classic',
        updateViaCache: 'imports',
      },
    ).catch((error) => {
      console.error('Service Worker registration failed:', error);
    });
    if ('Notification' in window) {
      const grantResult = await Notification.requestPermission();
      if (grantResult === 'granted') {
        // do nothing
      }
    }
  }
  else {
    console.warn('Service Workers are not supported in this browser.');
  }
}
