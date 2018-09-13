package fr.umlv.unitex.expr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public abstract class Operator implements Expression {
	
	private final ArrayList<Expression> operands = new ArrayList<>();
	
	
	
	public void addOperand(Expression expr) {
		operands.add(expr);
	}
	
	public void removeOperand(int index) {
		operands.remove(index);
	}
	
	
	/*
	public void addOperand(Expression expr, int start, int end) {
		List<Expression> list = expr.getOperands();
		for (int i = start; i < end; i++) {
			this.addOperand(list.get(i));
		}
	}
	*/
	
	protected abstract String getOpSymbol();
	protected abstract String getOpeningSymbol();
	protected abstract String getClosingSymbol();	
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(this.getOpeningSymbol());
		
		/*
		if (operands.size() == 0) {
			if (this instanceof Concatenation) {
				System.out.println("CONCAT");
			}
			else {
				System.out.println("PARA");
			}
			return "XXX";
		}
		*/
		
		int i;
		for (i = 0; i < operands.size() - 1; i++) {
			sb.append(operands.get(i)).append(this.getOpSymbol());
		}
		
		
		//sb.append(operands.stream().map(op -> op.toString()).collect(Collectors.joining(this.getOpSymbol())));
		sb.append(operands.get(i)).append(this.getClosingSymbol());
			
		return sb.toString();
	}
	
	/*
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (operands.size() > 1) {
			sb.append(this.getOpeningSymbol());
		}
		
		int i;
		for (i = 0; i < operands.size() - 1; i++) {
			sb.append(operands.get(i)).append(this.getOpSymbol());
		}
		
		
		//sb.append(operands.stream().map(op -> op.toString()).collect(Collectors.joining(this.getOpSymbol())));
		sb.append(operands.get(i));
		
		if (operands.size() > 1) {
			sb.append(this.getClosingSymbol());
		}
			
		return sb.toString();
	}
	*/
	
	
	
	public List<Expression> getOperands() {
		return Collections.unmodifiableList(operands);
	}
	
	
	
	public int getOperandsSize() {
		return operands.size();
	}
	
	

	

}
