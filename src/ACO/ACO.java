package ACO;

import java.io.*;
/*********************************************************************%
 % The class ACO is a metaheuristic designed to represent the         %
 % ant colony optimization system. This swarm intelligence runs       %
 % through the "Travelling Salesman Problem" search space to return   %
 % a tour with a low distance cost. This ant system apply a global    %
 % update, and an evaporation with each ant iteration.                %
 %                                                                    %
 % Stephen Hyde - 3603453                                             %
 % Cosc 3P71                                                          %
 % Assignment 3                                                       %
 **********************************************************************/

public class ACO {
    private File theFile;         //imported file from user
    private Ant[] ants;           //population of ants
    private int popAnts = 0;      //amount of ants
    private int numCities = 0;    //number of cities
    private int[] fileX;          //x values of cities in file 
    private int[] fileY;          //y values of cities in file
    private double[][] pheromone; //board to represent all edges for pheromone
    private double beta = 0;      //beta used for transition probability
    private double alpha = 0;     //alpha used for transition probability
    private int progLength = 0;   //how many colonies are created 
    private double[] bestScores;  //best score of each colony 
    private double[] bestAverages;//best average of each colony
    private double evapRate = 0;  //the rate of evaporation taking place
    
    /*ACO is a method that runs a colony of ants through the search space
     * of the tsp problem numerous times which is set by the user.*/
    public ACO(){
       
        importTSP(); //import tsp file 
        createPop(); //create population of ants
        initialPheromone(); //set the initial pheromone of each edge
    
        //runs the ants through passes which user tells it when to quit
        for(int s = 0; s < progLength; s++){
        runPass(); //runs ants through the tour
    
        System.out.println("");
        System.out.println("---------------------");
        System.out.println("");
        System.out.println("Running Ant colony number : " + s);
        //prints out the tour of each ant
        for(int j = 0; j<numCities;j++){
            System.out.println("Ant "  + j);
            for(int i = 0; i < numCities; i++){
               System.out.print(ants[j].getCity(i) + " ");
            }
    
            //prints the distance of the ant
            System.out.println();
            System.out.println(score(ants[j]));
            System.out.println();
        
         }
        //calculates lowest distance and best average for colony
        bestScores[s] = bestScore(ants);
        bestAverages[s] = averageScore(ants);
        //updates the pheromone for best ant in colony
        globalUpdate();
        //resets the ants to default
        for(int t = 0; t < numCities; t++)
            ants[t].setCounter(0);
        }
        //this prints a final summary of results after the tours are done
        finalReport(bestScores,bestAverages);
    }
    
    /*creates initial population of ants*/
      public void createPop(){
        Ant ant; 
        int i = 0;
        popAnts = numCities;
        ants = new Ant[popAnts]; //creates array of ants size = number of cities
        
        //loop to create instance of each ant
        while(i < popAnts){
           ant = new Ant(popAnts);
           ants[i] = ant;
           i++;
       }
    }
    
    //runPass runs 52 ants through 52 cities
    public void runPass(){
      
       int bestChoice = 0;  //best choice to add next city to tour
        
        //array to run all ants through all cities
        for(int j = 0; j< numCities;j++)
            for(int i = 0; i < popAnts; i++){
                //if ant has no cities in tour, add default starting position
                if(j == 0)
                    //adds city to next spot in tour
                    ants[i].nextCity(i);
                
                //finds best choice to add city to graph
                else{
                    bestChoice = antIteration(ants[i], j);
                    //adds city to next spot in tour
                    ants[i].nextCity(bestChoice);
                    //applys evaporation to edge visited
                    evaporation(HeuristicDist(fileX[ants[i].getCity(j-1)],fileX[ants[i].getCity(j)],fileY[ants[i].getCity(j-1)],fileY[ants[i].getCity(j)]),ants[i].getCity(j-1),ants[i].getCity(j));
                   }
            }
     }
    
    /*this method runs through all possibilities and based on the transition 
     probability formula it returns the best city to connect to*/
     public int antIteration(Ant a, int pos){
         int bestChoice = 0;   //best city to connect previous city to
         double distance = 0;  //distance of each edge
         double probTranTop[] = new double[numCities]; //array for top formula
         double probTrans[] = new double[numCities]; //sum of each option
         double probTranBot = 0;  //value to represent the bottom of formula
            
         //loop to run through each city
            for(int i = 0; i < numCities; i++){
                //checks if city already exist in ant tour
                if(cityContained(a,i) == false){
                    //finds distance then finds the top of part of formula
                   distance =  Math.pow((1/(HeuristicDist(fileX[a.getCity(pos-1)],fileX[i],fileY[a.getCity(pos-1)],fileY[i]))), beta);
                   probTranTop[i] = distance * (Math.pow(pheromone[a.getCity(pos-1)][i], alpha));
                  
                }
                //if city is contained add -1 to array to mark not to visit
                else
                   probTranTop[i] = -1;
             
          
         //array to calculate bottom of the formula
         for(int k = 0; k < numCities;k++){
             if(probTranTop[k] != -1)
                probTranBot = probTranBot + probTranTop[k];
         }
         //array to find the transition probability of each edge
         for(int l = 0; l < numCities;l++){
             if(probTranTop[l] != -1){
                 probTrans[l] = probTranTop[l] / probTranBot;
             }
             //if city is already contained in tour add -1 to not affect calc
             else
                 probTrans[l] = -1;
         }
         
       //finds the first transition probability for city that is not contained
         int s = 0;
         while(s < numCities){
             if(probTrans[s] != -1){
                 bestChoice = s;
                 break;  //finds first possibly choice and breaks
             }
             s++;
         }
         
         //finds the best transition probability of all non contained cities
            for(int t = (bestChoice+1); t < numCities; t++){
               if(probTrans[t] != -1){ 
                  if(probTrans[t] > probTrans[bestChoice]){
                      bestChoice = t;
                     }  
               }
            }  
         }
         return bestChoice; //returns highest transition probability 
        
     }
     
     //sets the pheromone of all edges to user inputed value
     public void initialPheromone(){
        
        DataInputStream in = new DataInputStream(System.in); 
        String input = ""; 
        double iniPher = 0; //value for user input for pheromone
        //ask user for initial pheromone value
        System.out.println("Plz input the initial pheromone for the paths between cities, between 0 and 1");
        System.out.println();
        try {						 			
               input = in.readLine();
           }
   	   catch (Exception e) {
                System.out.println("Invalid Input :");
            }
        
        iniPher = Double.parseDouble(input);
        //loop to add initial pheromone to each edge
         for(int i = 0; i < numCities;i++)
             for(int j =0; j < numCities; j++){
                pheromone[i][j] = iniPher;
             }
     }
     
     //checks if city already exist within the ant population
     public boolean cityContained(Ant a, int c){
         //compares city to rest of array and returns false if is not found
         for(int i = 0; i < numCities;i++){
             if(a.getCity(i) == c){
                 return true;
             }
         }   
         return false;
     }
     
     //calculates distance of and edge
     public double HeuristicDist(int x1, int x2, int y1, int y2){
        double score = 0; //score of an edge
        //formulate to find distance of two cities
        score = (int)(Math.sqrt(Math.pow((x1-x2),2) + (Math.pow((y1-y2),2))));
         return score;
         
     }
     
     //finds the score of an entire ant
     public double score(Ant c){
        double score = 0; //final distance of ant
        int x1 = 0; 
        int x2 = 0;
        int y1 = 0;
        int y2 = 0;
        
        //array to sum all distances using euclidean distance
        for(int i = 0; i<numCities;i++){
           //if i is at last spot of array connect it back to the first position
           if(i == (numCities-1)){
              x1 = fileX[c.getCity(i)];
              x2 = fileX[c.getCity(0)];
              y1 = fileY[c.getCity(i)];
              y2 = fileY[c.getCity(0)];   
           }
           //calculate distance of i, i+1
           else{
              x1 = fileX[c.getCity(i)];
              x2 = fileX[c.getCity(i+1)];
              y1 = fileY[c.getCity(i)];
              y2 = fileY[c.getCity(i+1)];
           }
           //formula to add new distance of edge to previous score
           score = score + (int)(Math.sqrt(Math.pow((x1-x2),2) + (Math.pow((y1-y2),2)))); 
        }
        return score;
    }
    
     /*Global Update updates the pheromone at every edge of the tour within the
       ant with the lowest score*/
     public void globalUpdate(){
        
         double[] score = new double[popAnts];   //array for colony scores
         int bestAnt = 0; //int to represent best ant
         
         //finds the scores of each ant 
         for(int i = 0; i < numCities; i++){
             score[i] = score(ants[i]);
         }
         
         //finds the ant with the lowest score
         for(int j = 1; j < numCities; j++){
             if(score[j] < score[bestAnt]){
                 bestAnt = j;
             }
         }
         
        /*formula to apply pheromone on each edge of the ant with the lowest
       score. the value is multiplied to previous value to keep it upper bound*/
         for(int k = 0; k < (numCities-1);k++){
             //updates twice for both ways of the array for one edge (j,k),(k,j)
             pheromone[ants[bestAnt].getCity(k)][ants[bestAnt].getCity(k+1)] = (((1-evapRate)*(pheromone[ants[bestAnt].getCity(k+1)][ants[bestAnt].getCity(k)]) + (1/score(ants[bestAnt])))) * (pheromone[ants[bestAnt].getCity(k+1)][ants[bestAnt].getCity(k)]);
             pheromone[ants[bestAnt].getCity(k+1)][ants[bestAnt].getCity(k)] = (((1-evapRate)*(pheromone[ants[bestAnt].getCity(k+1)][ants[bestAnt].getCity(k)]) + (1/score(ants[bestAnt])))) * (pheromone[ants[bestAnt].getCity(k+1)][ants[bestAnt].getCity(k)]);
         }
      }
     
     //this calculates the best score in the colony
     public double bestScore(Ant[] a){
       
        double best = 0; //best score for an ant in the colony
        int pos = 0;     //what position the best score occured
        
        best = score(a[0]); //initializing the first ant to be best value
        
        //loop to compare through array and find the ant with lowest score.
        for(int i = 1; i < a.length; i++){
            if(best > score(a[i])){
                best = score(a[i]);
                pos = i;
            }
            
        }
        System.out.println("Best Score was at ant " + pos + " " + " Score: " + best);
        
        return best;
     }
     
     //this method calculates the average score of a colony
     public double averageScore(Ant[] a){
       
        double average = 0; //best average for the ant within the colony
      
        //loop to find best average
        for(int i = 0; i < a.length; i++){
                average = average + score(a[i]);
            }
        //finds the average by taking the sum and dividing it by n
        average = average/a.length;
        System.out.println("average Score of the ant colony is : " + average);
        
        return average;
     }
     
     //this applies evaporation to a certain edge in the pheromone array
     public double evaporation(double edge, int x, int y){
         double evapo = 0; //double to represent evaporation takin place
         
         //this formula finds the evaporation needed to take place
         evapo = ((1-evapRate) * pheromone[x][y]) + (1/edge);
         
        /*evaporation is devided by previous pheromone intensity.
         dividing it keeps it from decreasing beyond lower bound(zero)*/
         pheromone[x][y] = evapo / pheromone[x][y];
         pheromone[y][x] = evapo / pheromone[y][x];
        
       return evapo; 
     }
     
     /*finalReport gives a overall summary of all runs through the 
       tsp search space, top score, top average*/
     public void finalReport(double[] s, double[] a){
         
         int bestScore = 0;   //int to represent colony with best score
         int bestAverage = 0; //int to represent colony with best average
         System.out.println("---------------------");
         System.out.println();
   
         //loop to find best score
         for(int i = 1; i < s.length; i++){
             if(s[bestScore] > s[i])
                 bestScore = i;
         }
          
         //loop to find best average
         for(int j = 1; j < s.length; j++){
             if(a[bestAverage] > s[j])
                 bestAverage = j;
         }
         //output results
         System.out.println("The ant Colony ran through the search space " + s.length + " times");
         System.out.println("Top score from all runs was Colony : " + (bestScore) + " Score : " + s[bestScore]);
         System.out.println("Top Average from all runs was Colony : " + (bestAverage) + " Score : " + a[bestAverage]);
         System.out.println("Parameters : Alpha = " + alpha + " Beta = " + beta + " Evaporation rate " + evapRate);
     }
     
     /*this method reads in the tsp file and then sorts the coordinates
     into arrays. Also asking user for constant values like alpha, beta etc*/
    public void importTSP(){
        
        String sub = "";
        String sub2 = "";
        String city = "" ;
        String line = "";
        char temp = ' ';
        String compare = "";
        int i = 1;
        int conver = 0;
        int j = 2;
        int k = 3;
        String input = "";    //user read in value
        
        FileInputStream inp = null;
        BufferedInputStream buff = null;
        DataInputStream data = null;
        DataInputStream in = new DataInputStream(System.in); 
        
        System.out.println("Plz input the path of the TSP text file. c:/berlin52.tsp");
        System.out.println();
        try {						 			
               input = in.readLine();
           }
   	   catch (Exception e) {
                System.out.println("Invalid Input :");
            } 
        theFile = new File(input);
        
      try {
      inp = new FileInputStream(theFile);
      buff = new BufferedInputStream(inp);
      data = new DataInputStream(buff);
      
      System.out.println("Plz input the number of cities to do the tsp problem with");
      System.out.println();
              try {						 			
                   input = in.readLine();
                   }
   	      catch (Exception e) {
                  System.out.println("Invalid Input :");
                  } 
       
       numCities = Integer.parseInt(input);  //sets numCities to user value
       pheromone = new double[numCities][numCities]; //creates pheromone array
       fileX = new int[numCities]; //sets size of x coordinates array
       fileY = new int[numCities]; //sets size of y coordinates array
       
      //reads in till data is no longer available from file
      while (data.available() != 0 && i < (numCities+1)) {
          if(i < 10)
             city = i + " ";
          else
             city = i + "";
          
          line = data.readLine();
          if(!line.equalsIgnoreCase("")){
            compare = line.substring(0, 2);
          }
          
          if(city.equalsIgnoreCase(compare) == true){
              sub = "";
              sub2 = "";
              while(true){
                  
                  if(i<10){
                      if(line.charAt(j) == '.') {
                          j = j+3;
                          while (true){
                              if(line.charAt(j) == '.') 
                                  break;
                               else
                                 sub2 = sub2 + line.charAt(j);
                               j++;
                           }
                           break;
                      }
                      else
                         sub = sub + line.charAt(j);
                      j++;
                  }
                  else {
                      
                       if(line.charAt(k) == '.'){
                          k = k+3;
                          while (true){
                              if(line.charAt(k) == '.') 
                                  break;
                               else
                                 sub2 = sub2 + line.charAt(k);
                               k++;
                           }
                           break;
                        }
                      else
                         sub = sub + line.charAt(k);
                      k++;
                  }
              }    
             
              
              // System.out.println(city + " " + sub + " " + sub2);   
              int aInt = Integer.parseInt(sub);
              int aInt2 = Integer.parseInt(sub2);
              fileX[i-1] = aInt;
              fileY[i-1] = aInt2;
              System.out.println(fileX[i-1] + " " + fileY[i-1] +  " city" + " " + i);
              k = 3;
              j = 2;
              i++;
          }
      }  
        System.out.println("Please enter the amount of tours you want the ant colony to perform");
        System.out.println();
         try {						 			
               input = in.readLine();
             }
         catch (Exception e) {
                System.out.println("Invalid Input :");
             }
        
        progLength = Integer.parseInt(input);
        bestScores = new double[progLength];
        bestAverages = new double[progLength];
        
        System.out.println("Enter the alpha to use on this pass, between 0 and 1; RECOMMENDED: .7 (For best results)");
        System.out.println();
        try {						 			
               input = in.readLine();
           }
   	   catch (Exception e) {
                System.out.println("Invalid Input :");
            }
        alpha = Double.parseDouble(input);
        
        System.out.println("Enter the beta to use on this pass, between 0 and 1; RECOMMENDED: .3 (For best results)");
        System.out.println();
        try {						 			
               input = in.readLine();
            }
        catch (Exception e) {
                System.out.println("Invalid Input :");
            }
       beta = Double.parseDouble(input);
       
       System.out.println("Enter the Evaporation Rate between 0 and 1; RECOMMENDED: .1 (For best results)");
       System.out.println();
        try {						 			
               input = in.readLine();
            }
        catch (Exception e) {
                System.out.println("Invalid Input :");
            }
       evapRate = Double.parseDouble(input);
 
      inp.close();
      buff.close();
      data.close();

    } 
     catch (FileNotFoundException e) {
      e.printStackTrace();
    }catch (IOException e) {
      e.printStackTrace();
    }
   }
    
    public static void main(String[] args) {new ACO();
        // TODO code application logic here
    }

}
