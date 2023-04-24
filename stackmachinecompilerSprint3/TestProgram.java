package stackmachine.compiler.sprint2;

public class TestProgram {

    public static void main(String[] args) {

        try {
                    
                   
            IStackMachineCompiler stackMachineCompiler = new StackMachineCompiler();
            
            stackMachineCompiler.compile("program test assignment.txt", "svm test assignment.txt");
            
            System.out.println("'program test assignment.txt' compiled succesfully!");
            
            
            // binary search.txt
            stackMachineCompiler.compile("program binary search.txt", "sm binary search.txt");
            
            System.out.println("'program binary search.txt' compiled succesfully!");
            
            // binary search boolean.txt 
            stackMachineCompiler.compile("program binary search boolean.txt", "sm binary search boolean.txt");
            
            System.out.println("'program binary search boolean.txt' compiled succesfully!");
            
            // factorial 10 array.txt
            stackMachineCompiler.compile("program factorial 10 array.txt", "sm factorial 10 array.txt");
            
            System.out.println("'program factorial 10 array.txt' compiled succesfully!");
            
            // fibonacci 20.txt
            stackMachineCompiler.compile("program fibonacci 20.txt", "sm fibonacci 20.txt");
            
            System.out.println("'program fibonacci 20.txt' compiled succesfully!");
            
            // fibonacci 20 array.txt
            stackMachineCompiler.compile("program fibonacci 20 array.txt", "sm fibonacci 20 array.txt");
            
            System.out.println("'program fibonacci 20 array.txt' compiled succesfully!");
            
            // Newton sqrt.txt
            stackMachineCompiler.compile("program Newton sqrt.txt", "sm Newton sqrt.txt");
            
            System.out.println("'program Newton sqrt.txt' compiled succesfully!");
            
            
        } catch (Exception e) {
            System.out.println(e.getMessage());            
        }    
    }

}
