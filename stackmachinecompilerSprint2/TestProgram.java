package stackmachine.compiler.sp3;


// THE GRAMMAR 
//program                ->  void main { declarations instructions }
//declarations           ->  declaration declarations | ε
//declaration            ->  type identifiers ;
//type                   ->  int | float | boolean
//identifiers            ->  id optional-declaration more-identifiers
//more-identifiers       ->  , id optional-declaration more-identifiers | ε
//optional-declaration   ->  = expression | [num] | ε
//instructions           ->  instruction instructions | ε
//instruction            ->  declaration |
//                           id assignment ;
//                           print (id) ; |
//                           { instructions }
//assignment             ->  optional-array = expression |                   
//optional-array         ->  [expression] | ε                   
//expression             ->  expression + term |
//                           expression - term |
//                           term
//term                   ->  term * factor |
//                           term / factor |
//                           term % factor |
//                           factor
//factor                 ->  (expression)      |
//                           id optional-array |
//                           num

public class TestProgram {

    public static void main(String[] args) {

        try {
                    
            IStackMachineCompiler stackMachineCompiler = new StackMachineCompiler();
            
            stackMachineCompiler.compile("program test array.txt", "sm test array.txt");
            
            System.out.println("'program test array.txt' compiled succesfully!");
            
        } catch (Exception e) {
            System.out.println(e.getMessage());            
        }    
    }

}