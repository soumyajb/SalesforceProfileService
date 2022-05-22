package profileAnalyzer.core;

import java.util.Scanner;
import java.io.Console;

import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;


public class LoginSingleton {

private  MetadataConnection connectionSalesforce = null;
private LoginResult loginResult = null;
private Scanner sc = new Scanner(System.in);

private LoginSingleton()
{
	
}
private static LoginSingleton loginInstance = new LoginSingleton();

public static LoginSingleton getInstance()
{
	return loginInstance;
}

public  MetadataConnection getConnection(Boolean isTestEnv)
{
	System.out.println("Enter Username");
	final String uname = sc.nextLine();
	System.out.println("Enter Password");
	Console c = System.console();
	
		
	final String pword =c==null?sc.nextLine():String.valueOf(c.readPassword());
	System.out.println("Are you connecting to a test environment? (Y/N)");
	String currentValue =sc.nextLine();
	while(!(currentValue.toLowerCase().equals("y")||currentValue.toLowerCase().equals("n")))
	{
		System.out.println("Are you connecting to a test environment? (Y/N)");
		currentValue=sc.nextLine();
	}
	
	final String url = 	currentValue.equals("Y")?"https://test.salesforce.com/services/Soap/c/54.0":"https://login.salesforce.com/services/Soap/c/54.0";
	if(loginResult != null && connectionSalesforce!= null)
	{
		return connectionSalesforce;
	}
	
	try {
		loginResult = loginToSalesforce(uname, pword, url);
		//loginResult = loginToSalesforce("campaignload@jt.org", "@Campaign2021wPdg23EsmMit3w0m7VEKENh7u", "https://login.salesforce.com/services/Soap/c/54.0");
		connectionSalesforce =  createMetadataConnection(loginResult);
	} catch (ConnectionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	
	return connectionSalesforce;
}
private static MetadataConnection createMetadataConnection(
        final LoginResult loginResult) throws ConnectionException {
    final ConnectorConfig config = new ConnectorConfig();
    config.setServiceEndpoint(loginResult.getMetadataServerUrl());
    config.setSessionId(loginResult.getSessionId());
    return new MetadataConnection(config);
}

private static LoginResult loginToSalesforce(
        final String username,
        final String password,
        final String loginUrl) throws ConnectionException {
    final ConnectorConfig config = new ConnectorConfig();
    config.setAuthEndpoint(loginUrl);
    config.setServiceEndpoint(loginUrl);
    config.setManualLogin(true);
    return (new EnterpriseConnection(config)).login(username, password);
}
}

