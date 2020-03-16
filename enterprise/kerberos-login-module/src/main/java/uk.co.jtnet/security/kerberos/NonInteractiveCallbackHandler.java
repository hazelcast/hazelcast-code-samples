package uk.co.jtnet.security.kerberos;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

public class NonInteractiveCallbackHandler implements CallbackHandler {

	@Override
	public void handle(Callback[] callbacks) throws IOException,
	UnsupportedCallbackException {
		for (int i = 0; i < callbacks.length; i++) {
			throw new UnsupportedCallbackException(callbacks[i], "Non interactive login. Call back to user not supported as there isn't one.");
		}
	}

}
