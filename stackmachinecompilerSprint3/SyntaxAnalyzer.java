package stackmachine.compiler.sprint3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import slu.compiler.*;

//program                ->  void main { declarations instructions }

//declarations           ->  declaration declarations | ε

//declaration            ->  type identifiers ;

//type                   ->  int | float | boolean

//identifiers            ->  id optional-declaration more-identifiers

//more-identifiers       ->  , id optional-declaration more-identifiers | ε

//optional-declaration   ->  = logic-expression | [int] | ε

//instructions           ->  instruction instructions | ε

//instruction            ->  declaration                                        |  DONE
//                           id assignment ;                                    |
//                           if (logic-expression) instruction optional-else    |
//                           while (logic-expression) instruction               |
//                           do instruction while (logic-expression) ;          |
//                           print (expression) ;                               |
//                           { instructions }

//assignment             ->  optional-array = logic-expression             REIMPLEMENT  

//optional-array         ->  [expression]     | ε                           

//optional-else          ->  else instruction | ε               	           

//logic-expression       ->  logic-expression || logic-term |		  		DONE
//                           logic-term

//logic-term             ->  logic-term && logic-factor |		 	DONE
//                           logic-factor

//logic-factor           ->  ! logic-factor | true | false |			DONE
//                           relational-expression

//relational-expression  ->  expression relational-operator expression |		DONE  
//                           expression

//relational-operator    ->  < | <= | > | >= | == | !=			DONE 

//optional-array         ->  [expression]				

//expression             ->  expression + term |
//                           expression - term |
//                           term

//term                   ->  term * factor |
//                           term / factor |
//                           term % factor |
//                           factor

//factor                 ->  (expression)      |
//                           id optional-array |
//                           int               |
//                           float

public class SyntaxAnalyzer implements ISyntaxAnalyzer {
    private IToken token;
    private ILexicalAnalyzer scanner;
    private Map<String, IDataType> symbols;
    private List<String> code;
    
    public SyntaxAnalyzer(ILexicalAnalyzer lex) {
        this.scanner = lex;
        this.token = this.scanner.getToken();
        this.symbols = new HashMap<String, IDataType>();
        this.code = new ArrayList<String>();
    }

    public String compile() throws Exception {
        program();
        
        // stack machine code
        
        String code = "";
        
        for (String instruction : this.code)
            code = code + instruction + "\n";
        
        return code;
    }
    
    private void program() throws Exception {
        match("void");
        match("main");
        match("open_curly_bracket");
        
        // this is a new rule
        declarations();
        instructions();	// this is the body 
        
        match("closed_curly_bracket");
    }
    
    private void declarations() throws Exception {
        if (this.token.getName().equals("int") || this.token.getName().equals("float") || this.token.getName().equals("boolean")) {
            declaration();
            declarations();
        }
    }

    private void declaration() throws Exception {
        identifiers(type());
        match("semicolon");    
    }

    private String type() throws Exception {
        String type = this.token.getName();
        
        if (type.equals("int")) {
            match("int");
        } else if (type.equals("float")) {
            match("float");
        } else if (type.equals("boolean")) {
            match("boolean");
        }
        
        return type;
    }

    private void identifiers(String type) throws Exception {
        if (this.token.getName().equals("id")) {
            Identifier id = (Identifier) this.token;

            if (this.symbols.get(id.getLexeme()) == null)
                this.symbols.put(id.getLexeme(), new PrimitiveType(type));
            else
                throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' is already declared");
        
            match("id");
            
            optionalDeclaration(type, id);		// this means that if we write int a; now we can write int a = tosomthn

            moreIdentifiers(type);
        }
    }
    
    private void moreIdentifiers(String type) throws Exception {
        if (this.token.getName().equals("comma")) {
            match("comma");
            
            Identifier id = (Identifier) this.token;

            if (this.symbols.get(id.getLexeme()) == null)
                this.symbols.put(id.getLexeme(), new PrimitiveType(type));
            else
                throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' is already declared");

            match("id");

            optionalDeclaration(type, id);

            moreIdentifiers(type);
        }
    }
    
    private void optionalDeclaration(String type, Identifier id) throws Exception {
       // optional declaration: a variable may be initialized when it is declared
    	String tokenName = this.token.getName();
    	
    	if(tokenName.equals("assignment")) {
    		
    		this.code.add("push" + id.getLexeme());
    		
    		match("assignment");
    			
    		
    		expression();
    		
    		this.code.add("store");
    		
    		
    	}      
    }    
    
   

    private void instructions() throws Exception {
    //block of instructions
    	String tokenName = this.token.getName();
    	
    	// check the token in FIRST(instructions)
    	if(tokenName.equals("int") || tokenName.equals("float") || tokenName.equals("boolean") || tokenName.equals("id")){
    		
    		instruction();
    		instructions();
    		
    	}

    }
    
  //instruction            ->  declaration                                        |   
//  id assignment ;                                    |
//  if (logic-expression) instruction optional-else    |
//  while (logic-expression) instruction               |
//  do instruction while (logic-expression) ;          |
//  print (expression) ;                               |
//  { instructions }
    

    	
    	private void instruction() throws Exception{
    		
    	String token = this.token.getName();
    	    if (token.equals("id") ) {
	    	        // id assignment ;
	    	        match("id");
	    	        match("=");
	    	        
	    	        // Call expression to handle assignment value
	    	        expression();
	    	        match("semicolon");
	    	        
    	    } else if (token.equals("if")) {
	    	        // if (logic-expression) instruction optional-else
	    	        match("if");
	    	        match("open_parenthesis");
	    	        
	    	        // Call logicExpression to handle the if condition
	    	        logicExpression();
	    	        match("closed_parenthesis");
	    	        instruction();
	    	        if (token.equals("else")) {
	    	            match("else");
	    	            instruction();
	    	        }
	    	        
    	    } else if (token.equals("while")) {
	    	        // while (logic-expression) instruction
	    	        match("while");
	    	        match("open_parenthesis");
	    	        
	    	        // Call logicExpression to handle the while condition
	    	        logicExpression();
	    	        match("closed_parenthesis");
	    	        instruction();
	    	        
    	    } else if (token.equals("do")) {
	    	        // do instruction while (logic-expression) ;
	    	        match("do");
	    	        instruction();
	    	        match("while");
	    	        match("open_parenthesis");
	    	        
	    	        // Call logicExpression to handle the do-while condition
	    	        logicExpression();
	    	        match("closed_parenthesis");
	    	        match("semicolon");
	    	        
    	    } else if (token.equals("print")) {
	    	        // print (expression) ;
	    	        match("print");
	    	        match("open_parenthesis");
	    	        
	    	        // Call expression to handle the print value
	    	        expression();
	    	        match("closed_parenthesis");
	    	        match("semicolon");
	    	        
    	    } else if (token.equals("open_curly_bracket	")) {
	    	        // { instructions }
	    	        match("open_curly_bracket	");
	    	        while (token.equals("closed_curly_bracket")) {
	    	            instruction();
	    	        }
	    	        
	    	        match("closed_curly_bracket");
    	    } else {
	    	        // declaration
	    	        declaration();
    	    }
    
    }
    	
    	
    	private void assignment() throws Exception {
    	    optionalArray();
    	    if (this.token.getName().equals("=")) {
    	        match("=");
    	        logicExpression();
    	    } else {
    	        throw new Exception("Syntax error: expecting '=' after optional array");
    	    }
    	}
    
    private void optionalElse() throws Exception{
    	String token = this.token.getName();
    	
    	if(token.equals("else")) {
    		match("else");
    		instruction();
    	}
    	
    }
    
    private void logicExpression() throws Exception {
    	String token = this.token.getName();
    	// parse the first logic term 
    	logicTerm();
    	
    	// check for additional || logic terms 
    	while(token.equals("or")) {
    		match("or");
    		logicTerm();
    	}
    	
    }
    
    private void logicTerm() throws Exception {
    	String token = this.token.getName();
    	
    	// parse the first logic factor
    	logicFactor();
    	
    	// check for additional AND logic factors
    	while(token.equals("and")) {
    		match("and");
    		logicFactor();
    	}
    	
    }
    
    
  //logic-factor           ->  ! logic-factor | true | false |			REIMPLEMENT  
//  relational-expression
    private void logicFactor() throws Exception {
    	String token = this.token.getName();
    	
    	if(token.equals("not")) {
    		// ! logicFactor
    		match("not");
    		logicFactor();
    		
    	}else if (token.equals("true")) {
    		match("true");
    	
    	}else if (token.equals("false")) {
    		match("false");
    	
    	}else {
    		relationalExpression();
    	}
    	
    }
    
    
  //relational-expression  ->  expression relational-operator expression |		REIMPLEMENT  
//  expression
    
    private void relationalExpression() throws Exception{
    	 expression();
    	    if (isRelationalOperator()) {
    	        relationalOperator();
    	        expression();
    	    }
    }
 
    // extra function to help with relationalExpression
    private boolean isRelationalOperator() {
        String token = this.token.getName();
        return (token.equals("<") || token.equals("<=") ||
                token.equals(">") || token.equals(">=") ||
                token.equals("==") || token.equals("!="));
    }
    
    
//    relational-operator    ->  < | <= | > | >= | == | !=
    private  void relationalOperator() throws Exception {
        String token = this.token.getName();
        if (token.equals("<") || token.equals("<=") ||
            token.equals(">") || token.equals(">=") ||
            token.equals("==") || token.equals("!=")) {
            // match the relational operator token and move to the next token
            match(token);
        } else {
            // throw an exception if the current token is not a relational operator
            throw new Exception("Expected a relational operator! ");
        }
    }
    
   


    private void expression() throws Exception {
        term(); moreTerms();
    }
        
    private void term() throws Exception {
        factor(); moreFactors();
    }
        
    private void moreTerms() throws Exception {
        if (this.token.getName().equals("add")) {
            
            match("add");
            
            term();
                        
            this.code.add("+");
            
            moreTerms();

        } else if (this.token.getName().equals("subtract")) {
            
            match("subtract");
            
            term();

            this.code.add("-");
            
            moreTerms();
            
        } 
    }

    private void factor() throws Exception {        
        if (this.token.getName().equals("open_parenthesis")) {
            
            match("open_parenthesis");
            
            expression();
            
            match("closed_parenthesis");
        
        } else if (this.token.getName().equals("int")) {
                
            IntegerNumber number = (IntegerNumber) this.token;
                            
            this.code.add("push " + number.getValue());            
            
            match("int");
            
        } else if (this.token.getName().equals("id")) {

            Identifier id = (Identifier) this.token;
            
            this.code.add("push " + id.getLexeme());
            
            optionalArray();

            this.code.add("load");
            
        } else {
            
            throw new Exception("\nError at line " + this.scanner.getLine() + ": invalid arithmetic expression: open parenthesis, int or identifier expected");
        
        }
    }

    private void moreFactors() throws Exception {
        if (this.token.getName().equals("multiply")) {
            
            match("multiply");
            
            factor();        
                        
            this.code.add("*");
            
            moreFactors();
        
        } else if (this.token.getName().equals("divide")) {
            
            match("divide");
            
            factor();
                        
            this.code.add("/");

            moreFactors();
        
        } else if (this.token.getName().equals("remainder")) {
            
            match("remainder");
            
            factor();
                        
            this.code.add("%");            

            moreFactors();
            
        }        
    }
    
    private void optionalArray() throws Exception {
        if (this.token.getName().equals("id")) {        
            
            Identifier id = (Identifier) this.token;
            
            if (this.symbols.get(id.getLexeme()) == null) {                
                throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' is not declared");
            }
            
            match("id");
            
            if (this.token.getName().equals("open_square_bracket")) {
                
                match("open_square_bracket");
                
                expression();
                
                match("closed_square_bracket");
                
                this.code.add("+");
                
            } 
            
        } else {            
            
            throw new Exception("\nError at line " + this.scanner.getLine() + ": a variable is expected");
            
        }        
    }

    private void match(String tokenName) throws Exception {
        if (this.token.getName().equals(tokenName)) 
            this.token = this.scanner.getToken();
        else
            throw new Exception("\nError at line " + this.scanner.getLine() + ": " + this.scanner.getLexeme(tokenName) + " expected");
    }
    
    
//    // relational-operator    ->  < | <= | > | >= | == | !=
//    
//    private void relationalOperator() throws Exception{
//    	String tokenName = this.token.getName();
//    	if(tokenName.equals("<")) {
//    		match("less_than");
//    		
//    		this.code.add("<");
//    	}
//    	else if(tokenName.equals("<=")) {
//    		match("less_equals");
//    		
//    		this.code.add("<=");
//    		
//    	}else if(tokenName.equals(">")) {
//    		match("greater_than");
//    		
//    		this.code.add(">");
//    	}
//    	else if(tokenName.equals(">=")) {
//    		match("greater_equals");
//    		
//    		this.code.add(">=");
//    	}
//    	else if(tokenName.equals("==")) {
//    		match("equals");
//    		
//    		this.code.add("==");
//    	}
//    	else if(tokenName.equals("!=")) {
//    		match("not_equals");
//    		
//    		this.code.add("!=");
//    	}
//    	else {
//    		throw new Exception("Expected relational Operator!");
//    	}
//    }
    
    
    
    
    
}
