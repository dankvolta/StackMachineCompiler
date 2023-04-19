package test;

import slu.stackmachine.*;

public class TestProgram {

    public static void main(String[] args) {

        try {
                        
            IStackMachine stackMachine = new StackMachine();

            System.out.print("sm srqt(2) using Newton is ");
            
            stackMachine.run("sm newton sqrt 2.txt");           
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
