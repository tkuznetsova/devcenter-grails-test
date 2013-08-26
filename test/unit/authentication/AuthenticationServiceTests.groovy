package authentication

import org.junit.*
import grails.test.mixin.* // mock with grails http://www.ibm.com/developerworks/java/library/j-grails10209/
import groovy.mock.interceptor.*
import authentication.*
import eshop.Basket

@TestFor(AuthenticationController)
@Mock([AuthenticationUser, Basket]) // http://www.kelvin-williams.dk/?p=53
class AuthenticationServiceTests {
	
    def populateValidSignupParams(params) {
        assert params != null
       
		//params["basketId"] = null			 // nullable: true, blank: false
		params["login"] = 'someValidName' 	 // (size:5..64, unique: true, validator: { val, obj -> obj.authenticationService.checkLogin(val) } )
		params["password"] = 'someValidName' // (size:5..64, password: true, validator: { val, obj -> obj.authenticationService.checkPassword(val) })
		params["passwordConfirm"] = 'someValidName' // (password:true, validator: { val, obj -> obj.password == val })
		params["email"] = 'some@valid.email' 		// (email:true, nullable: true, blank: false)
		
		//params["name"] = null			 		 // (blank: true, nullable: true, maxSize:50)					//
		params["creditcardID"] = 4532302731121710// creditCard: true, blank: false, nullable: false				// r
		params["phone"] = 45323027311	 		 // minSize:9, blank: false, nullable: false, maxSize:11		// r
		params["addressline1"] = 'someValidName' // (blank: false, nullable: false, maxSize:20) 				// r
		//params["addressline2"] = null			 // (blank: true, nullable: true, maxSize:20)
		params["city"] = 'someValidName' 		 // (blank: false, nullable: false, maxSize:20)					// r
		//params["stateregion"] = null		     // (blank: true, nullable: true, maxSize:50)
		params["postalcode"] = 45323		 	 // minSize:5, blank: false, nullable: false, maxSize:7			// r
		params["country"] = 'someValidName'		 // (blank: false, nullable: false, maxSize:10)					// r
		params["creditlimit"] = 45323			 // range: 0..1E8, blank: false, nullable: false				// r
		params["role"] = 'user'			 		 // (blank: false, nullable: false, maxSize:10)					// r
    }

	void testSignupNoEmailConfirm() {
		def service = new AuthenticationService()
		
		def mockUserObject = [:]
		
		def now = new Date()

		def mockEvents = new MockFor(DummyEventHandler)
		mockEvents.demand.onFindByLogin(1) {  data -> return null }
		mockEvents.demand.onNewUserObject(1) {  data -> return mockUserObject }
		mockEvents.demand.onEncodePassword(1) { return it }
		mockEvents.demand.onConfirmAccount(1) {  data ->	return false }
		mockEvents.demand.onSaveUser(1) {  data -> 
			assert data == mockUserObject
    		assert data.login == "marc"
    		assert data.password == "secret"
    		assert data.email == "marc@anyware.co.uk"
    		assert data.status == AuthenticationService.STATUS_VALID
    		return true
		}
		mockEvents.demand.onSignup(1) {  params -> 
			assert params.user == mockUserObject
			assert params.params.dateOfBirth == now
		}
		mockEvents.demand.onLoggedIn(1) {  data -> 
			assert (data instanceof AuthenticatedUser)
		}
		
		mockEvents.use {
			service.events = new DummyEventHandler()
		
			def account = service.signup(login:"marc", password:"secret", email:"marc@anyware.co.uk", 
				immediate:true, extraParams:[dateOfBirth:now])
			assertNotNull account
			assertEquals 0, account.result
			assertEquals "marc", account.login
			assertNotNull account.loginTime
			assertTrue account.loggedIn
		}
	}
	
	void testSignupEmailConfirm() {
		def service = new AuthenticationService()

		def mockUserObject = [:]
		
		def now = new Date()
		
		def mockEvents = new MockFor(DummyEventHandler)
		mockEvents.demand.onFindByLogin(1) {  data -> return null }
		mockEvents.demand.onNewUserObject(1) {  data ->
			return mockUserObject
		}
		mockEvents.demand.onEncodePassword(1) { return it }
		mockEvents.demand.onConfirmAccount(1) {  data ->
			return true
		}
		mockEvents.demand.onSaveUser(1) {  data ->
			assert data == mockUserObject
			assert data.login == "marc"
			assert data.password == "secret"
			assert data.email == "marc@anyware.co.uk"
			assert data.status == AuthenticationService.STATUS_AWAITING_CONFIRMATION
			return true
		}
		mockEvents.demand.onSignup(1) { params ->
			assert params.user == mockUserObject
			assert params.params.dateOfBirth == now
		}

		mockEvents.use {
				service.events = new DummyEventHandler()

				def account = service.signup(login:"marc", password:"secret", email:"marc@anyware.co.uk",
					immediate:true, extraParams:[dateOfBirth:now])
				assertNotNull account
				assertEquals AuthenticatedUser.AWAITING_CONFIRMATION, account.result
				assertFalse account.loggedIn
		}
	}
	
	void testExists() {
		def service = new AuthenticationService()
		
		def mockEvents = new MockFor(DummyEventHandler)
		mockEvents.demand.onFindByLogin(1) { data ->
			assert data == "marc"
			return [:]
		}
		mockEvents.use {
			service.events = new DummyEventHandler()
			assertNotNull service.exists("marc")
		}

		// Test for non-existent login
		
		mockEvents = new MockFor(DummyEventHandler)
		mockEvents.demand.onFindByLogin(1) { data ->
			assert data == "marc"
			return null
		}
		
		mockEvents.use {
			service.events = new DummyEventHandler()
			def x = service.exists('marc')
			assertFalse x
		}
		
	}

	void testLoginExistingID() {
		def service = new AuthenticationService()
		
		def mockEvents = new MockFor(DummyEventHandler)
		mockEvents.demand.onFindByLogin(1) {  data ->
			assert data == "marc"
			return [password:'secret', status:AuthenticationService.STATUS_VALID]
		}
		mockEvents.demand.onEncodePassword(1) { return it }
		mockEvents.demand.onLoggedIn(1) {  data ->
			assertEquals "marc", data.login
		}

		mockEvents.use {
			service.events = new DummyEventHandler()
			def authToken = service.login("marc", "secret")
			assertNotNull authToken
			assertEquals 0, authToken.result
			assertEquals "marc", authToken.login
			assertTrue authToken.loggedIn
		}
	}
	
	void testLoginNoSuchID() {
		def service = new AuthenticationService()

		def mockEvents = new MockFor(DummyEventHandler)
		mockEvents.demand.onFindByLogin(1) {  data ->
			assert data == "marc"
			return null
		}

		mockEvents.use {
			service.events = new DummyEventHandler()
			def authResult = service.login("marc", "secret")
			assertNotNull authResult
			assertFalse authResult.loggedIn
			assertEquals AuthenticatedUser.ERROR_NO_SUCH_LOGIN, authResult.result
		}
	}
	
	void testLoginBadPassword() {
		def service = new AuthenticationService()
		def mockEvents = new MockFor(DummyEventHandler)
		mockEvents.demand.onFindByLogin(1) {  data ->
			assert data == "marc"
			return [password:'other', status:AuthenticationService.STATUS_VALID]
		}
		mockEvents.demand.onEncodePassword(1) { return it }

		mockEvents.use {
			service.events = new DummyEventHandler()
			def authResult = service.login("marc", "secret")
			assertNotNull authResult
			assertFalse authResult.loggedIn
			assertEquals AuthenticatedUser.ERROR_INCORRECT_CREDENTIALS, authResult.result
		}
	}
	
	void testLogout() {
		def service = new AuthenticationService()
	
		def mockEvents = new MockFor(DummyEventHandler)
		mockEvents.demand.onFindByLogin(1) {  data ->
			assert data == "marc"
			return [password:'secret', status:AuthenticationService.STATUS_VALID]
		}
		mockEvents.demand.onEncodePassword(1) { return it }
		mockEvents.demand.onLoggedIn(1) {  data ->
			assertEquals "marc", data.login
		}
		mockEvents.demand.onLoggedOut(1) {  data ->
			assertEquals "marc", data.login
		}

		mockEvents.use {
			service.events = new DummyEventHandler()

			def authToken = service.login("marc", "secret")
			assertNotNull authToken
			assertTrue authToken.loggedIn

			service.logout(authToken)
			assertFalse authToken.loggedIn
		}
	}

}
