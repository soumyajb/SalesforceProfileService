
import java.io.FileNotFoundException;

import profileAnalyzer.core.LoginSingleton;
import profileAnalyzer.core.ProfileAnalyzerHelper;




public class Main {
	static LoginSingleton instance = null;
	static ProfileAnalyzerHelper profileAnalyserInstance = null;
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//System.out.println(args[0].toString());
		
		if(args.length <1)
		{
			System.out.println("Usage: ProfileAnalyser.jar function");
			System.out.println("function values:"+"\n"
					+ "a) CopyProfile"+"\n"
					+ "b) GetProfileDetails"+"\n"
					+ "c) Init");
			return;
		}
		switch (args[0])
		{
		 case "CopyProfile":
			 
			 instance = LoginSingleton.getInstance();
			 //if(profileAnalyserInstance != null)
			 profileAnalyserInstance = new ProfileAnalyzerHelper();
			 profileAnalyserInstance.Init(true);
			 profileAnalyserInstance.getProfileMetadata(instance, "Profile");
			 profileAnalyserInstance.processProfiles();
			 
			 break;
		 case "GetProfileDetails":
			 //if(instance != null)
			 instance = LoginSingleton.getInstance();
			 //if(profileAnalyserInstance != null)
			 profileAnalyserInstance = new ProfileAnalyzerHelper();
			 profileAnalyserInstance.Init(false);
			
			 try {
				 profileAnalyserInstance.getProfileMetadata(instance, "Profile");
				profileAnalyserInstance.buildReport();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 break;
		 case "Init":
			 System.out.println("Inside Init");
			 ProfileAnalyzerHelper profileAnalyserInstanceInit = new ProfileAnalyzerHelper();
			 profileAnalyserInstanceInit.startSetup();
			 break;
		 default:
			 System.out.println("Unknown argument: " + args[0]);
			 break;
		}
		
	}

	
	public Main() {
		super();
	}
	
}