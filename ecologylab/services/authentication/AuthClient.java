/*
 * Created on Apr 6, 2006
 */
package ecologylab.services.authentication;

import ecologylab.appframework.ObjectRegistry;
import ecologylab.generic.BooleanSlot;
import ecologylab.services.ServicesClient;
import ecologylab.services.authentication.messages.AuthMessages;
import ecologylab.services.authentication.messages.Login;
import ecologylab.services.authentication.messages.LoginStatusResponse;
import ecologylab.services.authentication.messages.Logout;
import ecologylab.services.authentication.messages.LogoutStatusResponse;
import ecologylab.services.authentication.registryobjects.AuthClientRegistryObjects;
import ecologylab.services.messages.BadSemanticContentResponse;
import ecologylab.services.messages.ErrorResponse;
import ecologylab.services.nio.servers.NIOServerBase;
import ecologylab.xml.TranslationSpace;

/**
 * Represents the client side of an authenticating server. Requires that it is
 * connected and authenticated with the server before it can begin attempting to
 * process messages.
 * 
 * @author Zach Toups (toupsz@gmail.com)
 */
@Deprecated public class AuthClient extends ServicesClient implements AuthMessages,
        AuthClientRegistryObjects
{
    private AuthenticationListEntry entry = null;

    /**
     * @return Returns loggedIn.
     */
    public boolean isLoggedIn()
    {
        return ((BooleanSlot) objectRegistry.lookupObject(LOGIN_STATUS)).value;
    }

    /**
     * Creates a new AuthClient object using the given parameters.
     * 
     * @param server
     * @param port
     */
    public AuthClient(String server, int port)
    {
        this(server, port, null);
    }

    /**
     * Creates a new AuthClient object using the given parameters.
     * 
     * @param server
     * @param port
     * @param messageSpace
     * @param objectRegistry
     */
    public AuthClient(String server, int port, TranslationSpace messageSpace,
            ObjectRegistry objectRegistry)
    {
        this(server, port, messageSpace, objectRegistry, null);
    }

    /**
     * Creates a new AuthClient object using the given parameters.
     * 
     * @param server
     * @param port
     * @param entry
     */
    public AuthClient(String server, int port, AuthenticationListEntry entry)
    {
        this(server, port, TranslationSpace.get("authClient",
                "ecologylab.services.authentication"), new ObjectRegistry(),
                entry);
    }
	static final Class[] AUTH_CLASSES		=
	{
		Login.class, 
		Logout.class,
		LoginStatusResponse.class,
		LogoutStatusResponse.class,
		AuthenticationListEntry.class,
		BadSemanticContentResponse.class,
		ErrorResponse.class,
	};

	
    /**
     * Main constructor; creates a new AuthClient using the parameters.
     * 
     * @param server
     * @param port
     * @param messageSpace
     * @param objectRegistry
     * @param entry
     */
    public AuthClient(String server, int port, TranslationSpace messageSpace,
            ObjectRegistry objectRegistry, AuthenticationListEntry entry)
    {
        super(server, port, 
//        		messageSpace, 
        		NIOServerBase.composeTranslations(AUTH_CLASSES, "auth_client: ", port, "", messageSpace),
        		objectRegistry);

//        messageSpace.addTranslation(
//                ecologylab.services.authentication.messages.Login.class);
//        messageSpace.addTranslation(
//                ecologylab.services.authentication.messages.Logout.class);
//        messageSpace.addTranslation(ecologylab.services.authentication.AuthenticationListEntry.class);
//        messageSpace.addTranslation(
//                ecologylab.services.authentication.messages.LoginStatusResponse.class);
//
//        messageSpace.addTranslation(ecologylab.services.messages.BadSemanticContentResponse.class);
//        messageSpace.addTranslation(ecologylab.services.messages.ErrorResponse.class);

        objectRegistry.registerObject(LOGIN_STATUS, new BooleanSlot(false));

        this.entry = entry;
    }

    /**
     * @param entry
     *            The entry to set.
     */
    public void setEntry(AuthenticationListEntry entry)
    {
        this.entry = entry;
    }

    /**
     * Attempts to connect to the server using the AuthenticationListEntry that
     * is associated with the client's side of the connection. Returns true if
     * the client is connected and authenticated; false otherwise.
     */
    public boolean login()
    {
        // if we have an entry (username + password), then we can try to connect
        // to the server.
        if (entry != null)
        {
            if (this.isServerRunning())
            {
                sendLoginMessage();
            }
        }

        return this.isLoggedIn();
    }
    

    /**
     * Attempts to log out of the server using the AuthenticationListEntry that
     * is associated with the client's side of the connection. Returns true if
     * the client is connected and authenticated; false otherwise.
     */
    public boolean logout()
    {
        // if we have an entry (username + password), then we can try to connect
        // to the server.
        if (entry != null)
        {
            if (this.isServerRunning())
            {
                sendLogoutMessage();
            }
            else
            {
                this.setLoggedIn(false);
            }
        }
        else
        {
            this.setLoggedIn(false);
        }

        return this.isLoggedIn();
    }

    protected void setLoggedIn(boolean newValue)
    {
        ((BooleanSlot) objectRegistry.lookupObject(LOGIN_STATUS)).value = newValue;
    }
    
    /**
     * Sends a Logout message to the server; may be overridden by subclasses that
     * need to add addtional information to the Logout message.
     * 
     */
    protected void sendLogoutMessage()
    {
        this.sendMessage(new Logout(entry));
    }
    
    /**
     * Sends a Login message to the server; may be overridden by subclasses that
     * need to add addtional information to the Login message.
     * 
     */
    protected void sendLoginMessage()
    {
        // Login response will handle changing the LOGIN_STATUS
        this.sendMessage(new Login(entry));
    }
}
