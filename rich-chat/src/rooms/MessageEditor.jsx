import PropTypes from 'prop-types';
import classNames from 'classnames';
import { useEffect, useReducer, useRef } from 'react';
import { Editor, EditorState, RichUtils, convertFromRaw, convertToRaw } from 'draft-js';
import { stateToHTML } from 'draft-js-export-html';
import { Button, ButtonGroup } from 'react-bootstrap';
import { faBold, faItalic } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { getLocalStorage } from '../services/StorageService';

import 'draft-js/dist/Draft.css';
import * as style from './MessageEditor.scss';

const STORAGE_STATE_KEY = 'MESSAGE_STATE';

function debounce(callback, oldDebouncerId = null, ...callbackArguments) {
  if (oldDebouncerId !== null) {
    clearTimeout(oldDebouncerId);
  }

  return setTimeout((...args) => {
    try {
      callback(...args);
    }
    catch (e) {
      console.warn('Unable to process debounced traitement', e);
    }
  }, 1000, ...callbackArguments);
}

function createState() {
  const oldEditorValue = getLocalStorage().get(STORAGE_STATE_KEY);
  let editor = null;

  if (oldEditorValue) {
    try {
      editor = EditorState.createWithContent(convertFromRaw(oldEditorValue));
    }
    catch (e) {
      console.error('Failed to parse old editor state:', e);
    }
  }

  if (!editor) {
    editor = EditorState.createEmpty();
  }

  return {
    editor,
    debounceStoreId: null,
  };
}

function reduce(state, action) {
  switch (action?.type) {
    case 'reset':
      return { editor: EditorState.createEmpty() };
    case 'change': {
      const debounceMsgId = debounce(
        () => getLocalStorage().set(
          STORAGE_STATE_KEY,
          convertToRaw(action.value.getCurrentContent()),
        ),
        state.debounceStoreId,
      );

      return {
        ...state,
        editor: action.value,
        debounceStoreId: debounceMsgId,
      };
    }
    case 'bold':
      return { ...state, editor: RichUtils.toggleInlineStyle(state.editor, 'BOLD') };
    case 'italic':
      return { ...state, editor: RichUtils.toggleInlineStyle(state.editor, 'ITALIC') };
    default:
      throw new Error(`Illegal action type ${action.type}.`);
  }
}

function MessageEditor({ onAddMessage, className }) {
  const [state, dispatch] = useReducer(reduce, null, createState);
  const editorRef = useRef();

  useEffect(() => {
    if (editorRef.current) {
      editorRef.current.focus();
    }
  }, [editorRef]);

  const addMessage = () => {
    const htmlData = stateToHTML(state.editor.getCurrentContent());
    onAddMessage(htmlData).then((ok) => {
      if (ok) {
        dispatch({ type: 'reset' });
      }
    });
  };

  return (
    <div className={classNames('d-flex flex-column justify-content-start align-items-start', 'border border-1', className)}>
      <ButtonGroup size="sm">
        <Button variant="outline-secondary" onClick={() => dispatch({ type: 'bold' })}>
          <FontAwesomeIcon icon={faBold} title="gras" />
          {/* <span className="sr-only">gras</span> */}
        </Button>
        <Button variant="outline-secondary" onClick={() => dispatch({ type: 'italic' })}>
          <FontAwesomeIcon icon={faItalic} className="me-1" title="mettre en italique" />
          {/* <span className="sr-only">italique</span> */}
        </Button>
      </ButtonGroup>
      <div className={classNames('flex-grow-1 flex-shrink-0 w-100 overflow-y-auto', style.Editor)}>
        <Editor
          editorState={state.editor}
          onChange={value => dispatch({ type: 'change', value })}
          placeholder="Votre message"
          ref={editorRef}
        />
      </div>
      <div className="d-grid w-100">
        <Button onClick={addMessage} size="sm">Send</Button>
      </div>
    </div>
  );
}

MessageEditor.propTypes = {
  onAddMessage: PropTypes.func.isRequired,
  className: PropTypes.string,
};

export default MessageEditor;
