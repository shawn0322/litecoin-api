package com.guygrigsby.litecoinapi;

import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.List;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionOptions;

public class JSONRPCMethodCaller {
	
	protected String rpcUser;
	protected String rpcPassword;
	protected int rpcPort;
	protected String rpcAddress;
	
	
	
	protected JSONRPCMethodCaller(String user, String pass, int port, String serverAddress) {
		rpcUser = user;
		rpcPassword = pass;
		rpcPort = port;
		rpcAddress = serverAddress;
	}
	protected String callJSONRPCMethodForStringResponse(String method, List<Object> params) throws LitecoinAPIException {
		return callJSONRPCMethod(method, params).toString();
	}
	protected double callJSONRPCMethodForDoubleResponse(String method, List<Object> params) throws LitecoinAPIException {
		return Double.parseDouble(callJSONRPCMethod(method, params).toString());
	}
	protected Object callJSONRPCMethod(String method, List<Object> params) throws LitecoinAPIException {
		JSONRPC2Session session = getSession();
		JSONRPC2Request request;
		if (params == null) {
			request = new JSONRPC2Request(method, getNextRequestId());
		} else {
			request = new JSONRPC2Request(method, params, getNextRequestId());
		}
		JSONRPC2Response response = sendRequestToServer(session, request);
		
		if (response.indicatesSuccess())
			return response.getResult();
		else {
			handleResponseError(response);
		}
		return null;
	}
	
	private JSONRPC2Response sendRequestToServer(JSONRPC2Session session, JSONRPC2Request request) {
		// Send request
		JSONRPC2Response response = null;

		try {
			JSONRPC2SessionOptions options = new JSONRPC2SessionOptions();
			options.ignoreVersion(true);
			session.setOptions(options);
			response = session.send(request);

		} catch (JSONRPC2SessionException e) {
			e.printStackTrace();
		}
		return response;
	}
	
	protected void handleResponseError(JSONRPC2Response response) throws LitecoinAPIException {
		JSONRPC2Error err = response.getError();
		System.err.println("\terror.code    : " + err.getCode());
		System.err.println("\terror.message : " + err.getMessage());
		System.err.println("\terror.data    : " + err.getData());
		throw new LitecoinAPIException(err);
	}

	protected JSONRPC2Session getSession() {

		Authenticator.setDefault(new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(rpcUser, rpcPassword
						.toCharArray());
			}
		});
		URL serverURL = null;

		try {
			serverURL = new URL(rpcAddress + ":" + rpcPort);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return new JSONRPC2Session(serverURL);
	}

	private static int nextRequestNumber = 0;
	
	private static String getNextRequestId() {
		nextRequestNumber++;
		return "rqsr:" + nextRequestNumber; 
	}
}