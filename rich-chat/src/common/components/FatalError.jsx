import { useRouteError, Link } from 'react-router-dom';
import { Container, Row, Col, Card, Button } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faExclamationTriangle } from '@fortawesome/free-solid-svg-icons';

function FatalError() {
  const error = useRouteError();
  const errorMessage = error?.message ?? error?.error?.message ?? 'Unknown error';

  return (
    <Container className="d-flex justify-content-center align-items-center vh-100">
      <Row className="w-100">
        <Col xs={12} md={8} lg={6} className="mx-auto">
          <Card className="text-center shadow">
            <Card.Body>
              <FontAwesomeIcon icon={faExclamationTriangle} size="3x" className="text-danger mb-3" />
              <Card.Title className="mb-3">Oops! Something went wrong.</Card.Title>
              <Card.Text className="text-muted mb-4">
                {errorMessage}
              </Card.Text>
              <Button as={Link} to="/" variant="primary">
                Go Back Home
              </Button>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
}

export default FatalError;
