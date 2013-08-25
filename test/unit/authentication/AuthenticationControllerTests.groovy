package authentication

import org.junit.*
import grails.test.mixin.* // mock with grails http://www.ibm.com/developerworks/java/library/j-grails10209/
import authentication.AuthenticationUser
import eshop.Basket

@TestFor(AuthenticationController)
@Mock([AuthenticationUser, SignupForm, LoginForm, AuthenticationService, Basket]) // http://www.kelvin-williams.dk/?p=53
class AuthenticationControllerTests {

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

	void testSignup() {
		populateValidSignupParams(params)
		controller.signup()
		if(params.login) {
			def user = AuthenticationUser.findByLogin(params.login)
			assert session.user, user
		}
	}
}
