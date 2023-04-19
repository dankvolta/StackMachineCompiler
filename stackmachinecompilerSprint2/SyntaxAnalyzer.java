package stackmachine.compiler.sp3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import slu.compiler.*;

/* 
 *  Syntax-directed definition for data type declaration
 *  
 *     program              ->  void main { declarations instructions }
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
 *                              [num]  
 *                              { print ("array " + id.lexeme + " " + type + " " + num.value) }
 *                              { addSymbol(id.lexeme, array(type, num.value)) }               |                                              |
 *                              epsilon                     
 *  
 *  Syntax-directed definition to translate infix arithmetic expressions into stack machine code
 *
 *     instructions         ->  instruction instructions |
 *                              epsilon
 *                              
 *     instruction          ->  declaration                             |
 *                              id assignment ;                         |
 *                              print (expression) { print("print") } ;
 *                              
 *     assignment           ->  optional-array = { print("push " + id.lexeme) } expression { print("store") }
 *     
 *     optional-array       ->  [expression] { print("+") }    | 
 *                              epsilon
 *     
 *     expression           ->  expression + term { print("+") } |
 *                              expression - term { print("-") } |
 *                              term
 *                  
 *     term                 ->  term * factor { print("*") } |
 *                              term / factor { print("/") } |
 *                              term % factor { print("%") } |
 *                              factor
 *                  
 *     factor               ->  (expression)                                                     |
 *                              id  optional-array { print("push " + id.lexeme); print("load") } |
 *                              num { print("push " + num.value) }
 *                  
 *  Right-recursive SDD for a top-down recursive predictive parser
 *
 *     instructions         ->  instruction instructions |
 *                              epsilon
 *                              
 *     instruction          ->  declaration |
 *                              assignment ;
 *                              
 *     assignment           ->  id = { print("push " + id.lexeme) } expression { print("store") }
 *
 *     expression           ->  term moreTerms
 *     
 *     moreTerms            ->  + term { print("+") } moreTerms |
 *                              - term { print("-") } moreTerms |
 *                              epsilon
 *               
 *     term                 ->  factor moreFactors
 *     
 *     moreFactors          ->  * factor { print("*") } moreFactors |
 *                              / factor { print("/") } moreFactors |
 *                              % factor { print("%") } moreFactors |
 *                              epsilon
 *                    
 *     factor               ->  (expression)                                                     |
 *                              id  optional-array { print("push " + id.lexeme); print("load") } |
 *                              num { print("push " + num.value) }
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
        
        declarations();
        instructions();
        
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
            
            optionalDeclaration(type, id);

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
                        
            expression();
            
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
        String tokenName = this.token.getName();
        
        // the method for the nonterminal symbol instructions is called if the lookahead is in FIRST of instruction
        
        if (tokenName.equals("int") || tokenName.equals("float") || tokenName.equals("boolean") || tokenName.equals("id") || tokenName.equals("print")) {            
            instruction();
            instructions();            
        }
    }
    
    private void instruction() throws Exception {
        String tokenName = this.token.getName();
        
        if (tokenName.equals("int") || tokenName.equals("float") || tokenName.equals("boolean")) {
            
            declaration();
            
        } else if (tokenName.equals("id")) {

            assignment();
            match("semicolon");
            
        } else if (tokenName.equals("print")) {
            
            match("print");
            match("open_parenthesis");
            
            expression();

            this.code.add("print");
            
            match("closed_parenthesis");
            match("semicolon");
            
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
        
        expression();
        
        this.code.add("store");
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
            
            if (this.symbols.get(id.getLexeme()) == null) {                
                throw new Exception("\nError at line " + this.scanner.getLine() + ": identifier '" + id.getLexeme() + "' is not declared");
            }
            
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
    
    private void match(String tokenName) throws Exception {
        if (this.token.getName().equals(tokenName)) 
            this.token = this.scanner.getToken();
        else
            throw new Exception("\nError at line " + this.scanner.getLine() + ": " + this.scanner.getLexeme(tokenName) + " expected");
    }
}