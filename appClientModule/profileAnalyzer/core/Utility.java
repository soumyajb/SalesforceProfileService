package profileAnalyzer.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;

public class Utility {
	
	private static  Scanner s = new Scanner(System.in);
	public static String[] getInput(String text, Set<String> allPossibleVals,String stopString,Boolean isSingle)
	{
		 
		 String[] listofInput = new String[]{};
		 Boolean continueRun = false;
		 
		 try
		 {
			 
		 
		 String currentValue = "";
		 do
		 {
			 System.out.println(text+(allPossibleVals.size()==0?"":"Possible values"+allPossibleVals.toString())+".Enter "+stopString+" and press enter to exit the loop  to exit");
			 currentValue = s.nextLine().trim();
			 if(listofInput.length==0 && currentValue.trim().toLowerCase().equals(stopString.trim().toLowerCase()) && !isSingle)
			 {currentValue ="#~!#~!@~@~~~@!~~!@###@(~)~";System.out.println("This requires at least one value");}
			 //System.out.println("currentValue:"+currentValue+"stopString: "+stopString);
			 if((allPossibleVals.contains(currentValue) && allPossibleVals.size()>0)||(!currentValue.equals(stopString) && allPossibleVals.size()==0 && currentValue!="#~!#~!@~@~~~@!~~!@###@(~)~"))
			 {
				 
				 ArrayList<String> arrList = new ArrayList<String>(Arrays.asList(listofInput));
				 if(!(currentValue.trim().toLowerCase().equals(stopString.trim().toLowerCase())))
				 arrList.add(currentValue);
				 //System.out.println("arrList:"+currentValue);
				 listofInput=arrList.toArray(listofInput);
				
			 }
			 				 
			if(isSingle) 
			{continueRun = false; }
			//	continueRun = false;
			else if(stopString.toLowerCase().equals(currentValue.toLowerCase()) && listofInput.length>0) 
			{continueRun = false; }
				//continueRun = false;
			else if(allPossibleVals.size()>0 && allPossibleVals.contains(currentValue) )
				continueRun = true;
			else
				continueRun =true;
		 }while(continueRun);
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }
		 //System.out.println(listofInput[0]);
		 
		return listofInput;
	}
}
