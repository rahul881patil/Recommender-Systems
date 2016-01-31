package recoSystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class myComputation {

	// read data from a file and return 2 d matrix 
	public int[][] getDataMat(String fileName, int users, int items){

		int [][] dataMat = new int [users+1][items+1]; 
		try{
				BufferedReader br = new BufferedReader(new FileReader(fileName));
				String c_line = new String();
				while ((c_line = br.readLine() ) != null) {
					String [] data = c_line.split(" ");
					int userN = Integer.parseInt(data[0]);
					int itemN = Integer.parseInt(data[1]);
					int rating = Integer.parseInt(data[2]);
					//System.out.println(" User: "+ userN + " Item: "+ itemN + " Rate: "+ rating );
					dataMat[userN][itemN] = rating;
				}
				br.close();	
		}catch(Exception e){
			e.printStackTrace();
		}
		return dataMat;
	}
	
	// get the user rating row where whole matrix and user name has been given
	public int[] getUserRatingRow(int [][] dataMat, int userN, int items){
		int [] userRatingRow = new int [items + 1];
		try{
				for(int i=1; i<= items; i++){
					userRatingRow[i] = dataMat[userN][i];
				}
			
		}catch(Exception e){
			e.getMessage();
		}
		return userRatingRow;
	}
	
	//print user rating from a user rating row
	public void printUserRatingRow(int [] userRatingRow, int items){
		System.out.print("User Rating of User: ");
		for(int i = 1; i<= items; i++ ){
			System.out.print(userRatingRow[i]+" ");
		}
		System.out.println();
	}
	
	//print user rating from a user rating row - overloading
		public void printUserRatingRow(double [] userRatingRow, int items){
			System.out.print("User Rating of User: ");
			for(int i = 1; i<= items; i++ ){
				System.out.print(userRatingRow[i]+" ");
			}
			System.out.println();
		}
	
	// normalize user rating row
	public double[] getUserRatingRowNormalized(int [] userRatingRow, int items){
		double [] UserRatingNormalized = new double[items+1];
		float userAVG = this.getUserRatingAVG(userRatingRow, items);
		double userStdDeviation = this.getUserStdDeviation(userRatingRow, items);
		for (int i = 1; i <= items; i++){
			if(userRatingRow[i] != 0 ){
				UserRatingNormalized[i] = (userRatingRow[i] - userAVG)/userStdDeviation;
			}	
		}
		return UserRatingNormalized;
	}
	
	// prediction for user a's items
	public double[][] getUserItemPrediction(int[][] dataMat, int[] userRatingRow, int users, int items ){
		double[][] userItemPrediction = new double[2][items+1];
		double ratingWeighted = 0;
		double weightedSimilarity = 0;
		float userAverage = this.getUserRatingAVG(userRatingRow, items);
		int ratingCountedFor = 0;
				
		for(int i= 1; i<= items; i++){
			// predict if item rating is missing
			if(userRatingRow[i] == 0){
				for(int u = 1; u <= users; u ++){
					// should not compare with the same user and user should have rate for i item
					if(u != i && dataMat[u][i] != 0){
						int [] userRating = this.getUserRatingRow(dataMat, u, items);
						float userAVG = this.getUserRatingAVG(userRating, items);
						double userSimilarity = this.getUserSimilarity(userRating, userRatingRow, items, items);
						
						ratingWeighted += ((float)dataMat[u][i] - userAVG) * userSimilarity;
						weightedSimilarity += userSimilarity;
						ratingCountedFor ++;
					}
				}
				userItemPrediction[0][i] = userAverage + (ratingWeighted / weightedSimilarity);	
				userItemPrediction[1][i] = ratingCountedFor;
				ratingWeighted = 0;
				weightedSimilarity = 0;
				ratingCountedFor = 0;
			}else{
				userItemPrediction[0][i] = userRatingRow[i] ;
			}
				
		}
		return userItemPrediction;
	}
	
	// find how many item user has rated 
	public int getUserItemRateCount(int [] userRatingRow, int items){
		int countItem = 0;
		for (int i=1; i<= items; i++){
			if (userRatingRow[i] != 0){
				countItem++;
			}
		}
		return countItem;
	}
	
	// calculate standard deviation for a user
	public double getUserStdDeviation(int [] userRatingRow, int items){
		double stdDeviation = 0;
		float average = this.getUserRatingAVG(userRatingRow, items);
		for(int i=1; i<= items; i++){
			if(userRatingRow[i] != 0){
				stdDeviation =  (stdDeviation + ( Math.pow( (userRatingRow[i] - average), 2) ));
			}
		}
		return stdDeviation = Math.sqrt(stdDeviation);
	}
	
	// get similar item for user a and u 
	public int [] getUserSimilarityItemsRow(int []userARatingRow, int [] userURatingRow, int items ){
		int [] similarUserItems = new int [items+1];
		for (int i=0; i <= items; i++){
			if(userARatingRow[i] != 0 && userURatingRow[i] != 0){
				similarUserItems[i] = 1;
			}
		}
		return similarUserItems;
	}
	
	// get similar item for user a and u -> array[0] = count of similar items, rest other has item number
	public int [] getUserSimilarityItems(int []userARatingRow, int [] userURatingRow, int items ){
		int [] similarUserItems = new int[items+1];
		int index = 1;
		for (int i=0; i <= items; i++){
			if(userARatingRow[i] != 0 && userURatingRow[i] != 0){
				similarUserItems[index] = i;
				index ++;
				// store the count how many items are similar
				similarUserItems[0] += 1;
			}
		}
		return similarUserItems;
	}
	
	// calculate similarity of a user a and u for 1st m items
	public double getUserSimilarity(int []userARatingRow, int [] userURatingRow, int items, int m){
		double similarity = 0;
		double stdDeviationUserA = this.getUserStdDeviation(userARatingRow, items);
		double stdDeviationUserU = this.getUserStdDeviation(userURatingRow, items);
		float avgUserA = this.getUserRatingAVG(userARatingRow, items);
		float avgUserU = this.getUserRatingAVG(userURatingRow, items);
		for(int i = 1; i <= m; i++){		
			// only for common items
			if(userARatingRow[i] != 0 && userURatingRow[i] != 0){
				similarity = similarity + ((userARatingRow[i] - avgUserA) * (userURatingRow[i] - avgUserU));
			}
		}
		return similarity/ (stdDeviationUserA * stdDeviationUserU);
	}
	
	// find the average rating for user whose rating row is given as input
	public float getUserRatingAVG(int [] userRatingRow, int items){
		float userAVG = 0;
			for(int i = 1; i <= items; i++){
				userAVG = userAVG + userRatingRow[i];
			}			
		return userAVG/this.getUserItemRateCount(userRatingRow, items);
	}
	
	// write data mat to file
	public void writeDataMat(String fileName, int [][] dataMat, int users, int items){
		try{		
				File file = new File(fileName);
				file.createNewFile();
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				String buildLine ="";
				for (int i=0; i<= users; i++){
					for(int j=0; j <= items; j++){
						buildLine = buildLine +" "+ dataMat[i][j];
					}
					bw.write(buildLine);
					bw.write(System.getProperty("line.separator"));
					buildLine = "";
				}bw.close();
		}catch(Exception e){
			System.out.println("File Not Found " + e.getMessage());
		}
	}
	
	// get 1 d array 
	public double[] get1DArray(double[][] dataMat, int items){
		double[] myData = new double[items+1];
		for(int i=1; i<= items; i++){
			myData[i] = dataMat[0][i];
		}
		return myData;
	}

}
