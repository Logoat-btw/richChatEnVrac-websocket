import { Alert, Col, Nav, Row } from 'react-bootstrap';
import SignupForm from './SignupForm';
import SigninForm from './SigninForm';
import { useState } from 'react';

function AuthenticationWelcome() {
  const [tabSelected, setTabSelected] = useState('1');

  return (
    <>
      <Row className="justify-content-center mb-3">
        <Col xs={12} md={8} lg={6} xl={4}>
          <Alert variant="primary">
            <Alert.Heading>{APP_ENV_APP_TITLE}</Alert.Heading>
            <p>Bienvenue la team fsd!</p>
          </Alert>
        </Col>
      </Row>
      <Row className="justify-content-center mb-3">
        <Col xs="12" md={6} lg={4}>
          <Nav
            variant="tabs"
            fill
            className="justify-content-center mb-3"
            activeKey={tabSelected}
            onSelect={setTabSelected}
          >
            <Nav.Item>
              <Nav.Link eventKey="1">S&lsquo;authentifier</Nav.Link>
            </Nav.Item>
            <Nav.Item>
              <Nav.Link eventKey="2">Créer son compte</Nav.Link>
            </Nav.Item>
          </Nav>
          {tabSelected === '1'
            ? (
                <SigninForm />
              )
            : (
                <SignupForm />
              )}
        </Col>
      </Row>
    </>
  );
}

AuthenticationWelcome.propTypes = {
};

export default AuthenticationWelcome;
