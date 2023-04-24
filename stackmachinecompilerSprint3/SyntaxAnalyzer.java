package stackmachine.compiler.sprint2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import slu.compiler.*;

/* 
 *  Syntax-directed definition for data type declaration
 *  
 *     program              ->  void main { declarations  instructions}
 *
 *     declarations         ->  declaration declarations  |
 *                              epsilon
 *                                     
 *     declaration          ->  type { identifiers.type = type.value } identifiers ;
 *     
 *     type                 ->  int     { type.value = "int"     } |
 *                              float   { type.value = "float"   } |
 *                              boolean { type.value = "boolean" }                                   
 *                  
 *     identifiers          ->  id 
 *                              { addSymbol(id.lexeme, identifiers.type); optional-declaration.id = identifiers.id; more-identifiers.type = identifiers.type }
 *                              optional-declaration
 *                              more-identifiers
 *                                      
 *     more-identifiers     ->  , id 
 *                              { addSymbol(id.lexeme, identifiers.type); optional-declaration.id = identifiers.id; more-identifiers.type = identifiers.type }
 *                              optional-declaration
 *                              more-identifiers |
 *                              epsilon
 *                           
 *     optional-declaration ->  = { print("push " + id.lexeme) } expression { print("store") } |
 *                              epsilon                     
 *  
 *  Syntax-directed definition to translate infix arithmetic expressions into a stack machine code
 *
 *     instructions         ->  instruction instructions |
 *                              epsilon
 *                              
 *     instruction          ->  declaration |                   //IMPLEMENT
 *                              assignment ;
 *                              
 *     assignment           -> id { print("push " + id.lexeme) } expression { print("store") }
 *     
 *     expression           -> expression + term { print("+") } |
 *                             expression - term { print("-") }
 *                             term
 *                  
 *     term                 -> term * factor { print("*") } |
 *                             term / factor { print("/") } |
 *                             term % factor { print("%") } |
 *                             factor
 *                  
 *     factor               -> (expression) |
 *                              id  { print("push " + id.lexeme); } |
 *                              num { print("push " + num.value) }
 *                  
 *  Right-recursive SDD for a top-down recursive predictive parser
 *
 *     instruction          -> id { print("push " + id.lexeme) } expression { print("store") }
 *
 *     expression           -> term moreTerms
 *     
 *     moreTerms            -> + term { print("+") } moreTerms |
 *                             - term { print("-") } moreTerms |
 *                             epsilon
 *               
 *     term                 -> factor moreFactors
 *     
 *     moreFactors          -> * factor { print("*") } moreFactors |
 *                             / factor { print("/") } moreFactors |
 *                             % factor { print("%") } moreFactors |
 *                             epsilon
 *                    
 *     factor               -> (expression) |
 *                             id  { print("push " + id.lexeme); print("load") } |
 *                             num { print("push " + num.value) }
 *  
 */

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
        Identifier id = (Identifier) this.token;
        
        this.code.add("push " + id.getLexeme());
        
        match("assignment");
        
        expression();
        
        this.code.add("store");
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
}
