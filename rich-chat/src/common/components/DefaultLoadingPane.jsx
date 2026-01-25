import { Col, Row } from 'react-bootstrap';
import LoadingSpinner from './LoadingSpinner';

function DefaultLoadingPane() {
  return (
    <Row className="justify-content-center mb-3">
      <Col xs="auto">
        <LoadingSpinner As="h1" />
      </Col>
    </Row>
  );
}

export default DefaultLoadingPane;
