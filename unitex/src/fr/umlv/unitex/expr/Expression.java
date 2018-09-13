package fr.umlv.unitex.expr;

import java.util.ArrayList;
import java.util.List;

public interface Expression {

	
	List<Expression> getOperands();
	
	int getOperandsSize();
	
	
}
