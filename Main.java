
import java.awt.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.util.stream.Stream;



public class Main {
	 //UtherRobotica-Program to implement classification of forex market state and optimal entry conditions (Price/takeProfit/StopLoss)
	static ArrayList<ArrayList<Double>> history = new ArrayList<ArrayList<Double>>();
	static int SMA = 5;
	static double buffer = .60;
	 //history.minute
	//minute close/5sma,slopeSequence,absolute high, absolute low
	static int[] slopeSequence = {1,2,3,5,8,13,21,34,55,89,144,233,377,610};
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//1. upload historical price
		getHistory();
		 //System.out.printf("history finished");
		 
		//2. calculate SMA's (how many? what time frames? does it matter? have more but greater margin of error?)
		//and trims the array so all values have all the SMA values
		fillSMA(SMA);
		fillSlopes(slopeSequence,SMA);
		trimHistory();
		
		//3. calculate historical optimal trades (min and max price after this time)
		getMinMax();
		
		//4. Read file that ea prints to (LOOP AND REPEAT FROM HERE)
		ArrayList<Double> EAArray = getFromEA();
		//5. classify current price 
		ArrayList<ArrayList<Double>> classifiedArray = searchHistory(EAArray,buffer);
		printArray(classifiedArray);
		//6. calculate optimal trade given data
		ArrayList<Double> stopTakeProfit = calculateTrade(classifiedArray);
		//7. if history shows profitability, write trade information to file
		
		//8. ea to read file and trade
		
		
		
		
		
	}

	private static ArrayList<Double> calculateTrade(ArrayList<ArrayList<Double>> classifiedArray) {
		ArrayList<Double> orderArray = new ArrayList<Double>();
		ArrayList<Double> minArray = new ArrayList<Double>();
		ArrayList<Double> maxArray = new ArrayList<Double>();
		double averageMax = 0;
		double averageMin = 0;
		double sumMax = 0;
		double sumMin = 0;
		for(int x = 0; x<classifiedArray.size();x++){
			sumMax= sumMax+classifiedArray.get(x).get(classifiedArray.get(x).size()-1);
			sumMin= sumMin+classifiedArray.get(x).get(classifiedArray.get(x).size()-2);
			minArray.add(classifiedArray.get(x).get(classifiedArray.get(x).size()-2));
			maxArray.add(classifiedArray.get(x).get(classifiedArray.get(x).size()-1));
			
		}
		averageMax = sumMax/classifiedArray.size();
		averageMin = sumMin/classifiedArray.size();
		

		
		
		
	
		
		if(averageMax>(averageMin*-1))//Long
		{
			Collections.sort(maxArray);
			Collections.reverse(maxArray);
			Collections.sort(minArray);
			Collections.reverse(minArray);
			
			double takeProfit = 0;
			double stopLoss = 0;
			double profit = 0;
			for(int x = 0; x<maxArray.size();x++){
				double prof = (maxArray.get(x)+minArray.get(x))*((x+1)/maxArray.size());
				if(prof>profit){
					profit = prof;
					takeProfit = maxArray.get(x);
					stopLoss = minArray.get(x);
				}}
			orderArray.add(takeProfit);
			orderArray.add(stopLoss);
			orderArray.add(profit);
			orderArray.add(1.0);
			return orderArray;
			
		}
		
		if(averageMax<(averageMin*-1))//Short
		{
			Collections.sort(maxArray);
			Collections.sort(minArray);
			
			double takeProfit = 0;
			double stopLoss = 0;
			double profit = 0;
			for(int x = 0; x<maxArray.size();x++){
				double prof = (-maxArray.get(x)-minArray.get(x))*((x+1)/maxArray.size());
				if(prof>profit){
					profit = prof;
					takeProfit = minArray.get(x);
					stopLoss = maxArray.get(x);
				}}
			orderArray.add(takeProfit);
			orderArray.add(stopLoss);
			orderArray.add(profit);
			orderArray.add(-1.0);
			return orderArray;
			
			
		}
				
		
		
		return null;
	}

	private static ArrayList<ArrayList<Double>> searchHistory(ArrayList<Double> eAArray, double buffer2) {
		ArrayList<ArrayList<Double>> arrayToReturn = new ArrayList<ArrayList<Double>>();
		
		for (ArrayList<Double> foo: history) {
			arrayToReturn.add((ArrayList<Double>)foo.clone());
			}
		
		System.out.print(arrayToReturn.size());
			
			for(int y =0;y<arrayToReturn.size();y++){
				for(int z = 0;z<eAArray.size();z++){
					System.out.print(arrayToReturn.size());
					System.out.printf("%n");
					if(eAArray.get(z)>0){
					if(arrayToReturn.get(y).get(z+2)>eAArray.get(z)*(1+buffer2)||arrayToReturn.get(y).get(z+2)<eAArray.get(z)*(1-buffer2)){
						arrayToReturn.remove(y);
						y--;
						break;
					}}
					if(eAArray.get(z)<0){
						if(arrayToReturn.get(y).get(z+2)<eAArray.get(z)*(1+buffer2)||arrayToReturn.get(y).get(z+2)>eAArray.get(z)*(1-buffer2)){
							arrayToReturn.remove(y);
							y--;
							break;
						}}
			}	
		}
		
		
		return arrayToReturn;
	}

	private static ArrayList<Double> getFromEA() {

		// Create an instance of File for data.txt file.
        File file = new File("C:\\Users\\John\\desktop\\Programming\\fromEA.txt");
        try {
            // Create a new Scanner object which will read the data
            // from the file passed in. To check if there are more 
            // line to read from it we check by calling the 
            // scanner.hasNextLine() method. We then read line one 
            // by one till all lines is read.
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();//reads line
                ArrayList<String> items =new  ArrayList<String>(Arrays.asList(line.split(" ")));//splits line up using space
                ArrayList<Double> minute = new ArrayList<Double>();//creates minute array
                //System.out.printf(items.get(6)+"%n");
                for(int x = 0;x<slopeSequence.length;x++){
                minute.add(Double.parseDouble(items.get(x)));//adds the close price to the minute array
                }
                return minute;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
		return null;
		
	}

	private static void getMinMax() {
	for(int x = 1;x<history.size();x++){
		double min = history.get(x).get(0)-.001;
		Boolean low = false;
		double max = history.get(x).get(0)+.001;
		Boolean high = false;
		
		for(int y =x;y<history.size();y++){
			if(history.get(y).get(0)>max){max = history.get(y).get(0);high = true;}
			if(history.get(y).get(0)<min){min = history.get(y).get(0);low = true;}
			if(history.get(y-1).get(0)>history.get(x).get(0)&&history.get(y).get(0)<history.get(x).get(0)&&high==true&&low==true){break;}
			if(history.get(y-1).get(0)<history.get(x).get(0)&&history.get(y).get(0)>history.get(x).get(0)&&high==true&&low==true){break;}
			if(min-history.get(x).get(0)<-.01){break;}
			if(max-history.get(x).get(0)>.01){break;}
		}
		//System.out.print(min-history.get(x).get(0)+" ");
		//System.out.print(max-history.get(x).get(0));
		//System.out.printf("%n");
		if(min-history.get(x).get(0)>-.01){history.get(x).add(min-history.get(x).get(0));}
		else {history.get(x).add(-.01);}
		if(max-history.get(x).get(0)<.01){history.get(x).add(max-history.get(x).get(0));}
		else {history.get(x).add(.01);}
		
	}
		
	}

	private static void printArray(ArrayList<ArrayList<Double>> array) {
		for(int x = 0; x<array.size();x++){
			for(int y =0;y<array.get(x).size();y++){
				System.out.print(array.get(x).get(y)+" ");
			}
			System.out.printf("%n");
		}
		
	}

	private static void fillSlopes(int[] slope, int spacer) {
		for(int w = (int) slope[(slope.length-1)]+spacer;w<history.size();w++)
		{
		for(int x = 0;x<slope.length;x++){
			int g = w-slope[x];
			double p = (history.get(g).get(1)-history.get(w).get(1))/slope[x];
			history.get(w).add(p);
			
			
		}}
		
	}

	private static void trimHistory() {
		for(int x = 0; x<history.size();x++){
			if(history.get(x).size()<(2+slopeSequence.length))
			{history.remove(x);x--;}
		}
		
		
	}

	private static void fillSMA(int SMAperiod) {
		
		for(int x=(SMAperiod-1);x<history.size();x++){
			double  closeValues = 0;
				for(int y=0;y<SMAperiod;y++){
				closeValues = closeValues+history.get(x-y).get(0);
			}
			history.get(x).add(closeValues/SMAperiod);
		}
		}
	
		
	

	private static void getHistory() {
		// Create an instance of File for data.txt file.
        File file = new File("C:\\Users\\John\\desktop\\Programming\\EURUSD.txt");
        try {
            // Create a new Scanner object which will read the data
            // from the file passed in. To check if there are more 
            // line to read from it we check by calling the 
            // scanner.hasNextLine() method. We then read line one 
            // by one till all lines is read.
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();//reads line
                ArrayList<String> items =new  ArrayList<String>(Arrays.asList(line.split(",")));//splits line up using ,
                ArrayList<Double> minute = new ArrayList<Double>();//creates minute array
                //System.out.printf(items.get(6)+"%n");
                minute.add(Double.parseDouble(items.get(6)));//adds the close price to the minute array
                history.add(minute);//adds minute array to the history array
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
		
		
	}

}
