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

//assignment             ->  optional-array = logic-expression             DONE  

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
    private  int counter;		// this is helpful for the newLabel method
    
    public SyntaxAnalyzer(ILexicalAnalyzer lex) {
        this.scanner = lex;
        this.token = this.scanner.getToken();
        this.symbols = new HashMap<String, IDataType>();
        this.code = new ArrayList<String>();
        this.counter = 0;
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
        
        this.code.add("halt");
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
        if (this.token.getName().equals("assignment")) {
            
            match("assignment");

            // the token 'assignment' allows to assign  an initial value to a variable in the declaration

            this.code.add("push " + id.getLexeme());
                        
            logicExpression();
            
            this.code.add("store");            
        
        } else if (this.token.getName().equals("open_square_bracket")) {
            
            // the token 'open_square_bracket' declares an array of int, float or boolean
    
            match("open_square_bracket");
            
            // array of a primitive data  type: int, float, boolean
            
            int size = 1;
            
            if (this.token.getName().equals("int")) {
                IntegerNumber number = (IntegerNumber) this.token;
                
                size = number.getValue();
                
                this.code.add("array " + id.getLexeme() + " " + type + " " + size);
            }
            
            match("int");
            match("closed_square_bracket");
            
            this.symbols.put(id.getLexeme(), new ArrayType(type, size));
            
        }
    }  
   

    private void instructions() throws Exception {
    //block of instructions
    	String tokenName = this.token.getName();
    	
    	// check the token in FIRST(instructions)
    	if(tokenName.equals("int") || tokenName.equals("float") || tokenName.equals("boolean") || tokenName.equals("id") || tokenName.equals("if") ||  tokenName.equals("while") || tokenName.equals("do") || tokenName.equals("print") || tokenName.equals("open_curly_bracket")) {
    		
    		instruction();
    		instructions();
    		
    	}

    }
 
    	private void instruction() throws Exception{
    		
    	String token = this.token.getName();
    	
    	if(token.equals("int") || token.equals("float") || token.equals("boolean") ) {
    		declaration();
    	}
    	
    	else if (token.equals("id") ) {
	    	        // id assignment ;
//	    	        match("id");
	    	        assignment();
	    	        match("semicolon");

	    	        
    	    } else if (token.equals("if")) {
	    	        // if (logic-expression) instruction optional-else
	    	        match("if");
	    	        match("open_parenthesis");
	    	        logicExpression();
	    	        
	    	       
	    	        
	    	        String Else= newLabel();
	    	        this.code.add("gofalse" + Else);
	    	        
	    	        match("closed_parenthesis");
	    	        
	    	        instruction();
	    	        
	    	        String out = optionalElse(Else);
	    	        
	    	        this.code.add(out + ":");	
	    	        
	    	      
	    	        
    	    } else if (token.equals("while")) {
	    	        // while (logic-expression) instruction
	    	       
	    	        
	    	        String test = newLabel();
	    	       
	    	        this.code.add(test + ":");
	    	        match("while");
	    	        match("open_parenthesis");
	    	       
	    	        // Call logicExpression to handle the while condition
	    	        logicExpression();
	    	        
	    	        String out = newLabel();
	    	        this.code.add("gofalse" + out);
	    	        
	    	        match("closed_parenthesis");
	    	        
	    	        instruction();
	    	        
	    	        this.code.add("goto" + test);
	    	        this.code.add(out );
	    	        
    	    } else if (token.equals("do")) {
	    	        // do instruction while (logic-expression) ;
	    	        match("do");
	    	        
	    	        String test = newLabel();
	    	        this.code.add(test + ":");  
	    	        
	    	        instruction();
	    	        
	    	        
	    	        match("while");
	    	        match("open_parenthesis");
	    	        logicExpression();
	    	        
	    	        
	    	        match("closed_parenthesis");
	    	        
	    	        String out = newLabel();
	    	        this.code.add("gofalse" + out);
	    	        
	    	        
	    	        this.code.add("goto" + test);
	    	        this.code.add(out + ":");
	    	        
	    	        
	    	        
	    	       
//	    	        match("semicolon");
	    	        
    	    } else if (token.equals("print")) {
	    	        // print (expression) ;
	    	        match("print");
	    	        match("open_parenthesis");
	    	        
	    	        // Call expression to handle the print value
	    	        expression();
	    	        match("closed_parenthesis");
	    	        
	    	        match("semicolon");
	    	        this.code.add("print");
	    	        
    	    } else if (token.equals("open_curly_bracket")) {
	    	        // { instructions }
	    	        match("open_curly_bracket");
	    	        instructions();
	    	        
	    	        
	    	        match("closed_curly_bracket");
    	    } 
    
    }
    	
    	
    	 private void assignment() throws Exception {     
    		 

     	        Identifier id = (Identifier) this.token;
     	        
     	        if (this.symbols.get(id.getLexeme()) == null) {      	        		
       	            throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' is not declared");
     	        }

     	        this.code.add("push " + id.getLexeme());
     	        
     	        match("id");
     	        
     	        optionalArray(id);
     	        
     	        match("assignment");
     	        
     	        logicExpression();
     	        
     	        this.code.add("store");
    			 
    		 }
    		

    		 	

    private String optionalElse(String label) throws Exception{
    	String token = this.token.getName();
    	
    	if(token.equals("else")) {
    		String out = newLabel();
    		this.code.add("goto" + out);
    		this.code.add(out + ":");
    		
    		match("else");
    		instruction();
    		
    		this.code.add(out + ":");
    	}
    	return label;
    	
    }
    
    private void logicExpression() throws Exception {
    	logicTerm();
    	moreLogicTerms();
    	
    	
    }
    
    private void logicTerm() throws Exception {
    	logicFactor();
    	moreLogicFactors();
    	
    	
    }
    
    private void moreLogicFactors() throws Exception {
    	if(this.token.getName().equals("and")) {
    		match("and");
    		
    		logicFactor();
    		
    		this.code.add("&&");
    		
    		moreLogicFactors();
    		
    		
    	}
    }
    
    private void moreLogicTerms() throws Exception {
    	if(this.token.getName().equals("or")) {
    		match("or");
    		
    		logicTerm();
    		
    		this.code.add("||");
    		
    		moreLogicTerms();
    		
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
    		
    		this.code.add("!");
    		
    	}else if (token.equals("true")) {
    		this.code.add("push 1");
    		match("true");
    	
    	}else if (token.equals("false")) {
    		this.code.add("push 0");
    		match("false");
    	
    	}else {
    		relationalExpression();
    	}
    	
    }
    
    
  //relational-expression  ->  expression relational-operator expression |		REIMPLEMENT  
//  expression
    
    private void relationalExpression() throws Exception{
    	 expression();
    	 
    	 String tokenName = this.token.getName();
    	
    	 if (tokenName.equals("less_than") || tokenName.equals("less_equals") || tokenName.equals("greater_than") 
    	    		|| tokenName.equals("greater_equals") || tokenName.equals("equals") || tokenName.equals("not_equals")) {
    		 
    		String operator = this.scanner.getLexeme(tokenName);
    		match(tokenName);		
    	    expression();
    	     
    	    this.code.add(operator);
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
            
        }else if(this.token.getName().equals("float")){
        	
        	 RealNumber number = (RealNumber) this.token;
             
             this.code.add("push " + number.getValue());            
             
             match("float");
        	
        }
        
        else if (this.token.getName().equals("id")) {

            Identifier id = (Identifier) this.token;
            
            this.code.add("push " + id.getLexeme());
            
            match("id");
            
            optionalArray(id);

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
    
    private void optionalArray(Identifier id) throws Exception {
        if (this.token.getName().equals("open_square_bracket")) {
                
            match("open_square_bracket");
                
            expression();
                
            match("closed_square_bracket");
                
            // the operator + is used to calculate the address of the index of the array defined by expression
            // the value of expression is the offset added to the base address of the array
            
            this.code.add("+");
                
        }        
    }

    private void match(String tokenName) throws Exception {
        if (this.token.getName().equals(tokenName)) 
            this.token = this.scanner.getToken();
        else
            throw new Exception("\nError at line" + this.scanner.getLine() + ": " + this.scanner.getLexeme(tokenName) + " expected" + this.token.getName());
    }
    
	
    
    private String newLabel() { 
    	return "label" + Integer.toString(this.counter++);
    	
    }
    
    
    
    
}
