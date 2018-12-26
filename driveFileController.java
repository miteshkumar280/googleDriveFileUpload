package com.example.driveAPI.drivefile.controller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.driveAPI.drivefile.FileResponse;
import com.fasterxml.jackson.core.JsonFactory;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Value;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

@RestController
public class driveFileController {
	
	private static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	
	private static List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
	
	private static final String USER_IDENTITY_KEY = "MY_DUMMY_USER";
	
	@Value("${google.oauth.callback.uri}")
	private String CALLBACK_URI;
	
	@Value("{google.secret.key.path}")
	private Resource gdSecretKeys;
	
	@Value("{google.credentials.folder.path}")
	private Resource credentialsFolder;
	
	private GoogleAuthorizationCodeFlow flow;
	
	public driveFileController(){
		
	}
	
	@PostConstruct
	public void init() throws Exception{
		GoogleClientSecrets secrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(gdSecretKeys.getInputStream()));
		flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY , secrets, SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(credentialsFolder.getFile())).build();
	}
	
	@GetMapping(value = {"/"})
	public String showHomePage() throws Exception{
		boolean isUserAuthenticated = false;
		
		Credential credential = flow.loadCredential(USER_IDENTITY_KEY);
		if(credential != null)
		{
			 boolean tokenValid = credential.refreshToken();
			 if(tokenValid)
			 {
				  isUserAuthenticated = true;
			 }
		}
		return isUserAuthenticated?"dashBoard.html":"index.html";
	}

	
	@GetMapping(value = {"/googlesignin"})
	public void doGoogleSignIn(HttpServletResponse response) throws Exception{
		GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
		String redirectUrl = url.setRedirectUri(CALLBACK_URI).setAccessType("offline").build();
		response.sendRedirect(redirectUrl);
		
	}
	
	@GetMapping(value = {"/oauth"})
	public String saveAuthorizationCode(HttpServletRequest request) throws Exception{
		String code = request.getParameter("code");
		if(code != null){
			saveToken(code);
			return "dashboard.html";
		}
		return "index.html";
	}

	private void saveToken(String code) throws IOException {
		// TODO Auto-generated method stub
		
		GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(CALLBACK_URI).execute();
		flow.createAndStoreCredential(response, CALLBACK_URI);
		
	}
	
	
	@GetMapping(value = {"/create"})
	public void createFile(HttpServletResponse response) throws Exception{
		Credential cred = flow.loadCredential(USER_IDENTITY_KEY);
		Drive drive = new Drive.Builder(HTTP_TRANSPORT,JSON_FACTORY , cred).setApplicationName("SpringBootGoogleDriveAPI").build();
		File file = new File();
		file.setName("profile.jpg");
		
		FileContent content = new FileContent("image/png", new java.io.File("D:\\sample.jpg"));
		File uploadFile = drive.files().create(file,content).setFields("id").execute();
		
		String fileReference = String.format("{fileId : '%s'}", uploadFile.getId());
		response.getWriter().write(fileReference);
		
	}
	
	@GetMapping(value = {"/list"}, produces = {"application/json"})
	public @ResponseBody List<FileResponse> listFiles() throws Exception{
		Credential cred = flow.loadCredential(USER_IDENTITY_KEY);
		Drive drive = new Drive.Builder(HTTP_TRANSPORT,JSON_FACTORY , cred).setApplicationName("SpringBootGoogleDriveAPI").build();
		List<FileResponse> response = new ArrayList<>();
		FileList list = drive.files().list().setFields("files(id, name)").execute();
		for(File file : list.getFiles()){
			FileResponse fileResponse = new FileResponse();
			fileResponse.setId(file.getId());
			fileResponse.setName(file.getName());
			response.add(fileResponse);
		}
		return response;
		
	}
	
	@GetMapping(value = {"/uploadFile"})
	public void uploadFile(HttpServletResponse response) throws Exception{
		Credential cred = flow.loadCredential(USER_IDENTITY_KEY);
		Drive drive = new Drive.Builder(HTTP_TRANSPORT,JSON_FACTORY , cred).setApplicationName("SpringBootGoogleDriveAPI").build();
		File file = new File();
		file.setName("profile.jpg");
		file.setParents(Arrays.asList("18BC9OHzqRvP6QieTf64mR1_y7i_9krvp?ogsrc=32"));
		FileContent content = new FileContent("image/png", new java.io.File("D:\\sample.jpg"));
		File uploadFile = drive.files().create(file,content).setFields("id").execute();
		
		String fileReference = String.format("{fileId : '%s'}", uploadFile.getId());
		response.getWriter().write(fileReference);
		
	}
	
	
}
