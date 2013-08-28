package authentication

class DummyEventHandler {
    def onValidateLogin( loginID ) {  }
    def onValidatePassword( password ) { }
    def onEncodePassword( password ) { }
    def onFindByLogin( loginID ) {	}
    def onNewUserObject( loginID ) {  }
    def onSaveUser( user ) {  }
    def onLoggedIn( AuthenticatedUser login ) { }
    def onLoggedOut( AuthenticatedUser login ) { }
    def onSignup( params) { 
		def userInstance = AuthenticationUser.findByLogin(params.loginID)
		if(!userInstance) {
			flash.message = message(code: 'default.not.registered.message')
			redirect(controller: "main", action: "index")
			return 1
		} else {
			if(userInstance.password != authenticationService.encodePassword(params.password)) {
				flash.message = message(code: 'default.bad.password.message')
				redirect(action: "login_form")
				return 1 
			}
			return 0
		}
	}
    def onDelete( user ) { user.delete() }
    def onPasswordReset( login ) { }
    def onConfirmAccount( user ) { }    
    def onHasAuthorization( params ) { }    
    def onCheckAuthorized( params ) { }    
    def onUnauthorizedAccess( params ) { }    
}