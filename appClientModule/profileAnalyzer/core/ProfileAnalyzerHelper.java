package profileAnalyzer.core;

import java.util.Arrays;
import com.sforce.soap.metadata.Error;
import com.sforce.soap.metadata.ExtendedErrorDetails;
import com.sforce.soap.metadata.LoginFlow;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import com.sforce.soap.metadata.Metadata;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.Profile;
import com.sforce.soap.metadata.ProfileActionOverride;
import com.sforce.soap.metadata.ProfileApexClassAccess;
import com.sforce.soap.metadata.ProfileApexPageAccess;
import com.sforce.soap.metadata.ProfileApplicationVisibility;
import com.sforce.soap.metadata.ProfileCategoryGroupVisibility;
import com.sforce.soap.metadata.ProfileCustomMetadataTypeAccess;
import com.sforce.soap.metadata.ProfileCustomPermissions;
import com.sforce.soap.metadata.ProfileCustomSettingAccess;
import com.sforce.soap.metadata.ProfileExternalDataSourceAccess;
import com.sforce.soap.metadata.ProfileFieldLevelSecurity;
import com.sforce.soap.metadata.ProfileFlowAccess;
import com.sforce.soap.metadata.ProfileLayoutAssignment;
import com.sforce.soap.metadata.ProfileLoginHours;
import com.sforce.soap.metadata.ProfileLoginIpRange;
import com.sforce.soap.metadata.ProfileObjectPermissions;
import com.sforce.soap.metadata.ProfileRecordTypeVisibility;
import com.sforce.soap.metadata.ProfileTabVisibility;
import com.sforce.soap.metadata.ProfileUserPermission;
import com.sforce.soap.metadata.ReadResult;
import com.sforce.soap.metadata.SaveResult;
import com.sforce.ws.ConnectionException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;


public class ProfileAnalyzerHelper {
	

public String[] profileName = {"Custom: Sales Profile"};//new String[] {};//,"Custom: Support Profile","All Permission"
public Metadata[] metadataList = new Metadata[] {};
public Profile[] profilesMetadata = new Profile[] {};

public  Boolean isInitSuccess = false;
public  Boolean doCommit = false;
public  Boolean doCopyPermissionSets = false;
public  Boolean doCopyLicenses = false;
public  String newProfileName = "";
private MetadataConnection connection;
	
public  Boolean Init(Boolean getAllInfo)
{
	try
	{
		
		Set<String> currentSet =new HashSet<String>();
		String[] currentArr = new String[] {};
		profileName = Utility.getInput("Enter the profiles to Analyze .", new HashSet<String>(), "e",false);
		
		currentSet.addAll(Arrays.asList(new String[] { "Y","N" }));
		if(getAllInfo)
		{
			currentArr = Utility.getInput("Do you want to copy the profile?",currentSet , "e",true);
			doCommit=currentArr.length!= 0 ?currentArr[0].equals("Y")?true:false:false;
			currentArr = Utility.getInput("Do you want to copy the License?", currentSet, "e",true);
			doCopyLicenses = currentArr.length!= 0 ?currentArr[0].equals("Y")?true:false:false;
			currentArr =Utility.getInput("Do you want to copy the PermissionSets?", currentSet, "e",true);
			doCopyPermissionSets =  currentArr.length!= 0 ?currentArr[0].equals("Y")?true:false:false;
			
		}
		
		
		
		isInitSuccess= true;
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
	return isInitSuccess;
}

public void  getProfileMetadata(LoginSingleton login, String type)
{
	connection=login.getConnection(false);
	//getProfileMetadataif(connection==null)
		
	
	try
	{
		ReadResult readResult =connection.readMetadata(type, profileName);
		//System.out.println("readResult lneth"+readResult.getRecords().length);
		Metadata[] results = readResult.getRecords();
		//System.out.println("results"+results);
		//System.out.println("results"+results.length);
		profilesMetadata = new Profile[profileName.length];
		int i=0;
		for(Metadata result:results)
		{
			System.out.println("Retrieved Metadata for the Profile:"+((Profile)result).getFullName());
			Profile currentProfile= (Profile) result;
			profilesMetadata[i]=currentProfile;
			
			i++;
			
			
		}
		
		
		
		
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
	
	
	
}
public Set<String> readExclusionFiles(String FileName)
{
	File exclusionFile = new File(FileName);
	Scanner sc = null ;
	Set<String> listofExcludedPermissions =  new HashSet<String> ();
	try
	{
		sc=new Scanner(exclusionFile);
		while(sc.hasNext())
		{
			listofExcludedPermissions.add(sc.nextLine());
		}
	}catch(Exception e) {
		e.printStackTrace();
	}
	finally
	{
		if(sc!= null)
		sc.close();
		sc = null;
	}
	return listofExcludedPermissions;
	
}
public void processProfiles() {
	Metadata[] updatedMetaList  = new Metadata[profilesMetadata.length];
	

	
	int counter = 0;
	for(Profile p:profilesMetadata)
	{
		if(!doCopyLicenses)
		p.setUserLicense("Salesforce Platform");
		String newProfileName = Utility.getInput("Please enter the new Profile Name", new HashSet<String>(), "e",true)[0];
		p.setFullName(newProfileName);
	    Set<String> allfiles = readExclusionFiles("ExclusionMaster");
	    for(String currentfile:allfiles){
	    	String[ ] arrayDetails = currentfile.split(",");
	    	//System.out.println(arrayDetails);
	    	if(arrayDetails[0].equals("Profile")){
	    	//System.out.println("Entered loop:"+arrayDetails[1]+":"+arrayDetails[2]);
	    	 p=removeExclusions(p,readExclusionFiles(arrayDetails[1]),arrayDetails[2]);
	    	}
	    }
	    
	   
	    updatedMetaList[counter] = p;
	    
	    counter++;
	}
		SaveResult[] saveResult = null;
		try {
			if(doCommit)
			saveResult = connection.createMetadata(updatedMetaList);
			else
			{
				System.out.println("Skipping the write of the new Profile to Org.Generating file.....");
				Profile[] proflidt = new Profile[updatedMetaList.length]; 
				int profcounter=0;
				for(Metadata dt:updatedMetaList)
				{
					proflidt[profcounter]=(Profile) dt;
				}
				profilesMetadata = proflidt;
				buildReport();
				
				
			}
			
		} catch (FileNotFoundException | ConnectionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	for(SaveResult result:saveResult)
	{
		System.out.println("Success:"+result.getSuccess());
		if(result.getSuccess())
		{
			System.out.println( result.getFullName());
		}
		for(Error e:result.getErrors())
		{
			System.out.println("Error:"+e.getMessage());
			for(String field: e.getFields())
			{
				System.out.println("ErrorField:"+field);
			}
			for(ExtendedErrorDetails errorDet:e.getExtendedErrorDetails())
			{
				System.out.println(errorDet.getValue().toString());
			}
		}
		
	}
}
public Profile removeExclusions(Profile p,Set<String> exclusionList,String typeofChild)
{
	int counter = 0;
	//System.out.println("exclusionList:"+exclusionList);
	//System.out.println("typeofChild"+typeofChild);
	switch(typeofChild.trim()) 
	{
			case "userPermissions":					 
					 ProfileUserPermission[] userpermission = p.getUserPermissions();
					 ProfileUserPermission[] replacementUserPermission = new ProfileUserPermission[userpermission.length];
					 counter=0;
					 System.out.println("userpermission:"+userpermission);
					 for(ProfileUserPermission perm:userpermission)
					 {
						 System.out.println(typeofChild+":"+perm);
						 if(!exclusionList.contains(perm.getName()) )
							 {replacementUserPermission[counter]=perm;counter++;}
						 else
							 System.out.println("Skipping the permission:"+perm.getName());
						 
					 }
					 p.setUserPermissions(replacementUserPermission);		 
				
				break;
			case "tabVisibilities":
				    ProfileTabVisibility[] tabVisibilities = p.getTabVisibilities();
				    ProfileTabVisibility[] replacementTabVisibilities = new ProfileTabVisibility[tabVisibilities.length];
					 counter = 0;
					 
					 for(ProfileTabVisibility tabVis:tabVisibilities)
					 {
						// System.out.println("tabvisibilities:"+tabVis.getTab());
						 if(!exclusionList.contains(tabVis.getTab()))
							 {replacementTabVisibilities[counter]=tabVis;counter++;}
						 else
							 System.out.println("Skipping the tabVisibilities:"+tabVis.getTab());
						 
					 }
					 p.setTabVisibilities(replacementTabVisibilities);
				break;
			case "recordTypeVisibilities":
				ProfileRecordTypeVisibility[] recordTypeVisibilities = p.getRecordTypeVisibilities();
				ProfileRecordTypeVisibility[] replacementrecordTypeVisibilities = new ProfileRecordTypeVisibility[recordTypeVisibilities.length];
				 counter = 0;
				 for(ProfileRecordTypeVisibility recType:recordTypeVisibilities)
				 {
					 if(!exclusionList.contains(recType.getRecordType()))
						 {replacementrecordTypeVisibilities[counter]=recType;counter++;}
					 else
						 System.out.println("Skipping the recordTypeVisibilities:"+recType.getRecordType());
					 
				 }
				 p.setRecordTypeVisibilities(replacementrecordTypeVisibilities);
				break;
			case "profileActionOverrides":
				ProfileActionOverride[] profileActionOverrides = p.getProfileActionOverrides();
				ProfileActionOverride[] replacementprofileActionOverrides = new ProfileActionOverride[profileActionOverrides.length];
				 counter = 0;
				 for(ProfileActionOverride profActnOvrd:profileActionOverrides)
				 {
					 if(!exclusionList.contains(profActnOvrd.getActionName()))
						 {replacementprofileActionOverrides[counter]=profActnOvrd;counter++;}
					 else
						 System.out.println("Skipping the profileActionOverrides:"+profActnOvrd.getActionName());
					 
				 }
				 p.setProfileActionOverrides(replacementprofileActionOverrides);
				break;
			case "pageAccesses":
				ProfileApexPageAccess[] pageAccesses = p.getPageAccesses();
				ProfileApexPageAccess[] replacementpageAccesses = new ProfileApexPageAccess[pageAccesses.length];
				 counter = 0;
				 for(ProfileApexPageAccess profActnOvrd:pageAccesses)
				 {
					 if(!exclusionList.contains(profActnOvrd.getApexPage()))
						 {replacementpageAccesses[counter]=profActnOvrd;counter++;}
					 else
						 System.out.println("Skipping the pageAccesses:"+profActnOvrd.getApexPage());
					 
				 }
				 p.setPageAccesses(replacementpageAccesses);
				break;
			case "objectPermissions":
				ProfileObjectPermissions[] objectPermissions = p.getObjectPermissions();
				ProfileObjectPermissions[] replacementobjectPermissions = new ProfileObjectPermissions[objectPermissions.length];
				 counter = 0;
				 for(ProfileObjectPermissions profActnOvrd:objectPermissions)
				 {
					 if(!exclusionList.contains(profActnOvrd.getObject()))
						 {replacementobjectPermissions[counter]=profActnOvrd;counter++;}
					 else
						 System.out.println("Skipping the objectPermissions:"+profActnOvrd.getObject());
					 
				 }
				 p.setObjectPermissions(replacementobjectPermissions);
				break;
			case "loginFlows":
				LoginFlow[] loginFlow = p.getLoginFlows();
				LoginFlow[] replacementloginFlow = new LoginFlow[loginFlow.length];
				 counter = 0;
				 for(LoginFlow profActnOvrd:loginFlow)
				 {
					 if(!exclusionList.contains(profActnOvrd.getFlow()))
						 {replacementloginFlow[counter]=profActnOvrd;counter++;}
					 else
						 System.out.println("Skipping the LoginFlow:"+profActnOvrd.getFlow());
					 
				 }
				 p.setLoginFlows(replacementloginFlow);
				break;
			case "layoutAssignments":
				ProfileLayoutAssignment[] layoutAssignments = p.getLayoutAssignments();
				ProfileLayoutAssignment[] replacementlayoutAssignments = new ProfileLayoutAssignment[layoutAssignments.length];
				 counter = 0;
				 for(ProfileLayoutAssignment profActnOvrd:layoutAssignments)
				 {
					 if(!exclusionList.contains(profActnOvrd.getLayout()))
						 {replacementlayoutAssignments[counter]=profActnOvrd;counter++;}
					 else
						 System.out.println("Skipping the ProfileLayoutAssignments:"+profActnOvrd.getLayout());
					 
				 }
				 p.setLayoutAssignments(replacementlayoutAssignments);
				break;
			case "flowAccesses":
				ProfileFlowAccess[] flowAccesses = p.getFlowAccesses();
				ProfileFlowAccess[] flowAccessesAssignments = new ProfileFlowAccess[flowAccesses.length];
				 counter = 0;
				 for(ProfileFlowAccess profActnOvrd:flowAccesses)
				 {
					 if(!exclusionList.contains(profActnOvrd.getFlow()))
						 {flowAccessesAssignments[counter]=profActnOvrd;counter++;}
					 else
						 System.out.println("Skipping the flowAccesses:"+profActnOvrd.getFlow());
					 
				 }
				 p.setFlowAccesses(flowAccessesAssignments);
				break;
			case "fieldPermissions":
			ProfileFieldLevelSecurity[] fieldPermissions = p.getFieldPermissions();
			ProfileFieldLevelSecurity[] replacementfieldPermissions = new ProfileFieldLevelSecurity[fieldPermissions.length];
			 counter = 0;
			 for(ProfileFieldLevelSecurity profActnOvrd:fieldPermissions)
			 {
				 if(!exclusionList.contains(profActnOvrd.getField()))
					 {replacementfieldPermissions[counter]=profActnOvrd;counter++;}
				 else
					 System.out.println("Skipping the fieldPermissions:"+profActnOvrd.getField());
				 
			 }
			 p.setFieldPermissions(replacementfieldPermissions);
			break;
			
			case "externalDataSourceAccesses":
				ProfileExternalDataSourceAccess[] externalDataSourceAccesses = p.getExternalDataSourceAccesses();
				ProfileExternalDataSourceAccess[] replacementexternalDataSourceAccesses = new ProfileExternalDataSourceAccess[externalDataSourceAccesses.length];
				 counter = 0;
				 for(ProfileExternalDataSourceAccess profActnOvrd:externalDataSourceAccesses)
				 {
					 if(!exclusionList.contains(profActnOvrd.getExternalDataSource()))
						 {replacementexternalDataSourceAccesses[counter]=profActnOvrd;counter++;}
					 else
						 System.out.println("Skipping the externalDataSourceAccesses:"+profActnOvrd.getExternalDataSource());
					 
				 }
				 p.setExternalDataSourceAccesses(replacementexternalDataSourceAccesses);
				break;
			case "customSettingAccesses":
				ProfileCustomSettingAccess[] customSettingAccesses = p.getCustomSettingAccesses();
				ProfileCustomSettingAccess[] replacementcustomSettingAccesses = new ProfileCustomSettingAccess[customSettingAccesses.length];
				 counter = 0;
				 for(ProfileCustomSettingAccess profActnOvrd:customSettingAccesses)
				 {
					 if(!exclusionList.contains(profActnOvrd.getName()))
						 {replacementcustomSettingAccesses[counter]=profActnOvrd;counter++;}
					 else
						 System.out.println("Skipping the customSettingAccesses:"+profActnOvrd.getName());
					 
				 }
				 p.setCustomSettingAccesses(replacementcustomSettingAccesses);
				break;
			case "customPermissions":
				ProfileCustomPermissions[] customPermissions = p.getCustomPermissions();
				ProfileCustomPermissions[] replacementcustomPermissions = new ProfileCustomPermissions[customPermissions.length];
				 counter = 0;
				 for(ProfileCustomPermissions profActnOvrd:customPermissions)
				 {
					 if(!exclusionList.contains(profActnOvrd.getName()))
						 {replacementcustomPermissions[counter]=profActnOvrd;counter++;}
					 else
						 System.out.println("Skipping the customSettingAccesses:"+profActnOvrd.getName());
					 
				 }
				 p.setCustomPermissions(replacementcustomPermissions);
				break;
			case "customMetadataTypeAccesses":
				ProfileCustomMetadataTypeAccess[] customMetadataTypeAccesses = p.getCustomMetadataTypeAccesses();
				ProfileCustomMetadataTypeAccess[] replacementcustomMetadataTypeAccesses = new ProfileCustomMetadataTypeAccess[customMetadataTypeAccesses.length];
				 counter = 0;
				 for(ProfileCustomMetadataTypeAccess profActnOvrd:customMetadataTypeAccesses)
				 {
					 if(!exclusionList.contains(profActnOvrd.getName()))
						 {replacementcustomMetadataTypeAccesses[counter]=profActnOvrd;counter++;}
					 else
						 System.out.println("Skipping the customMetadataTypeAccesses:"+profActnOvrd.getName());
					 
				 }
				 p.setCustomMetadataTypeAccesses(replacementcustomMetadataTypeAccesses);
				break;
			case "classAccesses":
				ProfileApexClassAccess[] classAccesses = p.getClassAccesses();
				ProfileApexClassAccess[] replacementcustomclassAccesses = new ProfileApexClassAccess[classAccesses.length];
				 counter = 0;
				 for(ProfileApexClassAccess profActnOvrd:classAccesses)
				 {
					 if(!exclusionList.contains(profActnOvrd.getApexClass()))
						 {replacementcustomclassAccesses[counter]=profActnOvrd;counter++;}
					 else
						 System.out.println("Skipping the classAccesses:"+profActnOvrd.getApexClass());
					 
				 }
				 p.setClassAccesses(replacementcustomclassAccesses);
				break;
			case "categoryGroupVisibilities":
				ProfileCategoryGroupVisibility[] categoryGroupVisibilities = p.getCategoryGroupVisibilities();
				ProfileCategoryGroupVisibility[] replacementcategoryGroupVisibilities = new ProfileCategoryGroupVisibility[categoryGroupVisibilities.length];
				 counter = 0;
				 for(ProfileCategoryGroupVisibility profActnOvrd:categoryGroupVisibilities)
				 {
					 if(!exclusionList.contains(profActnOvrd.getDataCategoryGroup()))
						 {replacementcategoryGroupVisibilities[counter]=profActnOvrd;counter++;}
					 else
						 System.out.println("Skipping the categoryGroupVisibilities:"+profActnOvrd.getDataCategoryGroup());
					 
				 }
				 p.setCategoryGroupVisibilities(replacementcategoryGroupVisibilities);
				break;
			case "applicationVisibilities":
				ProfileApplicationVisibility[] applicationVisibilities = p.getApplicationVisibilities();
				ProfileApplicationVisibility[] replacementapplicationVisibilities = new ProfileApplicationVisibility[applicationVisibilities.length];
				 counter = 0;
				 for(ProfileApplicationVisibility profActnOvrd:applicationVisibilities)
				 {
					 if(!exclusionList.contains(profActnOvrd.getApplication()))
						 {replacementapplicationVisibilities[counter]=profActnOvrd;counter++;}
					 else
						 System.out.println("Skipping the applicationVisibilities:"+profActnOvrd.getApplication());
					 
				 }
				 p.setApplicationVisibilities(replacementapplicationVisibilities);
				break;
	
			
			default:
				break;
	}
	
	return p;
}

public void startSetup() {
	// TODO Auto-generated method stub
	File files = new File("ExclusionMaster");
	Scanner filesScanner = null;
	try {
		filesScanner= new Scanner(files);
		while(filesScanner.hasNext())
		{
			String currentLine = filesScanner.nextLine();
			String[] currentArray = currentLine.split(",");
			File currentFile = new File(currentArray[1]);
			if(!currentFile.isDirectory() && !currentFile.exists())
				currentFile.createNewFile();
		}
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	finally
	{
		if(filesScanner != null )
			filesScanner.close();
	}
	
}

public String buildReport() throws FileNotFoundException
{
	File template  = new File("ProfileTemplate.html");
	String fullcontent = "";
	Scanner sc =null;
	try {
		sc= new Scanner(template);
		while(sc.hasNext())
		{
			fullcontent = fullcontent + sc.nextLine();
		}
		
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
	finally
	{
		if(sc != null)
		sc.close();
	}
	
	//System.out.println(fullcontent);
   // System.out.println("Total Count"+profilesMetadata.length);
	for(Profile p:profilesMetadata)
	{
		String fullname = p.getFullName();
		fullcontent=fullcontent.replace("{ProfileNameHeader}",fullname);
		//System.out.println("fullcontent"+fullcontent);
		fullcontent=fullcontent.replace("{bodydetails}",buildProfilePage(p));
		//System.out.println("fullcontent2"+fullcontent);
		FileWriter fw = null;
		File file = null;
		try {
			 System.out.println(System.getProperty("user.dir"));
			 file = new File(p.getFullName().replace(" ","_").replace(":", "_")+"_"+p.getUserLicense()+".html");
			 System.out.println(file.getAbsolutePath());
			 fw= new FileWriter(file);
			fw.write(fullcontent);
		    System.out.println(file.getAbsolutePath());
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		finally {
			if(fw!= null)
				try {
					fw.close();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			file=null;
		}
	}
	return newProfileName;
	
}
public static String buildProfilePage(Profile p)
{
	
	String start = "<section><div id=\"content\" class=\"content\"></div><h2 class=\"header\">test</h2><table class=\"customTable\"><thead><tr>";
	String afterheader = "</tr></thead></tr></thead><tbody><tbody>";
	String tempHeader= "";
	String tempTable = "";
	String endheader = "</tbody></table></section>";
	String finalContent = "";
	ProfileApplicationVisibility[] appVisib = p.getApplicationVisibilities();
	tempHeader = "<th>Application Name</th><th>Visible</th><th>Default?</th>";
	tempTable ="";
	for(ProfileApplicationVisibility a:appVisib)
	{
		tempTable = tempTable+ "<tr><td>"+a.getApplication() +"</td><td>"+ a.getVisible()+"</td><td>"+ a.getDefault()+"</td></tr>";
	}
	finalContent =finalContent+start.replace("test","Profile Application Visibility")+tempHeader+afterheader + tempTable+ endheader;
	ProfileCategoryGroupVisibility[] categGroupVisi = p.getCategoryGroupVisibilities();
	tempHeader = "<th>DataCategoryGroup</th><th>Visible</th><th>Data Categories</th>";
	tempTable ="";
	for(ProfileCategoryGroupVisibility a:categGroupVisi)
	{
		String datacat = "<ul>";
		for(String currtem:a.getDataCategories())
		{
			datacat = datacat+"<li>"+currtem+"</li>";
		}
		datacat=datacat+"</ul>";
		tempTable = tempTable+ "<tr><td>"+a.getDataCategoryGroup() +"</td><td>"+ a.getVisibility()+"</td><td>"+ datacat+"</td></tr>";
	}
	finalContent =finalContent+start.replace("test","ProfileCategoryGroupVisibility")+tempHeader+afterheader + tempTable+ endheader;
	ProfileApexClassAccess[] classAccess = p.getClassAccesses();
	tempHeader = "<th>Apex Class</th><th>Enabled</th>";
	tempTable ="";
	for(ProfileApexClassAccess a:classAccess)
	{
		tempTable = tempTable+ "<tr><td>"+a.getApexClass() +"</td><td>"+ a.getEnabled()+"</td></tr>";
	}
	finalContent =finalContent+start.replace("test","Apex Class Access")+tempHeader+afterheader + tempTable+ endheader;
	Boolean isCustom = p.getCustom();
	
	ProfileCustomMetadataTypeAccess[] customMetatypAccess = p.getCustomMetadataTypeAccesses();
	tempHeader = "<th>Meta Data Name</th><th>Enabled</th>";
	tempTable ="";
	for(ProfileCustomMetadataTypeAccess a:customMetatypAccess)
	{
		tempTable = tempTable+ "<tr><td>"+a.getName() +"</td><td>"+ a.getEnabled()+"</td></tr>";
	}
	finalContent =finalContent+start.replace("test","ProfileCustomMetadataTypeAccess")+tempHeader+afterheader + tempTable+ endheader;
	
	ProfileCustomPermissions[] custPerm = p.getCustomPermissions();
	tempHeader = "<th>Custom Permission Name</th><th>Enabled</th>";
	tempTable ="";
	for(ProfileCustomPermissions a:custPerm)
	{
		tempTable = tempTable+ "<tr><td>"+a.getName() +"</td><td>"+ a.getEnabled()+"</td></tr>";
	}
	finalContent =finalContent+start.replace("test","Custom Permission")+tempHeader+afterheader + tempTable+ endheader;
	
	
	ProfileCustomSettingAccess[] custSettingAccess = p.getCustomSettingAccesses();
	tempHeader = "<th>Custom Settings Name</th><th>Enabled</th>";
	tempTable ="";
	for(ProfileCustomSettingAccess a:custSettingAccess)
	{
		tempTable = tempTable+ "<tr><td>"+a.getName() +"</td><td>"+ a.getEnabled()+"</td></tr>";
	}
	finalContent =finalContent+start.replace("test","ProfileCustomSettingAccess")+tempHeader+afterheader + tempTable+ endheader;
	
	
	
	String desc = p.getDescription();
	
	
	ProfileExternalDataSourceAccess[] extDataSrc = p.getExternalDataSourceAccesses();
	
	tempHeader = "<th>External DataSource Access Name</th><th>Enabled</th>";
	tempTable ="";
	for(ProfileExternalDataSourceAccess a:extDataSrc)
	{
		tempTable = tempTable+ "<tr><td>"+a.getExternalDataSource() +"</td><td>"+ a.getEnabled()+"</td></tr>";
	}
	finalContent =finalContent+start.replace("test","External DataSource Access")+tempHeader+afterheader + tempTable+ endheader;
	
	ProfileFieldLevelSecurity[] fieldPerm = p.getFieldPermissions();
	
	tempHeader = "<th>Field Name</th><th>Editable?</th><th>Readable?</th>";
	tempTable ="";
	for(ProfileFieldLevelSecurity a:fieldPerm)
	{
		tempTable = tempTable+ "<tr><td>"+a.getField() +"</td><td>"+ a.getEditable()+"</td><td>"+ a.getReadable()+"</td></tr>";
	}
	finalContent =finalContent+start.replace("test","Field Level Security")+tempHeader+afterheader + tempTable+ endheader;
	
	
	ProfileFlowAccess[] flowaccess = p.getFlowAccesses();
	
	tempHeader = "<th>Flow Name</th><th>Editable</th>";
	tempTable ="";
	for(ProfileFlowAccess a:flowaccess)
	{
		tempTable = tempTable+ "<tr><td>"+a.getFlow()+"</td><td>"+ a.getEnabled()+"</td></tr>";
	}
	finalContent =finalContent+start.replace("test","Flow Access")+tempHeader+afterheader + tempTable+ endheader;
	
	LoginFlow[] loginflow = p.getLoginFlows();
	
	tempHeader = "<th>Flow</th><th>Friendly Name</th><th>VF Flow Page</th><th>VF flow Page Title</th><th>Flow Type</th><th>UI Login Flow Type</th><th>Use Lightning Runtime</th>";
	tempTable ="";
	for(LoginFlow a:loginflow)
	{
		tempTable = tempTable+ "<tr><td>"+a.getFlow()+"</td>"
				+ "<td>"+ a.getFriendlyName()+"</td><td>"+ a.getVfFlowPage()+"</td>"
				+ "<td>"+ a.getVfFlowPageTitle()+"</td><td>"+a.getFlowType() +"</td>"
				+ "<td>"+a.getUiLoginFlowType()+"</td><td>"+a.getUseLightningRuntime() +"</td></tr>";
	}
	finalContent =finalContent+start.replace("test","Login Flow")+tempHeader+afterheader + tempTable+ endheader;
	
	ProfileLayoutAssignment[] layoutAssign = p.getLayoutAssignments();
	
	tempHeader = "<th>Record Type</th><th>Layout Name</th>";
	tempTable ="";
	for(ProfileLayoutAssignment a:layoutAssign)
	{
		tempTable = tempTable+ "<tr><td>"+a.getRecordType() +"</td><td>"+ a.getLayout()+"</td></tr>";
	}
	finalContent =finalContent+start.replace("test","Page Layout")+tempHeader+afterheader + tempTable+ endheader;
	
	
	LoginFlow[]  lflow= p.getLoginFlows();
	tempHeader = "<th>Flow</th><th>Friendly Name</th><th>VF Flow Page</th><th>VF flow Page Title</th><th>Flow Type</th><th>UI Login Flow Type</th><th>Use Lightning Runtime</th>";
	tempTable ="";
	for(LoginFlow a:lflow)
	{
		tempTable = tempTable+ "<tr><td>"+a.getFlow()+"</td>"
				+ "<td>"+ a.getFriendlyName()+"</td><td>"+ a.getVfFlowPage()+"</td>"
				+ "<td>"+ a.getVfFlowPageTitle()+"</td><td>"+a.getFlowType() +"</td>"
				+ "<td>"+a.getUiLoginFlowType()+"</td><td>"+a.getUseLightningRuntime() +"</td></tr>";
	}
	finalContent =finalContent+start.replace("test","Login Flow")+tempHeader+afterheader + tempTable+ endheader;
	
	
	
	
	
	ProfileLoginHours loginHour = p.getLoginHours();
	//System.out.println("res: "+(loginHour != null?"Not Null":"Null"));
	
	tempHeader = "<th>Record Type</th><th>Layout Name</th>";
	tempTable ="";
	
    tempTable = tempTable+ "<tr><td>Monday+</td><td>"+(loginHour != null?loginHour.getMondayStart():"Not Setup")+"</td><td>"+(loginHour !=null ?loginHour.getMondayEnd():"Not Setup")+"</td></tr>";
    tempTable = tempTable+ "<tr><td>Tuesday+</td><td>"+(loginHour != null?loginHour.getTuesdayStart():"Not Setup")+"</td><td>"+(loginHour !=null ?loginHour.getTuesdayEnd():"Not Setup")+"</td></tr>";
    tempTable = tempTable+ "<tr><td>Wednesday+</td><td>"+(loginHour != null?loginHour.getWednesdayStart():"Not Setup")+"</td><td>"+(loginHour !=null ?loginHour.getWednesdayEnd():"Not Setup")+"</td></tr>";
    tempTable = tempTable+ "<tr><td>Thursday+</td><td>"+(loginHour != null?loginHour.getThursdayStart():"Not Setup")+"</td><td>"+(loginHour !=null ?loginHour.getThursdayEnd():"Not Setup")+"</td></tr>";
    tempTable = tempTable+ "<tr><td>Friday+</td><td>"+(loginHour != null?loginHour.getFridayStart():"Not Setup")+"</td><td>"+(loginHour !=null ?loginHour.getFridayEnd():"Not Setup")+"</td></tr>";
    tempTable = tempTable+ "<tr><td>Saturday+</td><td>"+(loginHour != null?loginHour.getSaturdayStart():"Not Setup")+"</td><td>"+(loginHour !=null ?loginHour.getSaturdayEnd():"Not Setup")+"</td></tr>";
    tempTable = tempTable+ "<tr><td>Sunday+</td><td>"+(loginHour != null?loginHour.getSundayStart():"Not Setup")+"</td><td>"+(loginHour !=null ?loginHour.getSundayEnd():"Not Setup")+"</td></tr>";
	
	finalContent =finalContent+start.replace("test","Page Layout")+tempHeader+afterheader + tempTable+ endheader;
	
	ProfileLoginIpRange[] loginRange = p.getLoginIpRanges();
	
	tempHeader = "<th>Description</th><th>Start Address</th><th>End Address</th>";
	tempTable ="";
	for(ProfileLoginIpRange a:loginRange)
	{
		tempTable = tempTable+ "<tr><td>"+a.getDescription() +"</td><td>"+ a.getStartAddress()+"</td><td>"+ a.getEndAddress()+"</td></tr>";
	}
	finalContent =finalContent+start.replace("test","Profile LoginIp Range")+tempHeader+afterheader + tempTable+ endheader;
	
	ProfileObjectPermissions[] objPermission = p.getObjectPermissions();
	
	tempHeader = "<th>Obeject Name</th><th>Allow Create</th>"
			+ "<th>Allow Delete</th><th>Allow Edit</th>"
			+ "<th>Allow Read</th><th>Modify AllRecords"
			+ "</th><th>View All Records</th>";
	tempTable ="";
	for(ProfileObjectPermissions a:objPermission)
	{
		tempTable = tempTable+ "<tr><td>"+a.getObject() +"</td><td>"+ a.getAllowCreate()+"</td>"
				+ "<td>"+ a.getAllowDelete()+"</td><td>"+ a.getAllowEdit()+"</td>"
						+ "<td>"+ a.getAllowRead()+"</td><td>"+ a.getModifyAllRecords()+"</td>"
								+ "<td>"+ a.getViewAllRecords()+"</td></tr>";
	}
	finalContent =finalContent+start.replace("test","Object Permission")+tempHeader+afterheader + tempTable+ endheader; 
	
	ProfileApexPageAccess[] pageAccess = p.getPageAccesses();
	
	tempHeader = "<th>Apex Page</th><th>Enabled</th>";
	tempTable ="";
	for(ProfileApexPageAccess a:pageAccess)
	{
		tempTable = tempTable+ "<tr><td>"+a.getApexPage() +"</td><td>"+ a.getEnabled()+"</td></tr>";
	}
	finalContent =finalContent+start.replace("test","ProfileApexPageAccess")+tempHeader+afterheader + tempTable+ endheader;
	
	ProfileActionOverride[] profileActionOverrde = p.getProfileActionOverrides();
	
	tempHeader = "<th>Action Name</th><th>Content </th><th>Page/SObject name</th><th>Record Type</th>";
	tempTable ="";
	for(ProfileActionOverride a:profileActionOverrde)
	{
		tempTable = tempTable+ "<tr><td>"+a.getActionName() +"</td><td>"+ a.getContent()+"</td><td>"+ a.getPageOrSobjectType()+"</td><td>"+ a.getRecordType()+"</td></tr>";
	}
	finalContent =finalContent+start.replace("test","Action Override")+tempHeader+afterheader + tempTable+ endheader;
	
	
	ProfileRecordTypeVisibility[] recordTypeVisib = p.getRecordTypeVisibilities();
	
	tempHeader = "<th>Record Type</th><th>Default</th><th>PersonAccountDefault</th><th>Visible</th>";
	tempTable ="";
	for(ProfileRecordTypeVisibility a:recordTypeVisib)
	{
		tempTable = tempTable+ "<tr><td>"+a.getRecordType() +"</td><td>"+ a.getDefault()+"</td><td>"+ a.getPersonAccountDefault()+"</td><td>"+ a.getVisible()+"</td></tr>";
	}
	finalContent =finalContent+start.replace("test","ProfileRecordTypeVisibility")+tempHeader+afterheader + tempTable+ endheader;
	
	ProfileTabVisibility[] tabVisibi = p.getTabVisibilities();
	
	tempHeader = "<th>Tab Name</th><th>Visibility</th>";
	tempTable ="";
	for(ProfileTabVisibility a:tabVisibi)
	{
		tempTable = tempTable+ "<tr><td>"+(a!=null?a.getTab():"No Tab information") +"</td><td>"+ (a!=null?a.getVisibility():"No Visibility information")+"</td></tr>";
	}
	finalContent =finalContent+start.replace("test","Tab Visibility")+tempHeader+afterheader + tempTable+ endheader;
	
	String userLicense = p.getUserLicense();
	ProfileUserPermission[] userPermission = p.getUserPermissions();
	
	tempHeader = "<th>Permission</th><th>Enabled</th>";
	tempTable ="";
	for(ProfileUserPermission a:userPermission)
	{
		tempTable = tempTable+ "<tr><td>"+a.getName() +"</td><td>"+ a.getEnabled()+"</td></tr>";
	}
	finalContent =finalContent+start.replace("test","User Permission")+tempHeader+afterheader + tempTable+ endheader;
	
    //System.out.println("------------------------------------------------------------------------------------");
    //System.out.println(finalContent);
    //System.out.println("------------------------------------------------------------------------------------");
	return finalContent;
	
}
}
