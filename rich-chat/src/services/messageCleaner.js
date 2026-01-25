import DOMPurify from 'dompurify';

export default function cleanMesssage(message) {
  return DOMPurify.sanitize(message, { USE_PROFILES: { html: true, svg: true } });
}
