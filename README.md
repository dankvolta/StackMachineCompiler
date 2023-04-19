# StackMachineCompiler

Implement a predictive, recursive parser for a subset of C-based language instructions. The parser should generate code for a stack machine. The parser should meet the following requirements:

    The parser should use the library LexicalAnalyzer. (it is a .jar file)
    The parser should verify that the input program complies the language grammar.
    The parser should show the outcome of the compilation process.
    The parser should check that all variables are declared.
    The parser should generate the code for the stack machine.
    The code generated by the compiler should run on the virtual stack machine.

# Project planning

Implement this project in 3 sprints. 
Sprint 1 --> implements the grammar rules for declarations and arithmetic expressions. 
Sprint 2 --> implements declaration of arrays of primitive data types. 
Sprint 3 --> implements the grammar rules for logic expressions, conditionals, and iterative instructions.

# THE GRAMMAR: 
// sprint 1 grammar:

program                ->  void main { declarations instructions }
declarations           ->  declaration declarations | ε
declaration            ->  type identifiers ;
type                   ->  int | float | boolean
identifiers            ->  id optional-declaration more-identifiers
more-identifiers       ->  , id optional-declaration more-identifiers | ε
optional-declaration   ->  = expression | ε
instructions           ->  instruction instructions | ε
instruction            ->  declaration       |
                           id assignment ; 
assignment             ->  = expression                           
expression             ->  expression + term |
                           expression - term |
                           term
term                   ->  term * factor |
                           term / factor |
                           term % factor |
                           factor
factor                 ->  (expression)  |
                           id            |
                           num
                           
                           
                           
                           
                           
                           
// sprint 2 grammar: 
program                ->  void main { declarations instructions }
declarations           ->  declaration declarations | ε
declaration            ->  type identifiers ;
type                   ->  int | float | boolean
identifiers            ->  id optional-declaration more-identifiers
more-identifiers       ->  , id optional-declaration more-identifiers | ε
optional-declaration   ->  = expression | [num] | ε
instructions           ->  instruction instructions | ε
instruction            ->  declaration          |
                           id assignment ;      |
                           print (expression) ; |
                           { instructions }
assignment             ->  optional-array = expression                   
optional-array         ->  [expression] | ε                   
expression             ->  expression + term |
                           expression - term |
                           term
term                   ->  term * factor |
                           term / factor |
                           term % factor |
                           factor
factor                 ->  (expression)      |
                           id optional-array |
                           num
                           
                           
                           
                           
// sprint 3 grammar: 
program                ->  void main { declarations instructions }
declarations           ->  declaration declarations | ε
declaration            ->  type identifiers ;
type                   ->  int | float | boolean
identifiers            ->  id optional-declaration more-identifiers
more-identifiers       ->  , id optional-declaration more-identifiers | ε
optional-declaration   ->  = logic-expression | [num] | ε
instructions           ->  instruction instructions | ε
instruction            ->  declaration                                        |
                           id assignment ;                                    |
                           if (logic-expression) instruction                  |
                           if (logic-expression) instruction else instruction |
                           while (logic-expression) instruction               |
                           do instruction while (logic-expression) ;          |
                           print (expression) ;                               |
                           { instructions }
assignment             ->  optional-array = logic-expression |                   
optional-array         ->  [expression]    | ε                       
logic-expression       ->  logic-expression || logic-term |
                           logic-term
logic-term             ->  logic-term && logic-factor |
                           logic-factor
logic-factor           ->  ! logic-factor | true | false |
                           relational-expression
relational-expression  ->  expression relational-operator expression |
                           expression
relational-operator    ->  < | <= | > | >= | == | !=
optional-array         ->  [expression]
expression             ->  expression + term |
                           expression - term |
                           term
term                   ->  term * factor |
                           term / factor |
                           term % factor |
                           factor
factor                 ->  (expression)      |
                           id optional-array |
                           num

                           
                           
                           
                           
                           
                           
                           
                           
                           
                           
                           
                           
