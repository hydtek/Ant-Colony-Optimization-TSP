package ACO;
/*********************************************************************%
 % The class Ant is an object wrapper class to represent an ant       %
 % and all the cities the ant has visitied within the tour, this      %
 % class allows return of each city in the ant tour                   %
 %                                                                    %
 % Stephen Hyde - 3603453                                             %
 % Cosc 3P71                                                          %
 % Assignment 3                                                       %
 **********************************************************************/
public class Ant {
    private int[] city; //each city in ant tour
    private int counter = 0; //counter for next available spot
    public Ant(){
        city = new int[52]; //sets default value to 52
    }
    public Ant(int numCity){
        
        city = new int[numCity]; //sets size of array to number of cities
        //initializes empty array with -1 to distinguish end or next available
        for(int i = 0; i < numCity;i++){
            city[i] = -1;
        }
    }
    //inputs city into next available spot in the array
    public void nextCity(int c){
        city[counter] = c;
        counter++;
    }
    //resets the count to default and delets the array of previous values
    public void setCounter(int c){
        for(int i = 0; i < city.length; i++)
            city[i] = -1;
        
        counter = c;
    }
    //returns value for any city in array
    public int getCity(int i){
        return city[i];
    }
}
